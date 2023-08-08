package uk.gov.hmcts.ccd.hikari;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.stereotype.Component;

// needed to fix  "error" : "Cannot serialize 'spring.datasource.hikari'" in actuator /configprops endpoint
// for and be able to see configuration properties set for Hikari connection pool
@Component
public class HikariConfigurationPropertiesReportEndpoint extends ConfigurationPropertiesReportEndpoint {

    public HikariConfigurationPropertiesReportEndpoint(Iterable<SanitizingFunction> sanitizingFunctions,
                                                       Show showValues) {
        super(sanitizingFunctions, showValues);
    }

    @Override
    protected void configureJsonMapper(JsonMapper.Builder mapper) {
        super.configureJsonMapper(mapper);
        mapper.addMixIn(HikariDataSource.class, HikariDataSourceMixIn.class);
    }
}
