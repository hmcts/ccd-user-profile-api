package uk.gov.hmcts.ccd.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindAllUserProfilesOperationTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private FindAllUserProfilesOperation classUnderTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new FindAllUserProfilesOperation(userProfileRepository);
    }

    @Nested
    public class FindAllUserProfilesTests {

        @Test
        @DisplayName("Should map each UserProfileEntity to a UserProfile and return a list of mapped UserProfiles")
        public void shouldReturnAllUserProfiles() {
            UserProfile userProfile1 = createUserProfile("test1@example.com", "TEST", "CT1");
            UserProfile userProfile2 = createUserProfile("test2@example.com", "TEST2", "CT2");

            when(userProfileRepository.findAll()).thenReturn(Arrays.asList(userProfile1, userProfile2));

            List<UserProfile> userProfiles = classUnderTest.execute();
            assertEquals(2, userProfiles.size());
            assertEquals("test1@example.com", userProfiles.get(0).getId());
            assertEquals("TEST", userProfiles.get(0).getWorkBasketDefaultJurisdiction());
            assertEquals("CT1", userProfiles.get(0).getWorkBasketDefaultCaseType());
            assertEquals("test2@example.com", userProfiles.get(1).getId());
            assertEquals("TEST2", userProfiles.get(1).getWorkBasketDefaultJurisdiction());
            assertEquals("CT2", userProfiles.get(1).getWorkBasketDefaultCaseType());
        }

        private UserProfile createUserProfile(String id, String jurisdiction, String caseType) {
            UserProfile userProfile = new UserProfile();
            userProfile.setId(id);
            userProfile.setWorkBasketDefaultJurisdiction(jurisdiction);
            userProfile.setWorkBasketDefaultCaseType(caseType);
            return userProfile;
        }
    }
}
