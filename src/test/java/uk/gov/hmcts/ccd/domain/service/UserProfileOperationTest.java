package uk.gov.hmcts.ccd.domain.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionRepository;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class UserProfileOperationTest {

    private UserProfileOperation subject;

    @Before
    public void setUp() {
        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        JurisdictionRepository jurisdictionRepository = mock(JurisdictionRepository.class);
        CreateUserProfileOperation createUserProfileOperation = mock(CreateUserProfileOperation.class);
        subject = new UserProfileOperation(userProfileRepository, jurisdictionRepository, createUserProfileOperation);
    }

    @Test
    public void isUpdateRequired() {

        // Given two user profiles
        final UserProfile userProfileFromDefinition = createUserDefault("user@hmcts.net",
                                                                        "jurisdiction",
                                                                        "case type",
                                                                        "state");

        final UserProfile userProfile = createUserDefault("user@hmcts.net", "jurisdiction", "case type", "state");

        // case: empty jurisdiction list on userProfile
        assertTrue(subject.isUpdateRequired(userProfile, userProfileFromDefinition));

        // case: jurisdiction id is null
        final Jurisdiction jurisdiction = new Jurisdiction();
        userProfile.addJurisdiction(jurisdiction);
        assertTrue(subject.isUpdateRequired(userProfile, userProfileFromDefinition));

        // case: expect no updates
        jurisdiction.setId("jurisdiction");
        assertFalse(subject.isUpdateRequired(userProfile, userProfileFromDefinition));

        // case: default jurisdiction is different
        final UserProfile userProfile1 = buildUserProfileWithJurisdiction(userProfileFromDefinition, jurisdiction);
        userProfile1.setWorkBasketDefaultJurisdiction("changed");
        assertTrue(subject.isUpdateRequired(userProfile1, userProfileFromDefinition));

        // case: default case type is different
        final UserProfile userProfile2 = buildUserProfileWithJurisdiction(userProfileFromDefinition, jurisdiction);
        userProfile2.setWorkBasketDefaultCaseType("changed");
        assertTrue(subject.isUpdateRequired(userProfile2, userProfileFromDefinition));

        // case default state is different
        final UserProfile userProfile3 = buildUserProfileWithJurisdiction(userProfileFromDefinition, jurisdiction);
        userProfile3.setWorkBasketDefaultState("changed");
        assertTrue(subject.isUpdateRequired(userProfile3, userProfileFromDefinition));
    }

    private UserProfile buildUserProfileWithJurisdiction(final UserProfile userProfileIn,
                                                         final Jurisdiction jurisdiction) {
        final UserProfile userProfile = createUserDefault(
            userProfileIn.getId(),
            userProfileIn.getWorkBasketDefaultJurisdiction(),
            userProfileIn.getWorkBasketDefaultCaseType(),
            userProfileIn.getWorkBasketDefaultState());
        userProfile.addJurisdiction(jurisdiction);
        return userProfile;
    }

    private UserProfile createUserDefault(final String userId,
                                          final String jurisdiction,
                                          final String caseType,
                                          final String state) {
        final UserProfile userProfile = new UserProfile();
        userProfile.setId(userId);
        userProfile.setWorkBasketDefaultCaseType(caseType);
        userProfile.setWorkBasketDefaultJurisdiction(jurisdiction);
        userProfile.setWorkBasketDefaultState(state);
        return userProfile;
    }
}
