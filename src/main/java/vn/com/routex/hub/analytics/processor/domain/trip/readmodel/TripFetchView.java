package vn.com.routex.hub.analytics.processor.domain.trip.readmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.routex.hub.analytics.processor.domain.trip.TripStatus;

import java.time.OffsetDateTime;
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TripFetchView {
    private String id;
    private String tripCode;
    private String routeId;
    private String creator;
    private String originCode;
    private String originName;
    private String destinationCode;
    private String destinationName;
    private OffsetDateTime departureTime;
    private String rawDepartureTime;
    private String rawDepartureDate;
    private TripStatus status;
}