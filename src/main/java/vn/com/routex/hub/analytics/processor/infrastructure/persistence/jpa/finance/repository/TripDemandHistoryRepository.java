package vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.jpa.finance.entity.TripDemandHistoryEntity;

import java.util.List;

@Repository
public interface TripDemandHistoryRepository extends JpaRepository<TripDemandHistoryEntity, String> {

    @Query(value = "SELECT route_id as routeId, " +
            "SUM(booked_seats) as ticketCount, " +
            "AVG(occupancy_rate) as occupancyRate " +
            "FROM TRIP_DEMAND_HISTORY " +
            "WHERE merchant_id = :merchantId " +
            "GROUP BY route_id " +
            "ORDER BY ticketCount DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findPopularRoutes(@Param("merchantId") String merchantId);
}
