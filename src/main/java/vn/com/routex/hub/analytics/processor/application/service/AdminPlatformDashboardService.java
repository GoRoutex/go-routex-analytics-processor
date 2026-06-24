package vn.com.routex.hub.analytics.processor.application.service;

import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewResult;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminRevenueReconciliationQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminRevenueReconciliationResult;

public interface AdminPlatformDashboardService {
    FetchAdminPlatformOverviewResult getPlatformOverview(FetchAdminPlatformOverviewQuery query);

    FetchAdminRevenueReconciliationResult getRevenueReconciliation(FetchAdminRevenueReconciliationQuery query);
}
