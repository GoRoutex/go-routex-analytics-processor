package vn.com.routex.hub.analytics.processor.interfaces.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.go.routex.identity.security.base.BaseRequest;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchMerchantDashboardQuery;
import vn.com.routex.hub.analytics.processor.application.command.dashboard.FetchMerchantDashboardResult;
import vn.com.routex.hub.analytics.processor.application.service.DashboardService;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.ApiRequestUtils;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.HttpUtils;
import vn.com.routex.hub.analytics.processor.interfaces.model.dashboard.response.MerchantDashboardResponse;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.ANALYTIC_PATH;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.API_PATH;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.API_VERSION;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ApiConstant.DASHBOARD_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + ANALYTIC_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('merchant:dashboard') or hasRole('MERCHANT_OWNER')")
public class MerchantDashboardController {

    private final DashboardService dashboardService;

    @GetMapping(DASHBOARD_PATH)
    public ResponseEntity<MerchantDashboardResponse> getDashboard(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false, defaultValue = "DAY") String filterType,
            HttpServletRequest servletRequest) {

        BaseRequest baseRequest = ApiRequestUtils.getBaseRequestOrDefault(servletRequest);
        String resolvedMerchantId = (merchantId != null) ? merchantId : ApiRequestUtils.getMerchantId(servletRequest);

        FetchMerchantDashboardQuery query = FetchMerchantDashboardQuery.builder()
                .merchantId(resolvedMerchantId)
                .filterType(filterType)
                .context(ApiRequestUtils.getRequestContext(baseRequest))
                .build();

        FetchMerchantDashboardResult result = dashboardService.getDashboard(query);
        return HttpUtils.buildResponse(baseRequest, result.data());
    }
}
