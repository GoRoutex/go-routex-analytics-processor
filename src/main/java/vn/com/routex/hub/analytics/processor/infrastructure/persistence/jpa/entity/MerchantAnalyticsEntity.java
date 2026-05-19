package vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.analytics.processor.domain.auditing.AbstractAuditingEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "merchant_analytics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"merchant_id", "analytics_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MerchantAnalyticsEntity extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "analytics_date", nullable = false)
    private LocalDate analyticsDate;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "ticket_count", nullable = false)
    private Long ticketCount;

    @Column(name = "booking_count", nullable = false)
    private Long bookingCount;

    @Column(name = "cancellation_count", nullable = false)
    private Long cancellationCount;

    @Column(name = "trip_count", nullable = false)
    private Long tripCount;

    @Column(name = "total_seats_occupied", nullable = false)
    private Long totalSeatsOccupied;

    @Column(name = "total_seats_available", nullable = false)
    private Long totalSeatsAvailable;
}
