package uk.gov.hmcts.ccd.auth;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientAuthorsationConfigurationTest {

    @Test
    public void checkService() {

        AuthorizedConfiguration services = mock(AuthorizedConfiguration.class);
        List<String> allowableServices = new ArrayList<String>();
        allowableServices.add("reference");
        allowableServices.add("reference-2");
        when(services.getServices()).thenReturn(allowableServices);
        AuthCheckerConfiguration subject = new AuthCheckerConfiguration(services);

        // this should return allowable services
        Collection<String> result = subject.authorizedServicesExtractor().apply(null);
        assertTrue(result.containsAll(allowableServices));
        assertTrue(allowableServices.containsAll(result));
    }

}
