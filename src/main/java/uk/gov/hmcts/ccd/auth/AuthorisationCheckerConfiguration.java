package uk.gov.hmcts.ccd.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.function.Function;

@Configuration
public class AuthorisationCheckerConfiguration {

    private AuthorizedConfiguration services;

    public AuthorisationCheckerConfiguration(AuthorizedConfiguration services) {
        this.services = services;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return request -> services.getServices();
    }

}
