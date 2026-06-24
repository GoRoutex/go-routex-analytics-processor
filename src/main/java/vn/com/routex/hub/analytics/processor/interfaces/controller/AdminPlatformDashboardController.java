package vn.com.routex.hub.analytics.processor.interfaces.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.go.routex.identity.security.base.BaseRequest;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminPlatformOverviewResult;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminRevenueReconciliationQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchAdminRevenueReconciliationResult;
import vn.com.routex.hub.analytics.processor.application.service.AdminPlatformDashboardService;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.ApiRequestUtils;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.HttpUtils;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.AdminPlatformOverviewResponse;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.AdminRevenueReconciliationResponse;

import java.time.LocalDate;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.ANALYTIC_PATH;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.API_VERSION;

@RestController
@RequestMapping(API_PATH + API_VERSION + ANALYTIC_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('platform:dashboard')")
public class AdminPlatformDashboardController {

    private final AdminPlatformDashboardService adminPlatformDashboardService;

    @GetMapping("/admin/platform-overview")
    public ResponseEntity<AdminPlatformOverviewResponse> getPlatformOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "DAY") String granularity,
            HttpServletRequest servletRequest
    ) {
        BaseRequest baseRequest = ApiRequestUtils.getBaseRequestOrDefault(servletRequest);
        FetchAdminPlatformOverviewQuery query = FetchAdminPlatformOverviewQuery.builder()
                .from(from)
                .to(to)
                .filter(filter)
                .granularity(granularity)
                .context(ApiRequestUtils.getRequestContext(baseRequest))
                .build();

        FetchAdminPlatformOverviewResult result = adminPlatformDashboardService.getPlatformOverview(query);
        return HttpUtils.buildResponse(baseRequest, result.data());
    }

    @GetMapping("/admin/revenue-reconciliation")
    public ResponseEntity<AdminRevenueReconciliationResponse> getRevenueReconciliation(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "DAY") String granularity,
            @RequestParam(required = false, defaultValue = "10") Integer topMerchants,
            HttpServletRequest servletRequest
    ) {
        BaseRequest baseRequest = ApiRequestUtils.getBaseRequestOrDefault(servletRequest);
        FetchAdminRevenueReconciliationQuery query = FetchAdminRevenueReconciliationQuery.builder()
                .from(from)
                .to(to)
                .filter(filter)
                .granularity(granularity)
                .topMerchants(topMerchants)
                .context(ApiRequestUtils.getRequestContext(baseRequest))
                .build();

        FetchAdminRevenueReconciliationResult result = adminPlatformDashboardService.getRevenueReconciliation(query);
        return HttpUtils.buildResponse(baseRequest, result.data());
    }
}
