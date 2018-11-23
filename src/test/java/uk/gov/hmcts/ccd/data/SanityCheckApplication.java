package uk.gov.hmcts.ccd.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.inject.Inject;
import javax.sql.DataSource;

@SpringBootApplication
@ComponentScan("uk.gov.hmcts.ccd.data")
public class SanityCheckApplication {

    @Inject
    protected DataSource db;

    public static void main(String[] args) {
        SpringApplication.run(SanityCheckApplication.class, args);
    }
}
