package uk.gov.hmcts.ccd.userprofile.tests;

import uk.gov.hmcts.ccd.userprofile.tests.helper.S2SHelper;
import uk.gov.hmcts.ccd.userprofile.tests.helper.idam.IdamHelper;

public enum AATHelper {

    INSTANCE;

    private final IdamHelper idamHelper;
    private final S2SHelper s2SHelper;

    AATHelper() {
        idamHelper = new IdamHelper(getIdamURL());
        s2SHelper = new S2SHelper(getS2SUrl());
    }

    public String getTestUrl() {
        return System.getenv("TEST_URL");
    }

    public String getIdamURL() {
        return System.getenv("IDAM_URL");
    }

    public String getS2SUrl() {
        return System.getenv("S2S_URL");
    }

    public String getCaseworkerAutoTestEmail() {
        return System.getenv("CCD_CASEWORKER_AUTOTEST_EMAIL");
    }

    public String getCaseworkerAutoTestPassword() {
        return System.getenv("CCD_CASEWORKER_AUTOTEST_PASSWORD");
    }

    public String getDataStoreServiceName() {
        return System.getenv("CCD_DS_SERVICE_NAME");
    }

    public String getDataStoreServiceSecret() {
        return System.getenv("CCD_DS_SERVICE_SECRET");
    }

    public IdamHelper getIdamHelper() {
        return idamHelper;
    }

    public S2SHelper getS2SHelper() {
        return s2SHelper;
    }
}
