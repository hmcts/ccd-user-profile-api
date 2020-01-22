package uk.gov.hmcts.ccd.userprofile.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class UserProfileBeftaMain extends BeftaMain {

    public static void main(String[] args) {
        setTaAdapter(new UserProfileTestAutomationAdapter());
        BeftaMain.main(args);
    }

}
