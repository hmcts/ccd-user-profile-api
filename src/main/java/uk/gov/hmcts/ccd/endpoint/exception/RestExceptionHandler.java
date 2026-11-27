package uk.gov.hmcts.ccd.endpoint.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.ccd.error.ErrorCode;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(final RuntimeException e, final WebRequest request) {
        HttpStatus status = e instanceof BadRequestException ? HttpStatus.BAD_REQUEST
            : e instanceof NotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
        HttpHeaders headers = new HttpHeaders();
        if (status != HttpStatus.INTERNAL_SERVER_ERROR) {
            headers.add("Message", e.getMessage() == null ? status.getReasonPhrase() : e.getMessage());
        }
        return handleExceptionInternal(e, null, headers, status, request);
    }

    @ExceptionHandler(SQLException.class)
    protected ResponseEntity<Object> handleSQLException(final SQLException e, final WebRequest request) {
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
                                                              HttpHeaders headers, HttpStatusCode status,
                                                              WebRequest request) {
        try (var diagnostics = uk.gov.hmcts.ccd.error.FailureDiagnostics.open(exception, status.value())) {
            if (status.is5xxServerError()) {
                LOG.error("Request failed: code={} status={}",
                    ErrorCode.fromStatus(status.value()), status.value(), exception);
            } else {
                LOG.warn("Request failed: code={} status={}",
                    ErrorCode.fromStatus(status.value()), status.value(), exception);
            }
            // These application exceptions contain reviewed public business validation only.
            if ((exception instanceof BadRequestException || exception instanceof NotFoundException)
                && exception.getMessage() != null) {
                return super.handleExceptionInternal(exception, exception.getMessage(), headers, status, request);
            }
            String safeMessage = HttpStatus.valueOf(status.value()).getReasonPhrase();
            Object safeBody = exception instanceof SQLException ? Map.of("errorMessage", safeMessage) : safeMessage;
            if (exception instanceof org.springframework.web.ErrorResponse) {
                var problem = org.springframework.http.ProblemDetail.forStatusAndDetail(status, safeMessage);
                problem.setInstance(java.net.URI.create("/"));
                safeBody = problem;
            }
            return super.handleExceptionInternal(exception, safeBody, headers, status, request);
        }
    }
}
