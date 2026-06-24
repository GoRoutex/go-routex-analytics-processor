package vn.com.routex.hub.analytics.processor.application.command.dashboard;

import lombok.Builder;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.AdminPlatformOverviewResponse;

@Builder
public record FetchAdminPlatformOverviewResult(
        AdminPlatformOverviewResponse data
) {
}
