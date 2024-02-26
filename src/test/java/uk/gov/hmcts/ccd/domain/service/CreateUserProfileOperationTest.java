package uk.gov.hmcts.ccd.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CreateUserProfileOperationTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private CreateUserProfileOperation testClass;

    private UserProfile userProfile;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        testClass = new CreateUserProfileOperation(userProfileRepository);
        userProfile = new UserProfile();
    }

    @Test
    public void test_noID() {
        Exception thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            testClass.execute(userProfile, "");
        });

        assertEquals("A User Profile must have an Id", thrown.getMessage());
    }

    @Test
    public void test_noJurisdiction() {
        userProfile.setId("TEST");
        Exception thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            testClass.execute(userProfile, "");
        });

        assertEquals("A User Profile must have at least one associated Jurisdiction", thrown.getMessage());
    }

    @Test
    public void test_badJurisdiction() {
        userProfile.setId("TEST");
        userProfile.setJurisdictions(Arrays.asList(new Jurisdiction()));
        Exception thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            testClass.execute(userProfile, "");
        });

        assertEquals("A Jurisdiction must have an Id", thrown.getMessage());
    }

    @Test
    public void test_success() {
        userProfile.setId("TEST");
        userProfile.setJurisdictions(Arrays.asList(new Jurisdiction("TEST")));

        testClass.execute(userProfile, "");

        verify(userProfileRepository, times(1)).createUserProfile(userProfile, "");
    }
}
