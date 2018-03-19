package uk.gov.hmcts.ccd.userprofile.tests.functional;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.userprofile.tests.AATHelper;
import uk.gov.hmcts.ccd.userprofile.tests.BaseTest;
import java.util.function.Supplier;

class UserProfileTest extends BaseTest {

    protected UserProfileTest(AATHelper aat) {
        super(aat);
    }

    @Test
    @DisplayName("Should not create a user profile without a body")
    public void shouldNotCreateUserProfileWithoutBody() {

        Supplier<RequestSpecification> asUser = asAutoTestCaseworker();

        asUser.get()
            .given()
            .contentType(ContentType.JSON)
            .when()
            .post("/user-profile/users")
            .then()
            .statusCode(400);
    }

}
