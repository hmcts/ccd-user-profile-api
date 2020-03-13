package uk.gov.hmcts.ccd.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileLight;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindAllUserProfilesOperationTest {

    private static final String ACTIONED_BY_EMAIL = "pf4sd59ykc@example.com";

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

            when(userProfileRepository.findAll("TEST", ACTIONED_BY_EMAIL)).thenReturn(asList(userProfile1a,
                                                                                             userProfile1b));

            List<UserProfile> userProfiles = classUnderTest.getAll("TEST", ACTIONED_BY_EMAIL);
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

            when(userProfileRepository.findAll()).thenReturn(asList(userProfile1a, userProfile1b, userProfile2));

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
            when(userProfileRepository.findAll("TEST3", ACTIONED_BY_EMAIL)).thenReturn(emptyList());

            List<UserProfile> userProfiles = classUnderTest.getAll("TEST3", ACTIONED_BY_EMAIL);
            assertEquals(0, userProfiles.size());
        }

        @Test
        @DisplayName("getAllLight should map each UserProfileEntity to a UserProfile and return a list of mapped"
            + " UserProfiles, for the specified Jurisdiction")
        void getAllLightShouldReturnAllUserProfilesForJurisdiction() {
            final UserProfileLight userProfile1a = createUserProfileLight("test1a@example.com", "TEST",
                "CT1", "caseCreated");
            final UserProfileLight userProfile1b = createUserProfileLight("test1b@example.com", "TEST",
                "CT2", "caseCreated");
            final UserProfileLight userProfile2 = createUserProfileLight("test2@example.com", "TEST2",
                "CT2", "caseCreated");

            when(userProfileRepository.findAllLight("TEST", ACTIONED_BY_EMAIL))
                .thenReturn(asList(userProfile1a, userProfile1b));

            List<UserProfileLight> userProfiles = classUnderTest.getAllLight("TEST", ACTIONED_BY_EMAIL);
            assertEquals(2, userProfiles.size());
            assertEquals("test1a@example.com", userProfiles.get(0).getId());
            assertEquals("TEST", userProfiles.get(0).getDefaultJurisdiction());
            assertEquals("CT1", userProfiles.get(0).getDefaultCaseType());
            assertEquals("caseCreated", userProfiles.get(0).getDefaultState());

            assertEquals("test1b@example.com", userProfiles.get(1).getId());
            assertEquals("TEST", userProfiles.get(1).getDefaultJurisdiction());
            assertEquals("CT2", userProfiles.get(1).getDefaultCaseType());
            assertEquals("caseCreated", userProfiles.get(1).getDefaultState());

            assertThat(userProfiles, not(hasItem(userProfile2)));
        }

        @Test
        @DisplayName("getAllLight should map each UserProfileEntity to a UserProfile and return a list of mapped"
            + " UserProfiles")
        void getAllLightShouldReturnAllUserProfiles() {
            final UserProfileLight userProfile1a = createUserProfileLight("test1a@example.com", "TEST",
                "CT1", "caseCreated");
            final UserProfileLight userProfile1b = createUserProfileLight("test1b@example.com", "TEST",
                "CT2", "caseCreated");
            final UserProfileLight userProfile2 = createUserProfileLight("test2@example.com", "TEST2",
                "CT2", "caseCreated");

            when(userProfileRepository.findAllLight()).thenReturn(asList(userProfile1a, userProfile1b, userProfile2));

            List<UserProfileLight> userProfiles = classUnderTest.getAllLight();
            assertEquals(3, userProfiles.size());
            assertEquals("test1a@example.com", userProfiles.get(0).getId());
            assertEquals("TEST", userProfiles.get(0).getDefaultJurisdiction());
            assertEquals("CT1", userProfiles.get(0).getDefaultCaseType());
            assertEquals("caseCreated", userProfiles.get(0).getDefaultState());

            assertEquals("test1b@example.com", userProfiles.get(1).getId());
            assertEquals("TEST", userProfiles.get(1).getDefaultJurisdiction());
            assertEquals("CT2", userProfiles.get(1).getDefaultCaseType());
            assertEquals("caseCreated", userProfiles.get(1).getDefaultState());

            assertEquals("test2@example.com", userProfiles.get(2).getId());
            assertEquals("TEST2", userProfiles.get(2).getDefaultJurisdiction());
            assertEquals("CT2", userProfiles.get(2).getDefaultCaseType());
            assertEquals("caseCreated", userProfiles.get(2).getDefaultState());
        }

        @Test
        @DisplayName("getAllLight should return an empty list when the repository returns no UserProfiles")
        void getAllLightShouldReturnEmptyUserProfileList() {
            when(userProfileRepository.findAllLight("TEST3", ACTIONED_BY_EMAIL)).thenReturn(emptyList());

            List<UserProfileLight> userProfiles = classUnderTest.getAllLight("TEST3", ACTIONED_BY_EMAIL);
            assertEquals(0, userProfiles.size());
        }

        private UserProfile createUserProfile(String id, String jurisdiction, String caseType) {
            UserProfile userProfile = new UserProfile();
            userProfile.setId(id);
            userProfile.setWorkBasketDefaultJurisdiction(jurisdiction);
            userProfile.setWorkBasketDefaultCaseType(caseType);
            return userProfile;
        }

        private UserProfileLight createUserProfileLight(String id, String jurisdiction, String caseType, String state) {
            UserProfileLight userProfile = new UserProfileLight();
            userProfile.setId(id);
            userProfile.setDefaultJurisdiction(jurisdiction);
            userProfile.setDefaultCaseType(caseType);
            userProfile.setDefaultState(state);
            return userProfile;
        }
    }
}
