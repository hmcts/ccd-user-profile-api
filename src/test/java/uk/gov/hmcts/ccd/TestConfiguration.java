package uk.gov.hmcts.ccd;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

@Configuration
public class TestConfiguration {

    @Bean(name = "EmbeddedPostgres")
    public DataSource dataSource() throws IOException, SQLException {
        final EmbeddedPostgres pg = EmbeddedPostgres
                .builder()
                .start();

        return pg.getPostgresDatabase();
    }
}
