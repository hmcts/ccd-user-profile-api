package uk.gov.hmcts.ccd.userprofile.tests;

import uk.gov.hmcts.ccd.userprofile.tests.helper.S2SHelper;

public enum AATHelper {

    INSTANCE;

    private final S2SHelper s2SHelper;

    AATHelper() {
        s2SHelper = new S2SHelper(getS2SUrl(), getDataStoreServiceSecret(), getDataStoreServiceName());
    }

    public String getTestUrl() {
        return System.getenv("TEST_URL");
    }

    public String getS2SUrl() {
        return System.getenv("S2S_URL");
    }

    public String getDataStoreServiceName() {
        return System.getenv("BEFTA_S2S_CLIENT_ID");
    }

    public String getDataStoreServiceSecret() {
        return System.getenv("BEFTA_S2S_CLIENT_SECRET");
    }

    public S2SHelper getS2SHelper() {
        return s2SHelper;
    }
}
