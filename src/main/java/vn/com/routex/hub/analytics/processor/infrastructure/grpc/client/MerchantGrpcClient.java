package vn.com.routex.hub.analytics.processor.infrastructure.grpc.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import vn.com.routex.hub.grpc.MerchantGrpcServiceGrpc;
import vn.com.routex.hub.grpc.RecentTripInfo;
import vn.com.routex.hub.grpc.RecentTripsRequest;
import vn.com.routex.hub.grpc.RecentTripsResponse;
import vn.com.routex.hub.grpc.RouteNamesRequest;
import vn.com.routex.hub.grpc.RouteNamesResponse;
import vn.com.routex.hub.grpc.TripDetailsRequest;
import vn.com.routex.hub.grpc.TripDetailsResponse;

import java.util.List;
import java.util.Map;

@Service
public class MerchantGrpcClient {

    @GrpcClient("merchantService")
    private MerchantGrpcServiceGrpc.MerchantGrpcServiceBlockingStub merchantServiceStub;

    public Map<String, String> getRouteNames(List<String> routeIds) {
        RouteNamesRequest request = RouteNamesRequest.newBuilder()
                .addAllRouteIds(routeIds)
                .build();
        RouteNamesResponse response = merchantServiceStub.getRouteNames(request);
        return response.getRouteNamesMap();
    }

    public List<RecentTripInfo> getRecentTrips(String merchantId, int limit) {
        RecentTripsRequest request = RecentTripsRequest.newBuilder()
                .setMerchantId(merchantId)
                .setLimit(limit)
                .build();
        RecentTripsResponse response = merchantServiceStub.getRecentTrips(request);
        return response.getTripsList();
    }

    public TripDetailsResponse getTripDetails(String tripId) {
        TripDetailsRequest request = TripDetailsRequest.newBuilder()
                .setTripId(tripId)
                .build();
        return merchantServiceStub.getTripDetails(request);
    }
}
