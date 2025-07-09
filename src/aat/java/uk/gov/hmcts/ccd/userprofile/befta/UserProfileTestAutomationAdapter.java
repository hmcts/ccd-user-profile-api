package uk.gov.hmcts.ccd.userprofile.befta;

import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.ReflectionUtils;

public class UserProfileTestAutomationAdapter extends DefaultTestAutomationAdapter {

    @Override
    public synchronized Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        if (key.toString().startsWith("no_dynamic_injection_")) {
            return key.toString().replace("no_dynamic_injection_","");
        } else if (key.toString().startsWith("approximately ")) {
            try {
                String actualSizeFromHeaderStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.headers.Content-Length");
                String expectedSizeStr = key.toString().replace("approximately ", "");

                int actualSize =  Integer.parseInt(actualSizeFromHeaderStr);
                int expectedSize = Integer.parseInt(expectedSizeStr);

                if (Math.abs(actualSize - expectedSize) < (actualSize * 10 / 100)) {
                    return actualSizeFromHeaderStr;
                }
                return expectedSize;
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        } else if (key.toString().startsWith("contains ")) {
            try {
                String actualValueStr = (String) ReflectionUtils.deepGetFieldInObject(scenarioContext,
                    "testData.actualResponse.body.__plainTextValue__");
                String expectedValueStr = key.toString().replace("contains ", "");

                if (actualValueStr.contains(expectedValueStr)) {
                    return actualValueStr;
                }
                return "expectedValueStr " + expectedValueStr + " not present in response ";
            } catch (Exception e) {
                throw new FunctionalTestException("Problem checking acceptable response payload: ", e);
            }
        }
        return super.calculateCustomValue(scenarioContext, key);
    }
}
