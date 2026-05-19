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

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "TRIP_DEMAND_HISTORY")
public class TripDemandHistoryEntity extends AbstractAuditingEntity {

    @Id
    private String id; // tripId

    @Column(name = "MERCHANT_ID", nullable = false)
    private String merchantId;

    @Column(name = "ROUTE_ID", nullable = false)
    private String routeId;

    @Column(name = "DEPARTURE_DATE", nullable = false)
    private LocalDate departureDate;

    @Column(name = "DEPARTURE_HOUR", nullable = false)
    private Integer departureHour;

    @Column(name = "DAY_OF_WEEK", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "TOTAL_SEATS", nullable = false)
    private Integer totalSeats;

    @Column(name = "BOOKED_SEATS", nullable = false)
    private Integer bookedSeats;

    @Column(name = "OCCUPANCY_RATE", nullable = false)
    private Double occupancyRate;

    @Column(name = "IS_HOLIDAY")
    private Boolean isHoliday;
}
