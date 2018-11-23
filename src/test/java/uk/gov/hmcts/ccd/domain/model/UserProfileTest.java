package uk.gov.hmcts.ccd.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserProfileTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void checkJson() throws JsonProcessingException {
        final UserProfile userProfile = new UserProfile();
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("j");

        userProfile.addJurisdiction(jurisdiction);
        userProfile.setId("ngitb@mailg.com");
        userProfile.setWorkBasketDefaultCaseType("case");
        userProfile.setWorkBasketDefaultJurisdiction("j");
        userProfile.setWorkBasketDefaultState("s");

        final String result = MAPPER.writeValueAsString(userProfile);
        final String expected = "{\"id\":\"ngitb@mailg.com\","
            + "\"jurisdictions\":[{\"id\":\"j\"}],"
            + "\"work_basket_default_jurisdiction\":\"j\","
            + "\"work_basket_default_case_type\":\"case\","
            + "\"work_basket_default_state\":\"s\"}";
        assertEquals(expected, result);
    }
}
