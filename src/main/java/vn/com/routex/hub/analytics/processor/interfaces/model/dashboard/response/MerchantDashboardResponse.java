package vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.go.routex.identity.security.base.BaseResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MerchantDashboardResponse extends BaseResponse<MerchantDashboardResponse.DashboardData> implements Serializable {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class DashboardData implements Serializable {
        private Statistics stats;
        private List<RevenueChartData> revenueChart;
        private List<PopularRouteData> popularRoutes;
        private List<RecentTripData> recentTrips;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class Statistics implements Serializable {
        private Integer ticketsSold;
        private BigDecimal totalRevenue;
        private BigDecimal merchantShare;
        private Double ticketGrowthRate;
        private Double revenueGrowthRate;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class RevenueChartData implements Serializable {
        private String label;
        private BigDecimal revenue;
        private OffsetDateTime date;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class PopularRouteData implements Serializable {
        private String routeId;
        private String routeName;
        private Integer ticketCount;
        private Double occupancyRate;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class RecentTripData implements Serializable {
        private String tripId;
        private String routeName;
        private String vehiclePlate;
        private OffsetDateTime departureTime;
        private String status;
        private Integer bookedSeats;
    }
}
