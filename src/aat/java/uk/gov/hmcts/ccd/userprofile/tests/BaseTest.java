package uk.gov.hmcts.ccd.userprofile.tests;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Supplier;

@ExtendWith(AATExtension.class)
public abstract class BaseTest {
    protected final AATHelper aat;

    protected BaseTest(AATHelper aat) {
        this.aat = aat;
        RestAssured.useRelaxedHTTPSValidation();
    }

    protected Supplier<RequestSpecification> asDataStoreService() {
        final String s2sToken = aat.getS2SHelper()
                                   .getToken();

        return () -> RestAssured.given()
                          .header("ServiceAuthorization", s2sToken);
    }
}
