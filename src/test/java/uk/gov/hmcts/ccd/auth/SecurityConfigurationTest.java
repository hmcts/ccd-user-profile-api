package uk.gov.hmcts.ccd.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigurationTest.TestEndpoints.class)
@Import({SecurityConfiguration.class, SecurityConfigurationTest.SecurityTestConfig.class})
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAnonymousStatusHealthEndpoint() throws Exception {
        mockMvc.perform(get("/status/health"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAnonymousLoggerEndpoint() throws Exception {
        mockMvc.perform(get("/loggers"))
            .andExpect(status().is(HttpServletResponse.SC_FORBIDDEN));
    }

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        AuthCheckerFilter authCheckerFilter() {
            ServiceTokenParser serviceTokenParser = mock(ServiceTokenParser.class);
            AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
            AuthCheckerFilter filter = new AuthCheckerFilter(serviceTokenParser, authenticationManager);
            ReflectionTestUtils.setField(filter, "authorisedSServices", List.of("ccd_data"));
            return filter;
        }
    }

    @RestController
    public static class TestEndpoints {

        @GetMapping("/status/health")
        public ResponseEntity<Void> statusHealth() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/loggers")
        public ResponseEntity<Void> loggers() {
            return ResponseEntity.ok().build();
        }
    }
}
