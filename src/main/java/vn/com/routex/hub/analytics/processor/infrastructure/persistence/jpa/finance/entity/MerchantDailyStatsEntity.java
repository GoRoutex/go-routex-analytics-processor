package vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.analytics.processor.domain.auditing.AbstractAuditingEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "MERCHANT_DAILY_STATS")
public class MerchantDailyStatsEntity extends AbstractAuditingEntity {

    @Id
    private String id; // merchantId_date

    @Column(name = "MERCHANT_ID", nullable = false)
    private String merchantId;

    @Column(name = "STATS_DATE", nullable = false)
    private LocalDate statsDate;

    @Column(name = "TOTAL_TICKETS", nullable = false)
    private Integer totalTickets;

    @Column(name = "TOTAL_REVENUE", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "TOTAL_DISCOUNT")
    private BigDecimal totalDiscount;

    @Column(name = "MERCHANT_SHARE", nullable = false)
    private BigDecimal merchantShare;

    @Column(name = "SYSTEM_COMMISSION", nullable = false)
    private BigDecimal systemCommission;

    @Column(name = "OCCUPANCY_RATE")
    private Double occupancyRate;
}
