package vn.com.routex.hub.analytics.processor.infrastructure.persistence.security.envelope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import vn.com.go.routex.identity.security.base.BaseRequest;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.config.RequestAttributes;

import java.util.List;

@UtilityClass
public class RequestEnvelopeExtractor {

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    public static BaseRequest extract(HttpServletRequest request, String body, ObjectMapper objectMapper)
            throws JsonProcessingException {
        if (body != null && !body.isBlank()) {
            return objectMapper.readValue(body, BaseRequest.class);
        }

        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            throw new IllegalArgumentException("Missing request envelope body");
        }

        // Prefer standard headers, but accept common variants. Also accept query params for convenience.
        String requestId = firstNonBlank(
                request.getHeader(RequestAttributes.REQUEST_ID),
                request.getParameter("requestId")
        );
        String requestDateTime = firstNonBlank(
                request.getHeader(RequestAttributes.REQUEST_DATE_TIME),
                request.getParameter("requestDateTime")
        );
        String channel = firstNonBlank(
                request.getHeader(RequestAttributes.CHANNEL),
                request.getParameter("channel")
        );

        if (requestId == null || requestDateTime == null || channel == null) {
            throw new IllegalArgumentException("Missing request envelope headers");
        }

        return BaseRequest.builder()
                .requestId(requestId)
                .requestDateTime(requestDateTime)
                .channel(channel)
                .build();
    }
}
