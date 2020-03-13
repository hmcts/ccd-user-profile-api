package uk.gov.hmcts.ccd.data.userprofile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileMapperTest {

    public static final String USER_ID = "someId@test.com";
    public static final String JURISDICTION = "AUTOTEST1";
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
        UserProfileLightEntity userProfileLightEntity = getUserProfileEntity();

        UserProfile userProfile = UserProfileMapper.entityToModel(userProfileLightEntity);

        assertNotNull(userProfile);
        assertEquals(USER_ID, userProfile.getId());
        assertEquals(JURISDICTION, userProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(CASE_TYPE, userProfile.getWorkBasketDefaultCaseType());
        assertEquals(STATE, userProfile.getWorkBasketDefaultState());
    }

    @Test
    @DisplayName("entityToModel should map null UserProfileLightEntity with jurisdiction")
    public void shouldMapNullUserProfileLightEntityWithJurisdiction() {
        assertNull(UserProfileMapper.entityToModel(null, getJurisdiction(JURISDICTION)));
    }

    @Test
    @DisplayName("entityToModel should map UserProfileLightEntity with jurisdiction")
    public void shouldMapUserProfileLightEntityWithJurisdiction() {
        UserProfileLightEntity userProfileLightEntity = getUserProfileEntity();

        UserProfile userProfile = UserProfileMapper.entityToModel(userProfileLightEntity,
            getJurisdiction(JURISDICTION));

        assertNotNull(userProfile);
        assertEquals(1, userProfile.getJurisdictions().size());
        assertEquals(getJurisdiction(JURISDICTION).getId(), userProfile.getJurisdictions().get(0).getId());
        assertEquals(USER_ID, userProfile.getId());
        assertEquals(JURISDICTION, userProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(CASE_TYPE, userProfile.getWorkBasketDefaultCaseType());
        assertEquals(STATE, userProfile.getWorkBasketDefaultState());
    }

    private static UserProfileLightEntity getUserProfileEntity() {
        UserProfileLightEntity userProfileEntity = new UserProfileLightEntity();
        userProfileEntity.setId(USER_ID);
        userProfileEntity.setDefaultJurisdiction(JURISDICTION);
        userProfileEntity.setDefaultCaseType(CASE_TYPE);
        userProfileEntity.setDefaultState(STATE);
        return userProfileEntity;
    }

    private Jurisdiction getJurisdiction(String jurisdictionId) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(jurisdictionId);
        return jurisdiction;
    }
}
