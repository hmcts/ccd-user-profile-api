package uk.gov.hmcts.ccd.userprofile.befta;

import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;

public final class UserProfileTestDataLoader extends DefaultBeftaTestDataLoader {

    private TestDataLoaderToDefinitionStore loader =
        new TestDataLoaderToDefinitionStore(new UserProfileTestAutomationAdapter());

    private UserProfileTestDataLoader() {
    }

    public static void main(String[] args) {
        new DefaultBeftaTestDataLoader().loadTestDataIfNecessary();
    }

    @Override
    public void doLoadTestData() {
        loader.addCcdRoles();
        loader.importDefinitions();
    }
}
