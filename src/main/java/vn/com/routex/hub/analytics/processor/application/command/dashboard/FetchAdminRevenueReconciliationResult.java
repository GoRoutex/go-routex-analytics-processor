package vn.com.routex.hub.analytics.processor.application.command.dashboard;

import lombok.Builder;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.AdminRevenueReconciliationResponse;

@Builder
public record FetchAdminRevenueReconciliationResult(
        AdminRevenueReconciliationResponse data
) {
}
