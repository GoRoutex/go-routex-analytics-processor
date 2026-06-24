package vn.com.routex.hub.analytics.processor.application.service;

import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewResult;

public interface AdminPlatformDashboardService {
    FetchAdminPlatformOverviewResult getPlatformOverview(FetchAdminPlatformOverviewQuery query);
}
