package uk.gov.hmcts.ccd.endpoint.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(final RuntimeException e, final WebRequest request) {
        final StringBuilder errorMsg = new StringBuilder(e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Message", e.getMessage());

        if (e instanceof BadRequestException) {
            return handleExceptionInternal(e, errorMsg, headers, HttpStatus.BAD_REQUEST, request);
        }
        if (e instanceof NotFoundException) {
            return handleExceptionInternal(e, errorMsg, headers, HttpStatus.NOT_FOUND, request);
        }

        return handleExceptionInternal(e, errorMsg, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
