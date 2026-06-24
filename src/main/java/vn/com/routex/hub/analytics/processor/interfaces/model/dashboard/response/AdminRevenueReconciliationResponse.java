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
public class AdminRevenueReconciliationResponse extends BaseResponse<AdminRevenueReconciliationResponse.RevenueReconciliationData> implements Serializable {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class RevenueReconciliationData implements Serializable {
        private RevenueSummary summary;
        private List<RevenueTrendPoint> revenueTrend;
        private List<MerchantRevenueBar> merchantRevenueBars;
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
    public static class RevenueSummary implements Serializable {
        private BigDecimal grossRevenue;
        private BigDecimal platformRevenue;
        private BigDecimal merchantRevenue;
        private BigDecimal discountAmount;
        private Double commissionRate;
        private Long ticketsSold;
        private Double platformRevenueGrowthRate;
        private Double merchantRevenueGrowthRate;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class RevenueTrendPoint implements Serializable {
        private String label;
        private LocalDate date;
        private BigDecimal grossRevenue;
        private BigDecimal platformRevenue;
        private BigDecimal merchantRevenue;
        private Double commissionRate;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantRevenueBar implements Serializable {
        private String merchantId;
        private String merchantName;
        private BigDecimal grossRevenue;
        private BigDecimal platformRevenue;
        private BigDecimal merchantRevenue;
        private Double commissionRate;
        private Long ticketsSold;
    }
}
