package uk.gov.hmcts.ccd.data.userprofile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileLight;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserProfileMapperTest {

    public static final String USER_ID = "someId@test.com";
    public static final String JURISDICTION = "AUTOTEST1";
    public static final String EXISTING_JURISDICTION = "AUTOTEST";
    public static final String CASE_TYPE = "AllField";
    public static final String STATE = "caseCreated";

    @Test
    @DisplayName("entityToModel should map null UserProfileLightEntity")
    public void shouldMapNullUserProfileLightEntity() {
        assertNull(UserProfileMapper.entityToModel((UserProfileLightEntity) null));
    }

    @Test
    @DisplayName("entityToModel should map UserProfileLightEntity")
    public void shouldMapUserProfileLightEntity() {
        UserProfileLightEntity userProfileLightEntity = getUserProfileLightEntity();

        UserProfileLight userProfile = UserProfileMapper.entityToModel(userProfileLightEntity);

        assertNotNull(userProfile);
        assertEquals(USER_ID, userProfile.getId());
        assertEquals(JURISDICTION, userProfile.getDefaultJurisdiction());
        assertEquals(CASE_TYPE, userProfile.getDefaultCaseType());
        assertEquals(STATE, userProfile.getDefaultState());
    }

    @Test
    public void shouldNotMapNullEntity() {
        UserProfileEntity entity = null;
        UserProfile up = UserProfileMapper.entityToModel(entity);
        assertNull(up);
    }

    @Test
    public void shouldNotGetNullJurisdictions() {
        UserProfileEntity entity = new UserProfileEntity();
        assertNotNull(entity.getJurisdictions());
    }

    @Test
    public void shouldMapEntityToUserProfile() {
        UserProfileEntity entity = getUserProfileEntity();
        UserProfile userProfile = UserProfileMapper.entityToModel(entity);
        assertNotNull(userProfile);
        assertEquals(USER_ID, userProfile.getId());
        assertEquals(JURISDICTION, userProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(CASE_TYPE, userProfile.getWorkBasketDefaultCaseType());
        assertEquals(STATE, userProfile.getWorkBasketDefaultState());

        assertEquals(1, userProfile.getJurisdictions().size());
        assertEquals(JURISDICTION, userProfile.getJurisdictions().get(0).getId());
    }

    @Test
    public void shouldMapUserProfileToEntity() {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID);
        userProfile.setWorkBasketDefaultState(STATE);
        userProfile.setWorkBasketDefaultCaseType(CASE_TYPE);
        userProfile.setWorkBasketDefaultJurisdiction(JURISDICTION);

        userProfile.addJurisdiction(getJurisdiction(JURISDICTION));
        userProfile.addJurisdiction(getJurisdiction(EXISTING_JURISDICTION));

        Map<String, JurisdictionEntity> existingJurisdictions = new HashMap<>();
        existingJurisdictions.put(EXISTING_JURISDICTION, getJurisdictionEntity(EXISTING_JURISDICTION));

        UserProfileEntity entity = UserProfileMapper.modelToEntity(userProfile, existingJurisdictions);

        assertNotNull(entity);
        assertEquals(USER_ID, entity.getId());
        assertEquals(JURISDICTION, entity.getWorkBasketDefaultJurisdiction());
        assertEquals(CASE_TYPE, entity.getWorkBasketDefaultCaseType());
        assertEquals(STATE, entity.getWorkBasketDefaultState());

        assertEquals(2, entity.getJurisdictions().size());
        assertTrue(entity.getJurisdictions().stream().anyMatch(j -> JURISDICTION.equals(j.getId())));
        assertTrue(entity.getJurisdictions().stream().anyMatch(j -> EXISTING_JURISDICTION.equals(j.getId())));
    }

    private static UserProfileLightEntity getUserProfileLightEntity() {
        UserProfileLightEntity userProfileEntity = new UserProfileLightEntity();
        userProfileEntity.setId(USER_ID);
        userProfileEntity.setDefaultJurisdiction(JURISDICTION);
        userProfileEntity.setDefaultCaseType(CASE_TYPE);
        userProfileEntity.setDefaultState(STATE);
        return userProfileEntity;
    }

    private static UserProfileEntity getUserProfileEntity() {
        UserProfileEntity userProfileEntity = new UserProfileEntity();
        userProfileEntity.setId(USER_ID);
        userProfileEntity.setWorkBasketDefaultJurisdiction(JURISDICTION);
        userProfileEntity.setWorkBasketDefaultCaseType(CASE_TYPE);
        userProfileEntity.setWorkBasketDefaultState(STATE);
        userProfileEntity.addJurisdiction(getJurisdictionEntity(JURISDICTION));
        return userProfileEntity;
    }

    private static JurisdictionEntity getJurisdictionEntity(String jurisdictionId) {
        JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
        jurisdictionEntity.setId(jurisdictionId);
        return jurisdictionEntity;
    }

    private static Jurisdiction getJurisdiction(String jurisdictionId) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(jurisdictionId);
        return jurisdiction;
    }
}
