package uk.gov.hmcts.ccd.userprofile.befta;

public class UserProfileTestDataLoader {
    public UserProfileTestDataLoader() {
        new UserProfileTestAutomationAdapter().loadTestDataIfNecessary();
    }
}
