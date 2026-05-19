package vn.com.routex.hub.analytics.processor.domain.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.domain.analytics.port.MerchantAnalyticsService;
import vn.com.routex.hub.analytics.processor.application.service.HolidayService;
import vn.com.routex.hub.analytics.processor.application.service.cache.DashboardCacheService;
import vn.com.routex.hub.analytics.processor.infrastructure.grpc.client.MerchantGrpcClient;
import vn.com.routex.hub.analytics.processor.infrastructure.kafka.event.TicketIssuedEvent;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.entity.MerchantDailyStatsEntity;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.entity.TripDemandHistoryEntity;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.repository.MerchantDailyStatsRepository;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.repository.TripDemandHistoryRepository;
import vn.com.routex.hub.grpc.TripDetailsResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MerchantAnalyticsServiceImpl implements MerchantAnalyticsService {

    private final MerchantDailyStatsRepository dailyStatsRepository;
    private final TripDemandHistoryRepository tripDemandHistoryRepository;
    private final MerchantGrpcClient merchantGrpcClient;
    private final HolidayService holidayService;
    private final DashboardCacheService dashboardCacheService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @Override
    @Transactional
    public void processTicketIssuedEvent(TicketIssuedEvent event) {
        sLog.info("Processing TicketIssuedEvent for booking: {}", event.bookingId());

        String tripId = event.tripId();
        TripDetailsResponse trip;
        try {
            trip = merchantGrpcClient.getTripDetails(tripId);
        } catch (Exception e) {
            sLog.error("Trip details not found via gRPC for id: {}, skipping analytics processing", tripId, e);
            return;
        }

        String merchantId = trip.getMerchantId();
        String routeId = trip.getRouteId();
        OffsetDateTime departureTime = OffsetDateTime.now();
        if (trip.getDepartureTime() != null && !trip.getDepartureTime().isEmpty()) {
            try {
                departureTime = OffsetDateTime.parse(trip.getDepartureTime());
            } catch (Exception ex) {
                departureTime = OffsetDateTime.now();
            }
        }

        LocalDate statsDate = event.paidAt() != null ? event.paidAt().toLocalDate() : LocalDate.now();

        // 1. Calculate financial details
        BigDecimal amount = event.totalAmount() != null ? event.totalAmount() : BigDecimal.ZERO;
        int ticketCount = event.tickets() != null ? event.tickets().size() : 0;

        // Default commission rate: 10%
        BigDecimal commissionRate = new BigDecimal("10.00");
        BigDecimal systemCommission = amount.multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal merchantShare = amount.subtract(systemCommission);

        // 2. Update Daily Stats
        String statsId = merchantId + "_" + statsDate;
        MerchantDailyStatsEntity stats = dailyStatsRepository.findById(statsId)
                .orElse(MerchantDailyStatsEntity.builder()
                        .id(statsId)
                        .merchantId(merchantId)
                        .statsDate(statsDate)
                        .totalTickets(0)
                        .totalRevenue(BigDecimal.ZERO)
                        .totalDiscount(BigDecimal.ZERO)
                        .merchantShare(BigDecimal.ZERO)
                        .systemCommission(BigDecimal.ZERO)
                        .build());

        stats.setTotalTickets(stats.getTotalTickets() + ticketCount);
        stats.setTotalRevenue(stats.getTotalRevenue().add(amount));
        stats.setMerchantShare(stats.getMerchantShare().add(merchantShare));
        stats.setSystemCommission(stats.getSystemCommission().add(systemCommission));

        dailyStatsRepository.save(stats);

        // 3. Update Trip Demand History
        TripDemandHistoryEntity history = tripDemandHistoryRepository.findById(tripId)
                .orElse(TripDemandHistoryEntity.builder()
                        .id(tripId)
                        .merchantId(merchantId)
                        .routeId(routeId)
                        .departureDate(departureTime.toLocalDate())
                        .departureHour(departureTime.getHour())
                        .dayOfWeek(departureTime.getDayOfWeek().getValue())
                        .totalSeats(45) // standard capacity
                        .bookedSeats(0)
                        .isHoliday(holidayService.isHolidayOrPeakDay(departureTime.toLocalDate()))
                        .build());

        history.setBookedSeats(history.getBookedSeats() + ticketCount);
        if (history.getTotalSeats() > 0) {
            history.setOccupancyRate((double) history.getBookedSeats() / history.getTotalSeats() * 100);
        } else {
            history.setOccupancyRate(0.0);
        }

        tripDemandHistoryRepository.save(history);

        // 4. Evict Redis Cache for the merchant
        dashboardCacheService.evictMerchantDashboardCache(merchantId);
        sLog.info("Successfully updated stats and evicted dashboard cache for merchant: {}", merchantId);
    }
}
