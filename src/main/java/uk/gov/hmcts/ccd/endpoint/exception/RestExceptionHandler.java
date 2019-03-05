package uk.gov.hmcts.ccd.endpoint.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.ccd.AppInsights;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final AppInsights appInsights;

    @Autowired
    public RestExceptionHandler(AppInsights appInsights) {
        this.appInsights = appInsights;
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(final RuntimeException e, final WebRequest request) {
        final StringBuilder errorMsg = new StringBuilder(e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Message", e.getMessage());
        appInsights.trackException(e);
        if (e instanceof BadRequestException) {
            LOG.warn("exception with message {}, {}, {}", errorMsg, HttpStatus.BAD_REQUEST, request, e);
            return handleExceptionInternal(e, errorMsg, headers, HttpStatus.BAD_REQUEST, request);
        }
        if (e instanceof NotFoundException) {
            LOG.warn("exception with message {}, {}, {}", errorMsg, HttpStatus.NOT_FOUND, request, e);
            return handleExceptionInternal(e, errorMsg, headers, HttpStatus.NOT_FOUND, request);
        }
        LOG.warn("exception with message {}, {}, {}", errorMsg, HttpStatus.INTERNAL_SERVER_ERROR, request, e);
        return handleExceptionInternal(e, errorMsg, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
