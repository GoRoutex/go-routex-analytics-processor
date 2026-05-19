package vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception;

import lombok.EqualsAndHashCode;
import vn.com.go.routex.identity.security.base.ApiResult;
import vn.com.routex.hub.analytics.processor.application.RequestContext;

@EqualsAndHashCode(callSuper = true)
public class BusinessException extends BaseException {

    public BusinessException(String requestId, String requestDateTime, String channel, ApiResult result) {
        super(requestId, requestDateTime, channel, result);
    }

    public BusinessException(RequestContext context, ApiResult result) {
        super(context.requestId(), context.requestDateTime(), context.channel(), result);
    }

    public BusinessException(ApiResult result) { super(result); }
}
