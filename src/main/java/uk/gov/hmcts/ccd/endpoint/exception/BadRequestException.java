package uk.gov.hmcts.ccd.endpoint.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Public business failure: messages must be fixed, safe text without internal diagnostics or request values. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(final String message) {
        super(message);
    }
}
