package uk.gov.hmcts.ccd.domain.service;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteUserProfileJurisdictionOperationTest {

    private static final String ACTIONED_BY = "tg2ln35vek@example.com";

    private static final String USER_ID = "user@hmcts";
    private static final String JURISDICTION_ID = "TEST";
    private static final String WORKBASKET_DEFAULTS_ERROR = "Cannot delete user profile as the user's workbasket "
        + "defaults are set to the Jurisdiction the user is being deleted from. Please update the user's workbasket "
        + "default values to another Jurisdiction and try again.";

    @Mock
    private UserProfileRepository userProfileRepository;

    private DeleteUserProfileJurisdictionOperation classUnderTest;

    @Captor
    private ArgumentCaptor<UserProfile> userProfileArgCaptor;

    @Captor
    private ArgumentCaptor<Jurisdiction> jurisdictionArgCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new DeleteUserProfileJurisdictionOperation(userProfileRepository);
    }

    @Nested
    class DeleteUserProfileJurisdictionTests {

        @Test
        @DisplayName("Should throw an exception if the user does not exist")
        void shouldThrowExceptionIfUserDoesNotExist() {
            when(userProfileRepository.findById(USER_ID, ACTIONED_BY)).thenReturn(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> classUnderTest.deleteAssociation(USER_ID, JURISDICTION_ID, ACTIONED_BY));
            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw an exception if the user is not a member of the specified Jurisdiction")
        void shouldThrowExceptionIfUserIsNotMemberOfJurisdiction() {
            final UserProfile userProfile = createUserProfile(USER_ID, JURISDICTION_ID);
            when(userProfileRepository.findById(USER_ID, ACTIONED_BY)).thenReturn(userProfile);

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> classUnderTest.deleteAssociation(USER_ID, "TEST2", ACTIONED_BY));
            assertEquals("User is not a member of the TEST2 jurisdiction",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw an exception if the Jurisdiction being removed matches the user's Workbasket "
            + "default Jurisdiction")
        void shouldThrowExceptionIfJurisdictionMatchesWorkbasketDefault() {
            final UserProfile userProfile = createUserProfile(USER_ID, JURISDICTION_ID, "TEST2");
            userProfile.setWorkBasketDefaultJurisdiction(JURISDICTION_ID);
            when(userProfileRepository.findById(USER_ID, ACTIONED_BY)).thenReturn(userProfile);

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> classUnderTest.deleteAssociation(USER_ID, JURISDICTION_ID, ACTIONED_BY));
            assertEquals(WORKBASKET_DEFAULTS_ERROR, exception.getMessage());
        }

        @Test
        @DisplayName("Should remove the specified Jurisdiction from the user's list of Jurisdictions, if it is NOT "
            + "their Workbasket default")
        void shouldRemoveAssociationToJurisdiction() {
            final UserProfile userProfile = createUserProfile(USER_ID, JURISDICTION_ID, "TEST2");
            userProfile.setWorkBasketDefaultJurisdiction(JURISDICTION_ID);
            when(userProfileRepository.findById(USER_ID, ACTIONED_BY)).thenReturn(userProfile);

            final UserProfile expectedUserProfile = createUserProfile(USER_ID, JURISDICTION_ID);
            expectedUserProfile.setWorkBasketDefaultJurisdiction(JURISDICTION_ID);
            when(userProfileRepository.deleteJurisdictionFromUserProfile(userProfile,
                userProfile.getJurisdictions().get(1), ACTIONED_BY)).thenReturn(expectedUserProfile);

            final UserProfile updatedUserProfile = classUnderTest.deleteAssociation(USER_ID, "TEST2", ACTIONED_BY);
            verify(userProfileRepository)
                .deleteJurisdictionFromUserProfile(userProfileArgCaptor.capture(),
                                                   jurisdictionArgCaptor.capture(),
                                                   eq(ACTIONED_BY));
            assertEquals(USER_ID, userProfileArgCaptor.getValue().getId());
            assertEquals("TEST2", jurisdictionArgCaptor.getValue().getId());
            assertEquals(1, updatedUserProfile.getJurisdictions().size());
            assertEquals("TEST", updatedUserProfile.getWorkBasketDefaultJurisdiction());
        }

        @Test
        @DisplayName("Should set all workbasket defaults to null if the user no longer belongs to any Jurisdictions")
        void shouldNullWorkbasketDefaultsForUserWithNoJurisdictions() {
            final UserProfile userProfile = createUserProfile(USER_ID, JURISDICTION_ID);
            userProfile.setWorkBasketDefaultJurisdiction(JURISDICTION_ID);
            userProfile.setWorkBasketDefaultCaseType("TestCaseType");
            userProfile.setWorkBasketDefaultState("State1");
            when(userProfileRepository.findById(USER_ID, ACTIONED_BY)).thenReturn(userProfile);

            final UserProfile expectedUserProfile = createUserProfile(USER_ID);
            expectedUserProfile.setJurisdictions(Collections.emptyList());
            when(userProfileRepository.deleteJurisdictionFromUserProfile(userProfile,
                userProfile.getJurisdictions().get(0), ACTIONED_BY)).thenReturn(expectedUserProfile);

            final UserProfile
                updatedUserProfile =
                classUnderTest.deleteAssociation(USER_ID, JURISDICTION_ID, ACTIONED_BY);
            verify(userProfileRepository)
                .deleteJurisdictionFromUserProfile(userProfileArgCaptor.capture(),
                                                   jurisdictionArgCaptor.capture(),
                                                   eq(ACTIONED_BY));
            assertEquals(USER_ID, userProfileArgCaptor.getValue().getId());
            assertEquals(JURISDICTION_ID, jurisdictionArgCaptor.getValue().getId());
            assertEquals(0, updatedUserProfile.getJurisdictions().size());
            assertNull(updatedUserProfile.getWorkBasketDefaultJurisdiction());
            assertNull(updatedUserProfile.getWorkBasketDefaultCaseType());
            assertNull(updatedUserProfile.getWorkBasketDefaultState());
        }

        @Test
        @DisplayName("Should throw an exception if the user does not belong to any Jurisdictions")
        void shouldThrowExceptionIfUserIsNotMemberOfAnyJurisdictions() {
            final UserProfile userProfile = new UserProfile();
            userProfile.setId(USER_ID);
            when(userProfileRepository.findById(USER_ID, ACTIONED_BY)).thenReturn(userProfile);

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> classUnderTest.deleteAssociation(USER_ID, JURISDICTION_ID, ACTIONED_BY));
            assertEquals("User is not a member of any Jurisdictions",
                exception.getMessage());
        }

        private UserProfile createUserProfile(String userId, String... jurisdictionIds) {
            UserProfile userProfile = new UserProfile();
            userProfile.setId(userId);
            for (String jid : jurisdictionIds) {
                Jurisdiction jurisdiction = new Jurisdiction();
                jurisdiction.setId(jid);
                userProfile.addJurisdiction(jurisdiction);
            }
            return userProfile;
        }
    }
}
