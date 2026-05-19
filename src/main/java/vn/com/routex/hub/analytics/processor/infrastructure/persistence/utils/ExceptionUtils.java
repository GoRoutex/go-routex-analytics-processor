package vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils;


import lombok.experimental.UtilityClass;
import vn.com.go.routex.identity.security.base.ApiResult;

@UtilityClass
public class ExceptionUtils {

    public ApiResult buildResultResponse(String responseCode, String description) {
        return ApiResult
                .builder()
                .responseCode(responseCode)
                .description(description)
                .build();
    }
}
