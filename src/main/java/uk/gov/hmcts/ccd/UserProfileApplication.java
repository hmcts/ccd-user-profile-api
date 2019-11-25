package uk.gov.hmcts.ccd;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings({"PMD.UseUtilityClass", "HideUtilityClassConstructor"})
public class UserProfileApplication {

    public static void main(String[] args) {

        //Setting Liquibase DB Lock property before Spring starts up.
        LiquibaseConfiguration.getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .setUseDbLock(true);
        SpringApplication.run(UserProfileApplication.class, args);
    }
}
