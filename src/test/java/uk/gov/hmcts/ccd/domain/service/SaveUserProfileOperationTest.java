package uk.gov.hmcts.ccd.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionRepository;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveUserProfileOperationTest {

    private static final String ACTIONED_BY = "nush5mfvzc@example.com";

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JurisdictionRepository jurisdictionRepository;

    @Mock
    private CreateUserProfileOperation createUserProfileOperation;

    private SaveUserProfileOperation classUnderTest;

    @Captor
    private ArgumentCaptor<JurisdictionEntity> jurisdictionEntityArgCaptor;

    @Captor
    private ArgumentCaptor<UserProfile> userProfileArgCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new SaveUserProfileOperation(
            userProfileRepository,
            jurisdictionRepository,
            createUserProfileOperation);
    }

    @Nested
    class SaveUserProfileTests {

        @Test
        @DisplayName("Should create the Jurisdiction if the user is being added to one that doesn't exist")
        void shouldCreateJurisdictionForUserIfJurisdictionNotPresent() {
            UserProfile userProfile = createUserProfile("user@hmcts", "TEST");

            when(jurisdictionRepository.findEntityById(userProfile.getWorkBasketDefaultJurisdiction()))
                .thenReturn(null);

            classUnderTest.saveUserProfile(userProfile, ACTIONED_BY);
            verify(jurisdictionRepository).create(jurisdictionEntityArgCaptor.capture());
            assertEquals("TEST", jurisdictionEntityArgCaptor.getValue().getId());
        }

        @Test
        @DisplayName("Should create a User Profile if the user doesn't exist")
        void shouldCreateUserProfileIfUserDoesNotExist() {
            UserProfile userProfile = createUserProfile("user@hmcts", "TEST");

            JurisdictionEntity jurisdiction = new JurisdictionEntity();
            jurisdiction.setId(userProfile.getWorkBasketDefaultJurisdiction());
            when(jurisdictionRepository.findEntityById(userProfile.getWorkBasketDefaultJurisdiction()))
                .thenReturn(jurisdiction);
            when(userProfileRepository.findById(userProfile.getId(), ACTIONED_BY)).thenReturn(null);

            classUnderTest.saveUserProfile(userProfile, ACTIONED_BY);
            verify(createUserProfileOperation).execute(userProfileArgCaptor.capture(), eq(ACTIONED_BY));
            assertEquals("user@hmcts", userProfileArgCaptor.getValue().getId());
            assertEquals("TEST", userProfileArgCaptor.getValue().getWorkBasketDefaultJurisdiction());
            assertEquals(1, userProfileArgCaptor.getValue().getJurisdictions().size());
            assertEquals("TEST", userProfileArgCaptor.getValue().getJurisdictions().get(0).getId());
        }

        @Test
        @DisplayName("Should append Jurisdiction to the user's list if not already a member")
        void shouldAddJurisdictionIfUserIsNotAMember() {
            UserProfile userProfile = createUserProfile("user@hmcts", "TEST");

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setId(userProfile.getWorkBasketDefaultJurisdiction());
            when(jurisdictionRepository.findEntityById(userProfile.getWorkBasketDefaultJurisdiction()))
                .thenReturn(jurisdictionEntity);
            when(userProfileRepository.findById(userProfile.getId(), ACTIONED_BY)).thenReturn(userProfile);

            classUnderTest.saveUserProfile(userProfile, ACTIONED_BY);
            verify(userProfileRepository).updateUserProfileOnCreate(userProfileArgCaptor.capture(), eq(ACTIONED_BY));
            assertEquals("user@hmcts", userProfileArgCaptor.getValue().getId());
            assertEquals("TEST", userProfileArgCaptor.getValue().getWorkBasketDefaultJurisdiction());
        }

        @Test
        @DisplayName("Should throw an exception if the user is already a member of given Jurisdiction")
        void shouldThrowExceptionIfUserAlreadyHasJurisdiction() {
            UserProfile userProfile = createUserProfile("user@hmcts", "TEST");

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setId(userProfile.getWorkBasketDefaultJurisdiction());
            when(jurisdictionRepository.findEntityById(userProfile.getWorkBasketDefaultJurisdiction()))
                .thenReturn(jurisdictionEntity);
            when(userProfileRepository.findById(userProfile.getId(), ACTIONED_BY)).thenReturn(userProfile);

            when(userProfileRepository.updateUserProfileOnCreate(userProfile, ACTIONED_BY))
                .thenThrow(new BadRequestException("User is already a member of "
                    + "the " + userProfile.getWorkBasketDefaultJurisdiction() + " jurisdiction"));

            BadRequestException exception =
                assertThrows(BadRequestException.class, () -> classUnderTest.saveUserProfile(userProfile, ACTIONED_BY));
            assertEquals("User is already a member of the "
                + userProfile.getWorkBasketDefaultJurisdiction() + " jurisdiction", exception.getMessage());
        }

        private UserProfile createUserProfile(String id, String jurisdictionId) {
            UserProfile userProfile = new UserProfile();
            userProfile.setId(id);
            userProfile.setWorkBasketDefaultJurisdiction(jurisdictionId);
            Jurisdiction jurisdiction = new Jurisdiction();
            jurisdiction.setId(jurisdictionId);
            userProfile.addJurisdiction(jurisdiction);
            return userProfile;
        }
    }
}
