package vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Bao Pham
 * @created 31/03/2025
 * @project go-routex-driver-service
 **/

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundEx extends RuntimeException {
    public ResourceNotFoundEx(String s) {
        super(s);
    }


    public ResourceNotFoundEx(String s, Throwable cause) {
        super(s, cause);
    }
}
