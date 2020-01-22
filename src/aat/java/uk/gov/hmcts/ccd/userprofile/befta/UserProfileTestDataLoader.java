package uk.gov.hmcts.ccd.userprofile.befta;

public class UserProfileTestDataLoader {
    public static void main(String[] args) {
        new UserProfileTestAutomationAdapter().loadTestDataIfNecessary();
    }
}
