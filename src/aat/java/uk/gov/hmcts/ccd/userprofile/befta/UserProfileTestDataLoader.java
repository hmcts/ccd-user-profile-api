package uk.gov.hmcts.ccd.userprofile.befta;

public final class UserProfileTestDataLoader {
    private UserProfileTestDataLoader(){ }

    public static void main(String[] args) {
        new UserProfileTestAutomationAdapter().loadTestDataIfNecessary();
    }
}
