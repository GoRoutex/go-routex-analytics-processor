package vn.com.routex.hub.analytics.processor.domain.trip.readmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.analytics.processor.domain.trip.TripStatus;

import java.time.OffsetDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TripSearchView {
    private String id;
    private String routeId;
    private String merchantId;
    private String vehicleId;
    private String driverId;
    private String tripCode;
    private String pickupBranch;
    private String originCode;
    private String originName;
    private String destinationCode;
    private String destinationName;
    private OffsetDateTime departureTime;
    private String rawDepartureTime;
    private String rawDepartureDate;
    private TripStatus status;
}
