package vn.com.routex.hub.analytics.processor.domain.trip.port;


import vn.com.routex.hub.analytics.processor.domain.trip.TripStatus;
import vn.com.routex.hub.analytics.processor.domain.trip.model.TripAggregate;

import java.util.Optional;

public interface TripAggregateRepositoryPort {

    String generateTripCode(String originCode, String destinationCode);

    boolean existsByRouteId(String routeId, String merchantId);

    Optional<TripAggregate> findById(String tripId);

    Optional<TripAggregate> findById(String tripId, String merchantId);

    Optional<TripAggregate> findByRouteId(String routeId, String merchantId);

    void save(TripAggregate aggregate);

    void saveAll(java.util.List<TripAggregate> aggregates);


}
