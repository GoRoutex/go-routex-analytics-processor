package vn.com.routex.hub.analytics.processor.application.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.go.routex.identity.security.base.ApiResult;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewResult;
import vn.com.routex.hub.analytics.processor.application.service.AdminPlatformDashboardService;
import vn.com.routex.hub.analytics.processor.infrastructure.grpc.client.MerchantGrpcClient;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.AdminPlatformOverviewResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPlatformDashboardServiceImpl implements AdminPlatformDashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    private final MerchantGrpcClient merchantGrpcClient;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    @Override
    public FetchAdminPlatformOverviewResult getPlatformOverview(FetchAdminPlatformOverviewQuery query) {
        LocalDate to = query.to() != null ? query.to() : LocalDate.now();
        LocalDate from = query.from() != null ? query.from() : to.minusDays(6);
        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        String granularity = normalizeGranularity(query.granularity());
        List<Object[]> statsRows = nativeRows("""
                SELECT stats_date,
                       COALESCE(SUM(total_revenue), 0) AS revenue,
                       COALESCE(SUM(total_tickets), 0) AS tickets,
                       COALESCE(AVG(occupancy_rate), 0) AS occupancy
                FROM merchant_daily_stats
                WHERE stats_date BETWEEN :fromDate AND :toDate
                GROUP BY stats_date
                ORDER BY stats_date
                """, Map.of("fromDate", from, "toDate", to));

        BigDecimal totalRevenue = statsRows.stream()
                .map(row -> toBigDecimal(row[1]))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long ticketsSold = statsRows.stream()
                .mapToLong(row -> toLong(row[2]))
                .sum();

        double averageOccupancyRate = calculatePlatformOccupancy(from, to, statsRows);

        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(Math.max(0, ChronoUnit.DAYS.between(from, to)));
        Object[] previousTotals = firstRow("""
                SELECT COALESCE(SUM(total_revenue), 0) AS revenue,
                       COALESCE(SUM(total_tickets), 0) AS tickets
                FROM merchant_daily_stats
                WHERE stats_date BETWEEN :fromDate AND :toDate
                """, Map.of("fromDate", previousFrom, "toDate", previousTo));

        long activeMerchants = countActiveMerchants(from, to);
        long totalMerchants = countTotalMerchants(activeMerchants);

        AdminPlatformOverviewResponse.Summary summary = AdminPlatformOverviewResponse.Summary.builder()
                .totalRevenue(totalRevenue)
                .activeMerchants(activeMerchants)
                .totalMerchants(totalMerchants)
                .averageOccupancyRate(round(averageOccupancyRate))
                .openComplaints(0L)
                .ticketsSold(ticketsSold)
                .revenueGrowthRate(round(calculateGrowth(totalRevenue, toBigDecimal(previousTotals != null ? previousTotals[0] : BigDecimal.ZERO))))
                .trafficGrowthRate(round(calculateGrowth(ticketsSold, toLong(previousTotals != null ? previousTotals[1] : 0))))
                .build();

        AdminPlatformOverviewResponse.PlatformOverviewData data = AdminPlatformOverviewResponse.PlatformOverviewData.builder()
                .summary(summary)
                .customerTrafficSeries(toSeries(statsRows, 2))
                .revenueSeries(toSeries(statsRows, 1))
                .merchantPerformance(fetchMerchantPerformance(from, to))
                .regionDemand(fetchRegionDemand(from, to))
                .partnerStatusDistribution(fetchPartnerStatusDistribution(totalMerchants))
                .from(from)
                .to(to)
                .granularity(granularity)
                .build();

        AdminPlatformOverviewResponse response = AdminPlatformOverviewResponse.builder()
                .result(ApiResult.builder().responseCode("00").description("Success").build())
                .data(data)
                .build();

        return FetchAdminPlatformOverviewResult.builder().data(response).build();
    }

    private List<AdminPlatformOverviewResponse.TimeSeriesPoint> toSeries(List<Object[]> rows, int valueIndex) {
        return rows.stream()
                .map(row -> {
                    LocalDate date = toLocalDate(row[0]);
                    return AdminPlatformOverviewResponse.TimeSeriesPoint.builder()
                            .date(date)
                            .label(date != null ? date.format(DAY_LABEL_FORMAT) : "")
                            .value(toBigDecimal(row[valueIndex]))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AdminPlatformOverviewResponse.MerchantPerformance> fetchMerchantPerformance(LocalDate from, LocalDate to) {
        List<Object[]> rows = nativeRows("""
                SELECT s.merchant_id,
                       COALESCE(MAX(m.display_name), s.merchant_id) AS merchant_name,
                       COALESCE(SUM(s.total_revenue), 0) AS revenue,
                       COALESCE(SUM(s.total_tickets), 0) AS tickets,
                       COALESCE(AVG(s.occupancy_rate), 0) AS occupancy
                FROM merchant_daily_stats s
                LEFT JOIN merchants m ON m.id = s.merchant_id
                WHERE s.stats_date BETWEEN :fromDate AND :toDate
                GROUP BY s.merchant_id
                ORDER BY revenue DESC, tickets DESC
                LIMIT 10
                """, Map.of("fromDate", from, "toDate", to));

        return rows.stream()
                .map(row -> {
                    double occupancy = round(toDouble(row[4]));
                    return AdminPlatformOverviewResponse.MerchantPerformance.builder()
                            .merchantId(toString(row[0]))
                            .merchantName(firstNonBlank(toString(row[1]), shortId(toString(row[0]))))
                            .revenue(toBigDecimal(row[2]))
                            .ticketsSold(toLong(row[3]))
                            .occupancyRate(occupancy)
                            .performanceRate(occupancy)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AdminPlatformOverviewResponse.RegionDemand> fetchRegionDemand(LocalDate from, LocalDate to) {
        List<Object[]> rows = nativeRows("""
                SELECT route_id,
                       COALESCE(SUM(booked_seats), 0) AS tickets
                FROM trip_demand_history
                WHERE departure_date BETWEEN :fromDate AND :toDate
                GROUP BY route_id
                ORDER BY tickets DESC
                LIMIT 8
                """, Map.of("fromDate", from, "toDate", to));

        long totalTickets = rows.stream().mapToLong(row -> toLong(row[1])).sum();
        Map<String, String> routeNames = fetchRouteNames(rows.stream().map(row -> toString(row[0])).filter(Objects::nonNull).toList());

        return rows.stream()
                .map(row -> {
                    String routeId = toString(row[0]);
                    long tickets = toLong(row[1]);
                    return AdminPlatformOverviewResponse.RegionDemand.builder()
                            .regionId(routeId)
                            .regionName(firstNonBlank(routeNames.get(routeId), "Tuyến " + shortId(routeId)))
                            .ticketsSold(tickets)
                            .percentage(totalTickets > 0 ? round(tickets * 100.0 / totalTickets) : 0.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AdminPlatformOverviewResponse.PartnerStatusDistribution> fetchPartnerStatusDistribution(long knownTotalMerchants) {
        List<Object[]> rows = nativeRows("""
                SELECT COALESCE(CAST(status AS TEXT), 'UNKNOWN') AS status,
                       COUNT(*) AS total
                FROM merchants
                GROUP BY status
                ORDER BY total DESC
                """, Collections.emptyMap());
        long total = rows.stream().mapToLong(row -> toLong(row[1])).sum();
        if (total == 0 && knownTotalMerchants > 0) {
            rows = new ArrayList<>();
            rows.add(new Object[]{"ACTIVE", knownTotalMerchants});
            total = knownTotalMerchants;
        }
        long finalTotal = total;
        return rows.stream()
                .map(row -> {
                    long count = toLong(row[1]);
                    return AdminPlatformOverviewResponse.PartnerStatusDistribution.builder()
                            .status(toString(row[0]))
                            .count(count)
                            .percentage(finalTotal > 0 ? round(count * 100.0 / finalTotal) : 0.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double calculatePlatformOccupancy(LocalDate from, LocalDate to, List<Object[]> statsRows) {
        Object[] row = firstRow("""
                SELECT COALESCE(SUM(booked_seats), 0) AS booked,
                       COALESCE(SUM(total_seats), 0) AS total
                FROM trip_demand_history
                WHERE departure_date BETWEEN :fromDate AND :toDate
                """, Map.of("fromDate", from, "toDate", to));
        long bookedSeats = toLong(row != null ? row[0] : 0);
        long totalSeats = toLong(row != null ? row[1] : 0);
        if (totalSeats > 0) {
            return bookedSeats * 100.0 / totalSeats;
        }
        return statsRows.stream().mapToDouble(rowValue -> toDouble(rowValue[3])).average().orElse(0.0);
    }

    private long countActiveMerchants(LocalDate from, LocalDate to) {
        Object[] row = firstRow("""
                SELECT COUNT(DISTINCT id)
                FROM merchants
                WHERE status = 'ACTIVE'
                """, Collections.emptyMap());
        long activeFromMerchantTable = toLong(row != null ? row[0] : 0);
        if (activeFromMerchantTable > 0) {
            return activeFromMerchantTable;
        }
        Object[] fallback = firstRow("""
                SELECT COUNT(DISTINCT merchant_id)
                FROM merchant_daily_stats
                WHERE stats_date BETWEEN :fromDate AND :toDate
                """, Map.of("fromDate", from, "toDate", to));
        return toLong(fallback != null ? fallback[0] : 0);
    }

    private long countTotalMerchants(long activeFallback) {
        Object[] row = firstRow("SELECT COUNT(*) FROM merchants", Collections.emptyMap());
        long total = toLong(row != null ? row[0] : 0);
        return total > 0 ? total : activeFallback;
    }

    private Map<String, String> fetchRouteNames(List<String> routeIds) {
        if (routeIds == null || routeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return merchantGrpcClient.getRouteNames(routeIds);
        } catch (Exception e) {
            sLog.error("Error fetching admin region route names via gRPC: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> nativeRows(String sql, Map<String, Object> parameters) {
        try {
            Query query = entityManager.createNativeQuery(sql);
            parameters.forEach(query::setParameter);
            List<Object> rawRows = query.getResultList();
            return rawRows.stream()
                    .map(row -> row instanceof Object[] rowArray ? rowArray : new Object[]{row})
                    .collect(Collectors.toList());
        } catch (Exception e) {
            sLog.error("Admin platform overview query failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Object[] firstRow(String sql, Map<String, Object> parameters) {
        List<Object[]> rows = nativeRows(sql, parameters);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String normalizeGranularity(String granularity) {
        if (granularity == null || granularity.isBlank()) {
            return "DAY";
        }
        return switch (granularity.trim().toUpperCase()) {
            case "HOUR", "DAY", "WEEK", "MONTH" -> granularity.trim().toUpperCase();
            default -> "DAY";
        };
    }

    private double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private double calculateGrowth(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return (current - previous) * 100.0 / previous;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof Date date) return date.toLocalDate();
        if (value != null) {
            return LocalDate.parse(value.toString());
        }
        return null;
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }

    private String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String shortId(String id) {
        if (id == null || id.isBlank()) return "N/A";
        return id.length() <= 8 ? id : id.substring(0, 8);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
