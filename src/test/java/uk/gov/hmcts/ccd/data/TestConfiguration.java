package uk.gov.hmcts.ccd.data;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class TestConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TestConfiguration.class);

    @Bean(name = "EmbeddedPostgres")
    public DataSource dataSource() throws IOException {
        int port = randomPort();
        LOG.info("Starting Postgres on port number {}", port);
        final EmbeddedPostgres pg = EmbeddedPostgres
            .builder()
            .start();

        return pg.getPostgresDatabase();
    }

    private int randomPort() {
        return ThreadLocalRandom.current().nextInt(50366, 60366);
    }
}
