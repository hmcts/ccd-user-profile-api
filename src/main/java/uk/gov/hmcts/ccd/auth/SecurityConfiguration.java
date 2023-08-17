package uk.gov.hmcts.ccd.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.spring.AuthCheckerConfiguration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@Configuration
@Import(AuthCheckerConfiguration.class)
public class SecurityConfiguration {

    @Autowired
    private AuthCheckerFilter filter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .addFilter(this.filter)
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable) //NOSONAR only being flagged because of format change
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-resources/**",
                    "/swagger-ui/**",
                    "/webjars/**",
                    "/v3/**",
                    "/health",
                    "/health/readiness",
                    "/health/liveness",
                    "/status/health",
                    "/loggers/**",
                    "/error").permitAll()
                .anyRequest().authenticated())
        ;
        return http.build();
    }

}
