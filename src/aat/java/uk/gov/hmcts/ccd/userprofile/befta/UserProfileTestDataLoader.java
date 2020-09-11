package uk.gov.hmcts.ccd.userprofile.befta;

import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;

public final class UserProfileTestDataLoader {
    private UserProfileTestDataLoader(){ }

    public static void main(String[] args) {
        new DefaultTestAutomationAdapter().loadTestDataIfNecessary();
    }
}
