package vn.com.routex.hub.analytics.processor.infrastructure.kafka.event;

import lombok.Builder;
import vn.com.routex.hub.analytics.processor.domain.trip.TripStatus;

import java.time.OffsetDateTime;

@Builder
public record TripAssignedEvent(
        String tripId,
        String vehicleId,
        String driverId,
        OffsetDateTime departureTime,
        String originName,
        String destinationName,
        String assignedBy,
        OffsetDateTime assignedAt,
        TripStatus status
) {
}
