package uk.gov.hmcts.ccd.userprofile.befta;

import io.restassured.RestAssured;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;

public class UserProfileTestAutomationAdapter extends DefaultTestAutomationAdapter {

    @Override
    public void doLoadTestData() {
        RestAssured.useRelaxedHTTPSValidation();
    }

}
