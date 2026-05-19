package vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.go.routex.identity.security.base.ApiResult;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {
    private String requestId;
    private String requestDateTime;
    private String channel;
    private ApiResult result;

    public BaseException(String requestId, String requestDateTime, String channel, ApiResult result) {
        this.requestId = requestId;
        this.requestDateTime = requestDateTime;
        this.channel = channel;
        this.result = result;
    }

    public BaseException(ApiResult result) {
        this.result = result;
    }
}
