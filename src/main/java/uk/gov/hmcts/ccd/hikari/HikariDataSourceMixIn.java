package uk.gov.hmcts.ccd.hikari;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zaxxer.hikari.HikariConfigMXBean;

import java.io.PrintWriter;

// needed to fix  "error" : "Cannot serialize 'spring.datasource.hikari'" in actuator /configprops endpoint
// for and be able to see configuration properties set for Hikari connection pool
public abstract class HikariDataSourceMixIn {

    @JsonIgnore
    abstract PrintWriter getLogWriter();

    @JsonIgnore
    abstract HikariConfigMXBean getHikariConfigMXBean();
}
