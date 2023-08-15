package uk.gov.hmcts.ccd.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionRepository;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserProfileOperationTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JurisdictionRepository jurisdictionRepository;

    @Mock
    private CreateUserProfileOperation createUserProfileOperation;

    @InjectMocks
    private UserProfileOperation subject;

    public static final String USER_ID = "someId@test.com";
    public static final String JURISDICTION = "AUTOTEST1";
    public static final String CASE_TYPE = "AllField";
    public static final String STATE = "caseCreated";
    public static final String ACTIONED_BY = "actioned@test.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        subject = new UserProfileOperation(userProfileRepository, jurisdictionRepository, createUserProfileOperation);
    }

    @Test
    public void execute() {

        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(createUserDefault(USER_ID,
            JURISDICTION,
            CASE_TYPE,
            STATE));

        userProfiles.add(createUserDefault("new",
            "new_jurisdiction",
            "case",
            "state"));

        UserProfile noUpdate = createUserDefault(
            "no update",
            JURISDICTION,
            null,
            null
        );
        noUpdate.setJurisdictions(Arrays.asList(new Jurisdiction(JURISDICTION)));
        userProfiles.add(noUpdate);

        UserProfile newWithJurisdiction = createUserDefault(
            "new with jurisdiction",
            JURISDICTION,
            null,
            null
        );
        newWithJurisdiction.setJurisdictions(Arrays.asList(new Jurisdiction(JURISDICTION)));
        userProfiles.add(newWithJurisdiction);

        mockJurisditionEntity(JURISDICTION);
        when(jurisdictionRepository.findEntityById("new_jurisdiction")).thenReturn(null);

        mockUserProfile(USER_ID);
        mockUserProfile("no update");

        subject.execute(userProfiles, ACTIONED_BY);

        ArgumentCaptor<JurisdictionEntity> jurisdictionCaptor = ArgumentCaptor.forClass(JurisdictionEntity.class);

        verify(jurisdictionRepository, times(4)).findEntityById(anyString());
        verify(jurisdictionRepository, times(1)).create(jurisdictionCaptor.capture());

        JurisdictionEntity je = jurisdictionCaptor.getValue();
        assertEquals("new_jurisdiction", je.getId());


        ArgumentCaptor<UserProfile> operationCaptor = ArgumentCaptor.forClass(UserProfile.class);

        verify(userProfileRepository, times(4)).findById(anyString(), anyString());
        verify(createUserProfileOperation, times(2))
            .execute(operationCaptor.capture(), anyString());

        assertEquals(2, operationCaptor.getAllValues().size());

        UserProfile createdUP = operationCaptor.getAllValues().get(0);
        assertEquals("new", createdUP.getId());
        assertEquals("new_jurisdiction", createdUP.getWorkBasketDefaultJurisdiction());
        assertEquals("case", createdUP.getWorkBasketDefaultCaseType());
        assertEquals("state", createdUP.getWorkBasketDefaultState());

        assertEquals(1, createdUP.getJurisdictions().size());
        assertEquals("new_jurisdiction", createdUP.getJurisdictions().get(0).getId());

        UserProfile createdWithJurisdictionUP = operationCaptor.getAllValues().get(1);
        assertEquals("new with jurisdiction", createdWithJurisdictionUP.getId());
        assertEquals(JURISDICTION, createdWithJurisdictionUP.getWorkBasketDefaultJurisdiction());
        assertNull(createdWithJurisdictionUP.getWorkBasketDefaultCaseType());
        assertNull(createdWithJurisdictionUP.getWorkBasketDefaultState());

        assertEquals(1, createdWithJurisdictionUP.getJurisdictions().size());
        assertEquals(JURISDICTION, createdWithJurisdictionUP.getJurisdictions().get(0).getId());


        ArgumentCaptor<UserProfile> updateCaptor = ArgumentCaptor.forClass(UserProfile.class);

        verify(userProfileRepository, times(2))
            .updateUserProfile(updateCaptor.capture(), anyString());

        assertEquals(2, updateCaptor.getAllValues().size());

        UserProfile updateUP = updateCaptor.getAllValues().get(0);
        assertEquals(USER_ID.toLowerCase(Locale.UK), updateUP.getId());
        assertEquals(JURISDICTION, updateUP.getWorkBasketDefaultJurisdiction());
        assertEquals(CASE_TYPE, updateUP.getWorkBasketDefaultCaseType());
        assertEquals(STATE, updateUP.getWorkBasketDefaultState());
        assertNull(updateUP.getJurisdictions());

        UserProfile noUpdateValue = updateCaptor.getAllValues().get(1);
        assertEquals("no update", noUpdateValue.getId());
        assertEquals(JURISDICTION, noUpdateValue.getWorkBasketDefaultJurisdiction());
        assertNull(noUpdateValue.getWorkBasketDefaultCaseType());
        assertNull(noUpdateValue.getWorkBasketDefaultState());
        assertNull(noUpdateValue.getJurisdictions());

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

    private JurisdictionEntity mockJurisditionEntity(String jurisditionId) {
        JurisdictionEntity entity = new JurisdictionEntity();
        entity.setId(jurisditionId);
        when(jurisdictionRepository.findEntityById(jurisditionId)).thenReturn(entity);
        return entity;
    }

    private UserProfile mockUserProfile(String userId) {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(userId.toLowerCase(Locale.UK));
        userProfile.setWorkBasketDefaultJurisdiction(JURISDICTION);
        when(userProfileRepository.findById(eq(userId.toLowerCase(Locale.UK)), anyString())).thenReturn(userProfile);
        return userProfile;
    }
}
