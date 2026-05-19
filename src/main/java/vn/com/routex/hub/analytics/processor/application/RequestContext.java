package vn.com.routex.hub.analytics.processor.application;

import lombok.Builder;

@Builder
public record RequestContext(
        String requestId,
        String requestDateTime,
        String channel,
        String merchantId
) {
}
