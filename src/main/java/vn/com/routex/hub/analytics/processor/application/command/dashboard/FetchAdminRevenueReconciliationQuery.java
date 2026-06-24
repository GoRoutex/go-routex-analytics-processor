package vn.com.routex.hub.analytics.processor.application.command.dashboard;

import lombok.Builder;
import vn.com.routex.hub.analytics.processor.application.RequestContext;

import java.time.LocalDate;

@Builder
public record FetchAdminRevenueReconciliationQuery(
        LocalDate from,
        LocalDate to,
        String filter,
        String granularity,
        Integer topMerchants,
        RequestContext context
) {
}
