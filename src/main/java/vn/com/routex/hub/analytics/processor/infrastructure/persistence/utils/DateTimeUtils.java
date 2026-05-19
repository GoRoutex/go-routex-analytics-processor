package vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils;

import lombok.experimental.UtilityClass;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception.BusinessException;

import java.time.format.DateTimeFormatter;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.INVALID_INPUT_ERROR;


@UtilityClass
public class DateTimeUtils {

    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public int parseIntOrThrow(String v, String field, String requestId, String requestDateTime, String channel) {
        if (v == null || v.isBlank()) {
            throw new BusinessException(requestId, requestDateTime, channel,
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, field + " is required"));
        }
        if (!v.trim().matches("^\\d{1,3}$")) {
            throw new BusinessException(requestId, requestDateTime, channel,
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, field + " must be numeric"));
        }
        return Integer.parseInt(v.trim());
    }

    public int safeStopOrder(String v) {
        try { return Integer.parseInt(v); } catch (Exception e) { return Integer.MAX_VALUE; }
    }
}
