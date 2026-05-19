package vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.com.go.routex.identity.security.base.BaseRequest;
import vn.com.go.routex.identity.security.base.BaseResponse;
import vn.com.routex.hub.analytics.processor.application.RequestContext;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.config.RequestAttributes;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception.BusinessException;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR_MESSAGE;

@UtilityClass
public class HttpUtils {

    public RequestContext toContext(BaseRequest request) {
        return toContext(request, null);
    }

    public RequestContext toContext(HttpServletRequest request) {
        return RequestContext.builder()
                .requestId((String) request.getAttribute(RequestAttributes.REQUEST_ID))
                .requestDateTime((String) request.getAttribute(RequestAttributes.REQUEST_DATE_TIME))
                .channel((String) request.getAttribute(RequestAttributes.CHANNEL))
                .merchantId((String) request.getAttribute(RequestAttributes.MERCHANT_ID))
                .build();
    }

    public RequestContext toContext(BaseRequest request, String merchantId) {
        return RequestContext.builder()
                .requestId(request.getRequestId())
                .requestDateTime(request.getRequestDateTime())
                .channel(request.getChannel())
                .merchantId(merchantId)
                .build();
    }

    public <T, R extends BaseResponse<T>> ResponseEntity<R> buildResponse(BaseRequest request, R response) {
        if (response == null) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(TIMEOUT_ERROR, TIMEOUT_ERROR_MESSAGE)
            );
        }

        response.setRequestId(request.getRequestId());
        response.setRequestDateTime(request.getRequestDateTime());
        response.setChannel(request.getChannel());

        return ResponseEntity
                .status(response.getData() == null ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
