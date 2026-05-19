package vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import vn.com.go.routex.identity.security.base.BaseRequest;
import vn.com.routex.hub.analytics.processor.application.RequestContext;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.config.RequestAttributes;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception.BusinessException;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.INVALID_INPUT_ERROR;

@UtilityClass
public class ApiRequestUtils {
    public BaseRequest getBaseRequestOrDefault(HttpServletRequest request) {

        String requestId =
                (String) request.getAttribute(RequestAttributes.REQUEST_ID);

        String requestDateTime =
                (String) request.getAttribute(RequestAttributes.REQUEST_DATE_TIME);

        String requestChannel =
                (String) request.getAttribute(RequestAttributes.CHANNEL);

        return BaseRequest.builder()
                .requestId(requestId)
                .requestDateTime(requestDateTime)
                .channel(requestChannel)
                .build();
    }

    public String getMerchantId(HttpServletRequest request) {
        String merchantId = (String) request.getAttribute(RequestAttributes.MERCHANT_ID);
        if (merchantId != null && !merchantId.isBlank()) {
            return merchantId.trim();
        }
        return null;
    }

    public String requireMerchantId(HttpServletRequest request, BaseRequest baseRequest) {
        String merchantId = getMerchantId(request);
        if (merchantId == null || merchantId.isBlank()) {
            throw new BusinessException(
                    baseRequest.getRequestId(),
                    baseRequest.getRequestDateTime(),
                    baseRequest.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, "merchantId is required")
            );
        }
        return merchantId;
    }

    public BaseRequest getHeader(RequestContext context) {
        return BaseRequest.builder()
                .requestId(context.requestId())
                .requestDateTime(context.requestDateTime())
                .channel(context.channel())
                .build();
    }

    public int parseIntOrDefault(
            String v,
            int defaultValue,
            String field,
            String requestId,
            String requestDateTime,
            String channel
    ) {
        if (v == null || v.isBlank()) return defaultValue;
        return DateTimeUtils.parseIntOrThrow(v, field, requestId, requestDateTime, channel);
    }


    public String firstNonBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    public RequestContext getRequestContext(BaseRequest request) {
        return RequestContext.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .build();
    }
}
