package vn.com.routex.hub.analytics.processor.application.service;

import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchMerchantDashboardQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchMerchantDashboardResult;

public interface DashboardService {
    FetchMerchantDashboardResult getDashboard(FetchMerchantDashboardQuery query);
}
