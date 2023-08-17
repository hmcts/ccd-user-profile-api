package uk.gov.hmcts.ccd;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf,
                                                         @Value("${ccd.tx-timeout.default}") String defaultTimeout) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.setDefaultTimeout(Integer.parseInt(defaultTimeout));
        return transactionManager;
    }

    @Bean
    public FlywayConfigurationCustomizer flywayCustomizer() {
        return configuration -> configuration.configuration(
            Map.of("flyway.postgresql.transactional.lock", "false")
        );
    }

}
