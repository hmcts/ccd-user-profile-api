package uk.gov.hmcts.ccd.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenInvalidException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenMissingException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedServiceException;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenInvalidException;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParsingException;
import uk.gov.hmcts.reform.auth.parser.idam.spring.service.token.ServiceTokenParserConfiguration;

import java.util.Collection;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Import({ServiceTokenParserConfiguration.class})
@Component
public class AuthCheckerFilter extends AbstractPreAuthenticatedProcessingFilter {

    public static final String AUTHORISATION = "ServiceAuthorization";

    @Autowired
    private ServiceTokenParser serviceTokenParser;

    @Autowired
    private Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor;

    public AuthCheckerFilter(AuthenticationManager authenticationManager) {
        this.setAuthenticationManager(authenticationManager);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return authorizeService(request);
    }

    private Service authorizeService(HttpServletRequest request) {
        try {
            return authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful service authentication", e);
            return null;
        }
    }

    private Service authorise(HttpServletRequest request) throws UnauthorisedServiceException {
        Collection<String> authorizedServices = authorizedServicesExtractor
                    .apply(request)
                    .stream()
                    .map(String::toLowerCase)
                    .collect(toSet());
        if (authorizedServices.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one service defined");
        }

        String bearerToken = request.getHeader(AUTHORISATION);
        if (bearerToken == null) {
            throw new BearerTokenMissingException();
        }

        Service service = getTokenDetails(bearerToken);
        if (!authorizedServices.contains(service.getPrincipal().toLowerCase())) {
            throw new UnauthorisedServiceException();
        }

        return service;
    }

    private Service getTokenDetails(String bearerToken) {
        try {
            String subject = serviceTokenParser.parse(bearerToken);
            return new Service(subject);
        } catch (ServiceTokenInvalidException e) {
            throw new BearerTokenInvalidException(e);
        } catch (ServiceTokenParsingException e) {
            throw new AuthCheckerException("Error parsing JWT token", e);
        }
    }
}
