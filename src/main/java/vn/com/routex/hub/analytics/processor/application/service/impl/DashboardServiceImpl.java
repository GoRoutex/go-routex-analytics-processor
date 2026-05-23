package vn.com.routex.hub.analytics.processor.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.go.routex.identity.security.base.ApiResult;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchMerchantDashboardQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchMerchantDashboardResult;
import vn.com.routex.hub.analytics.processor.application.service.DashboardService;
import vn.com.routex.hub.analytics.processor.application.service.cache.DashboardCacheService;
import vn.com.routex.hub.analytics.processor.infrastructure.cache.redisson.RedisDistributedLocker;
import vn.com.routex.hub.analytics.processor.infrastructure.cache.redisson.RedisDistributedService;
import vn.com.routex.hub.analytics.processor.infrastructure.grpc.client.MerchantGrpcClient;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.entity.MerchantDailyStatsEntity;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.repository.MerchantDailyStatsRepository;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.repository.TripDemandHistoryRepository;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.MerchantDashboardResponse;
import vn.com.routex.hub.grpc.RecentTripInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MerchantGrpcClient merchantGrpcClient;
    private final MerchantDailyStatsRepository dailyStatsRepository;
    private final TripDemandHistoryRepository tripDemandHistoryRepository;
    private final DashboardCacheService dashboardCacheService;
    private final RedisDistributedService redisDistributedService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    private static final String LOCK_KEY = "lock:dashboard:merchant:%s:filter:%s";

    @Override
    public FetchMerchantDashboardResult getDashboard(FetchMerchantDashboardQuery query) {
        String merchantId = query.merchantId();
        String filterType = query.filterType();

        // 1. Try Cache First
        Optional<MerchantDashboardResponse> cached = dashboardCacheService.getDashboard(merchantId, filterType);
        if (cached.isPresent()) {
            return FetchMerchantDashboardResult.builder().data(cached.get()).build();
        }

        // 2. Distributed Locking to prevent stampede
        String lockKey = String.format(LOCK_KEY, merchantId, filterType);
        RedisDistributedLocker locker = redisDistributedService.getDistributedLock(lockKey);
        try {
            boolean acquired = locker.tryLock(5, 30, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    // Double check cache
                    cached = dashboardCacheService.getDashboard(merchantId, filterType);
                    if (cached.isPresent()) return FetchMerchantDashboardResult.builder().data(cached.get()).build();

                    return FetchMerchantDashboardResult.builder().data(computeAndCacheDashboard(merchantId, filterType)).build();
                } finally {
                    locker.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return FetchMerchantDashboardResult.builder().data(computeAndCacheDashboard(merchantId, filterType)).build();
        } catch (Exception e) {
            sLog.error("Error computing dashboard under lock: {}", e.getMessage(), e);
            return FetchMerchantDashboardResult.builder().data(computeAndCacheDashboard(merchantId, filterType)).build();
        }

        return FetchMerchantDashboardResult.builder().data(computeAndCacheDashboard(merchantId, filterType)).build();
    }

    private MerchantDashboardResponse computeAndCacheDashboard(String merchantId, String filterType) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = calculateStartDate(now, filterType);
        LocalDate previousStartDate = calculatePreviousStartDate(startDate, filterType);

        // Current Period Stats
        List<MerchantDailyStatsEntity> dailyStats = dailyStatsRepository.findAllByMerchantIdAndStatsDateBetween(
                merchantId,
                startDate,
                now
        );

        // Previous Period Stats for Growth Calculation
        List<MerchantDailyStatsEntity> previousStats = dailyStatsRepository.findAllByMerchantIdAndStatsDateBetween(
                merchantId,
                previousStartDate,
                startDate.minusDays(1)
        );

        // 1. Aggregate Statistics
        int totalTickets = dailyStats.stream().mapToInt(MerchantDailyStatsEntity::getTotalTickets).sum();
        BigDecimal totalRevenue = dailyStats.stream()
                .map(MerchantDailyStatsEntity::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal merchantShare = dailyStats.stream()
                .map(MerchantDailyStatsEntity::getMerchantShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Growth Calculation
        int prevTickets = previousStats.stream().mapToInt(MerchantDailyStatsEntity::getTotalTickets).sum();
        BigDecimal prevRevenue = previousStats.stream()
                .map(MerchantDailyStatsEntity::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double ticketGrowth = calculateGrowth(totalTickets, prevTickets);
        double revenueGrowth = calculateGrowth(totalRevenue, prevRevenue);

        MerchantDashboardResponse.Statistics stats = MerchantDashboardResponse.Statistics.builder()
                .ticketsSold(totalTickets)
                .totalRevenue(totalRevenue)
                .merchantShare(merchantShare)
                .ticketGrowthRate(ticketGrowth)
                .revenueGrowthRate(revenueGrowth)
                .build();

        // 2. Revenue Chart Data
        List<MerchantDashboardResponse.RevenueChartData> chartData = dailyStats.stream()
                .map(s -> MerchantDashboardResponse.RevenueChartData.builder()
                        .label(s.getStatsDate().format(DateTimeFormatter.ofPattern("dd/MM")))
                        .revenue(s.getTotalRevenue())
                        .date(s.getStatsDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset()))
                        .build())
                .sorted(Comparator.comparing(MerchantDashboardResponse.RevenueChartData::getDate))
                .collect(Collectors.toList());

        // 3. Popular Routes
        List<Object[]> routeObjects = tripDemandHistoryRepository.findPopularRoutes(merchantId);
        List<String> routeIds = routeObjects.stream()
                .map(obj -> (String) obj[0])
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> routeNamesMap = new HashMap<>();
        if (!routeIds.isEmpty()) {
            try {
                routeNamesMap = merchantGrpcClient.getRouteNames(routeIds);
            } catch (Exception e) {
                sLog.error("Error fetching route names via gRPC: {}", e.getMessage(), e);
            }
        }

        final Map<String, String> finalRouteNamesMap = routeNamesMap;
        List<MerchantDashboardResponse.PopularRouteData> popularRoutes = routeObjects.stream()
                .map(obj -> {
                    String routeId = (String) obj[0];
                    Long ticketCount = ((Number) obj[1]).longValue();
                    Double occupancy = ((Number) obj[2]).doubleValue();

                    String routeName = finalRouteNamesMap.getOrDefault(routeId,
                            routeId != null ? "Tuyến " + (routeId.length() > 5 ? routeId.substring(0, 5) : routeId) : "Tuyến không xác định");

                    return MerchantDashboardResponse.PopularRouteData.builder()
                            .routeId(routeId)
                            .routeName(routeName)
                            .ticketCount(ticketCount.intValue())
                            .occupancyRate(occupancy)
                            .build();
                })
                .collect(Collectors.toList());

        // 5. Recent Trips (via gRPC)
        List<RecentTripInfo> recentTripsGrpc = List.of();
        try {
            recentTripsGrpc = merchantGrpcClient.getRecentTrips(merchantId, 5);
        } catch (Exception e) {
            sLog.error("Error fetching recent trips via gRPC: {}", e.getMessage(), e);
        }

        List<MerchantDashboardResponse.RecentTripData> recentTrips = recentTripsGrpc.stream()
                .map(t -> {
                    OffsetDateTime departureTime = null;
                    if (t.getDepartureTime() != null && !t.getDepartureTime().isEmpty()) {
                        try {
                            departureTime = OffsetDateTime.parse(t.getDepartureTime());
                        } catch (Exception e) {
                            departureTime = OffsetDateTime.now();
                        }
                    }
                    return MerchantDashboardResponse.RecentTripData.builder()
                            .tripId(t.getTripId())
                            .routeName(t.getRouteName())
                            .vehiclePlate(t.getVehiclePlate())
                            .departureTime(departureTime)
                            .status(t.getStatus())
                            .bookedSeats(t.getBookedSeats())
                            .build();
                })
                .collect(Collectors.toList());

        ApiResult apiResult = ApiResult.builder()
                .responseCode("00")
                .description("Success")
                .build();

        MerchantDashboardResponse response = MerchantDashboardResponse.builder()
                .result(apiResult)
                .data(MerchantDashboardResponse.DashboardData.builder()
                        .stats(stats)
                        .revenueChart(chartData)
                        .popularRoutes(popularRoutes)
                        .recentTrips(recentTrips)
                        .build())
                .build();

        // 3. Store in Cache
        dashboardCacheService.putDashboard(merchantId, filterType, response);

        return response;
    }

    private double calculateGrowth(double current, double previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current - previous) / previous) * 100.0;
    }

    private double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }

    private LocalDate calculatePreviousStartDate(LocalDate startDate, String filterType) {
        if (filterType == null) return startDate.minusWeeks(1);
        return switch (filterType.toUpperCase()) {
            case "DAY" -> startDate.minusDays(1);
            case "WEEK" -> startDate.minusWeeks(1);
            case "MONTH" -> startDate.minusMonths(1);
            case "YEAR" -> startDate.minusYears(1);
            default -> startDate.minusWeeks(1);
        };
    }

    private LocalDate calculateStartDate(LocalDate now, String filterType) {
        if (filterType == null) return now.minusDays(7);

        return switch (filterType.toUpperCase()) {
            case "DAY" -> now;
            case "WEEK" -> now.minusWeeks(1);
            case "MONTH" -> now.minusMonths(1);
            case "YEAR" -> now.minusYears(1);
            default -> now.minusDays(7);
        };
    }
}
