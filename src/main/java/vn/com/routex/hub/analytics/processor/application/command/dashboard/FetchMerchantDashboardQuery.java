package vn.com.routex.hub.analytics.processor.application.command.dashboard;

import lombok.Builder;
import vn.com.routex.hub.analytics.processor.application.RequestContext;

@Builder
public record FetchMerchantDashboardQuery(
        String merchantId,
        String filterType,
        RequestContext context
) {
}
