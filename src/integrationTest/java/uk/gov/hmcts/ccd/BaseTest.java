package uk.gov.hmcts.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileEntity;

import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;


@Testcontainers
@ActiveProfiles("it")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {

    protected static final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
                                                                 MediaType.APPLICATION_JSON.getSubtype(),
                                                                 Charset.forName("utf8"));
    protected static final ObjectMapper mapper = new ObjectMapper();

    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:11.1");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @BeforeAll
    public static void beforeAll() {
        postgresqlContainer.start();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected UserProfileEntity mapUserProfileData(final ResultSet resultSet,
                                                   final Integer i) throws SQLException {

        final UserProfileEntity entity = new UserProfileEntity();
        entity.setWorkBasketDefaultCaseType(resultSet.getString("work_basket_default_case_type"));
        entity.setWorkBasketDefaultJurisdiction(resultSet.getString("work_basket_default_jurisdiction"));
        entity.setWorkBasketDefaultState(resultSet.getString("work_basket_default_state"));
        entity.setId(resultSet.getString("id"));
        return entity;
    }
}
