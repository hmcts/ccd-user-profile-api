package uk.gov.hmcts.ccd.userprofile.tests.functional;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.userprofile.tests.AATHelper;
import uk.gov.hmcts.ccd.userprofile.tests.BaseTest;

@DisplayName("User profile: Functional tests")
class UserProfileTest extends BaseTest {

    protected UserProfileTest(AATHelper aat) {
        super(aat);
    }

    @Test
    @DisplayName("Should not create a user profile without a body")
    void shouldNotCreateUserProfileWithoutBody() {

        asDataStoreService().get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .post(aat.getTestUrl() + "/user-profile/users")
            .then()
            .statusCode(400);
    }

}
