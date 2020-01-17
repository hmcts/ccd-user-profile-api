package uk.gov.hmcts.ccd.userprofile.tests;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Supplier;

@ExtendWith(AATExtension.class)
public abstract class BaseTest {
    protected final transient AATHelper aat;

    protected BaseTest(AATHelper aat) {
        this.aat = aat;
        RestAssured.useRelaxedHTTPSValidation();
    }

    protected Supplier<RequestSpecification> asDataStoreService() {
        return () -> RestAssured.given()
                          .header("ServiceAuthorization", aat.getS2SHelper().getToken());
    }
}
