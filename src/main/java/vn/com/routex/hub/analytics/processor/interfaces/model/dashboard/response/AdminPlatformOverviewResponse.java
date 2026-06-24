package vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.go.routex.identity.security.base.BaseResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AdminPlatformOverviewResponse extends BaseResponse<AdminPlatformOverviewResponse.PlatformOverviewData> implements Serializable {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class PlatformOverviewData implements Serializable {
        private Summary summary;
        private List<TimeSeriesPoint> customerTrafficSeries;
        private List<TimeSeriesPoint> revenueSeries;
        private List<MerchantPerformance> merchantPerformance;
        private List<RegionDemand> regionDemand;
        private List<PartnerStatusDistribution> partnerStatusDistribution;
        private LocalDate from;
        private LocalDate to;
        private String filter;
        private String granularity;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class Summary implements Serializable {
        private BigDecimal totalRevenue;
        private Long activeMerchants;
        private Long totalMerchants;
        private Double averageOccupancyRate;
        private Long openComplaints;
        private Long ticketsSold;
        private Double revenueGrowthRate;
        private Double trafficGrowthRate;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class TimeSeriesPoint implements Serializable {
        private String label;
        private LocalDate date;
        private BigDecimal value;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantPerformance implements Serializable {
        private String merchantId;
        private String merchantName;
        private BigDecimal revenue;
        private Long ticketsSold;
        private Double occupancyRate;
        private Double performanceRate;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class RegionDemand implements Serializable {
        private String regionId;
        private String regionName;
        private Long ticketsSold;
        private Double percentage;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class PartnerStatusDistribution implements Serializable {
        private String status;
        private Long count;
        private Double percentage;
    }
}
