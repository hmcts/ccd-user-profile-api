package uk.gov.hmcts.ccd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserProfileApplication {
    protected UserProfileApplication() {}

    public static void main(String[] args) {
        SpringApplication.run(UserProfileApplication.class, args);
    }
}
