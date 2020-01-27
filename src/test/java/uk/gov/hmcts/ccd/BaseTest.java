package uk.gov.hmcts.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileEntity;

import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public abstract class BaseTest {

    protected static final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
                                                                 MediaType.APPLICATION_JSON.getSubtype(),
                                                                 Charset.forName("utf8"));
    protected static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    @Qualifier("EmbeddedPostgres")
    protected DataSource db;

    @BeforeClass
    public static void init() {
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
