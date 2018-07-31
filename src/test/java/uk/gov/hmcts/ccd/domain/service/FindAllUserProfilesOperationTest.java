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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindAllUserProfilesOperationTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    private FindAllUserProfilesOperation classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new FindAllUserProfilesOperation(userProfileRepository);
    }

    @Nested
    class FindAllUserProfilesTests {

        @Test
        @DisplayName("Should map each UserProfileEntity to a UserProfile and return a list of mapped UserProfiles, "
            + "for the specified Jurisdiction")
        void shouldReturnAllUserProfilesForJurisdiction() {
            final UserProfile userProfile1a = createUserProfile("test1a@example.com", "TEST", "CT1");
            final UserProfile userProfile1b = createUserProfile("test1b@example.com", "TEST", "CT2");
            final UserProfile userProfile2 = createUserProfile("test2@example.com", "TEST2", "CT2");

            when(userProfileRepository.findAll("TEST")).thenReturn(Arrays.asList(userProfile1a, userProfile1b));

            List<UserProfile> userProfiles = classUnderTest.getAll("TEST");
            assertEquals(2, userProfiles.size());
            assertEquals("test1a@example.com", userProfiles.get(0).getId());
            assertEquals("TEST", userProfiles.get(0).getWorkBasketDefaultJurisdiction());
            assertEquals("CT1", userProfiles.get(0).getWorkBasketDefaultCaseType());
            assertEquals("test1b@example.com", userProfiles.get(1).getId());
            assertEquals("TEST", userProfiles.get(1).getWorkBasketDefaultJurisdiction());
            assertEquals("CT2", userProfiles.get(1).getWorkBasketDefaultCaseType());
            assertThat(userProfiles, not(hasItem(userProfile2)));
        }

        @Test
        @DisplayName("Should map each UserProfileEntity to a UserProfile and return a list of mapped UserProfiles")
        void shouldReturnAllUserProfiles() {
            final UserProfile userProfile1a = createUserProfile("test1a@example.com", "TEST", "CT1");
            final UserProfile userProfile1b = createUserProfile("test1b@example.com", "TEST", "CT2");
            final UserProfile userProfile2 = createUserProfile("test2@example.com", "TEST2", "CT2");

            when(userProfileRepository.findAll()).thenReturn(Arrays.asList(userProfile1a, userProfile1b, userProfile2));

            List<UserProfile> userProfiles = classUnderTest.getAll();
            assertEquals(3, userProfiles.size());
            assertEquals("test1a@example.com", userProfiles.get(0).getId());
            assertEquals("TEST", userProfiles.get(0).getWorkBasketDefaultJurisdiction());
            assertEquals("CT1", userProfiles.get(0).getWorkBasketDefaultCaseType());
            assertEquals("test2@example.com", userProfiles.get(2).getId());
            assertEquals("TEST2", userProfiles.get(2).getWorkBasketDefaultJurisdiction());
            assertEquals("CT2", userProfiles.get(2).getWorkBasketDefaultCaseType());
        }

        @Test
        @DisplayName("Should return an empty list when the repository returns no UserProfiles")
        void shouldReturnEmptyUserProfileList() {
            when(userProfileRepository.findAll("TEST3")).thenReturn(Collections.emptyList());

            List<UserProfile> userProfiles = classUnderTest.getAll("TEST3");
            assertEquals(0, userProfiles.size());
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
