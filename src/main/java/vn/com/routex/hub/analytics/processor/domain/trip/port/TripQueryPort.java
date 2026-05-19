package vn.com.routex.hub.analytics.processor.domain.trip.port;

import vn.com.routex.hub.analytics.processor.domain.trip.readmodel.TripFetchView;
import vn.com.routex.hub.analytics.processor.domain.trip.readmodel.TripSearchView;

import java.util.List;

public interface TripQueryPort {
    List<TripSearchView> searchAssignedTrips(
            String merchantId,
            String origin,
            String destination,
            int pageNumber,
            int pageSize
    );


}
