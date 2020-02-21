package uk.gov.hmcts.ccd.userprofile.befta;

import uk.gov.hmcts.befta.BeftaMain;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;

public class UserProfileBeftaMain extends BeftaMain {

    public static void main(String[] args) {
        setTaAdapter(new DefaultTestAutomationAdapter());
        BeftaMain.main(args);
    }

}
