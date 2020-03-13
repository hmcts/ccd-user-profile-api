package uk.gov.hmcts.ccd.data.userprofile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.domain.model.UserProfileLight;

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

        UserProfileLight userProfile = UserProfileMapper.entityToModel(userProfileLightEntity);

        assertNotNull(userProfile);
        assertEquals(USER_ID, userProfile.getId());
        assertEquals(JURISDICTION, userProfile.getDefaultJurisdiction());
        assertEquals(CASE_TYPE, userProfile.getDefaultCaseType());
        assertEquals(STATE, userProfile.getDefaultState());
    }

    private static UserProfileLightEntity getUserProfileEntity() {
        UserProfileLightEntity userProfileEntity = new UserProfileLightEntity();
        userProfileEntity.setId(USER_ID);
        userProfileEntity.setDefaultJurisdiction(JURISDICTION);
        userProfileEntity.setDefaultCaseType(CASE_TYPE);
        userProfileEntity.setDefaultState(STATE);
        return userProfileEntity;
    }
}
