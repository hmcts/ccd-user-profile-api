package uk.gov.hmcts.ccd.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Function;

@Configuration
public class AuthCheckerConfiguration {

    private AuthorizedConfiguration services;

    public AuthCheckerConfiguration(AuthorizedConfiguration services) {
        this.services = services;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return request -> services.getServices();
    }
}
