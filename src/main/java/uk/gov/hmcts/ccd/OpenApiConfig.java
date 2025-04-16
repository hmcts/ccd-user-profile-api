package uk.gov.hmcts.ccd;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("User Profile API")
                .description("API to store and retrieve user profile data.")
                .version("v0.0.1")
            );
    }
}
