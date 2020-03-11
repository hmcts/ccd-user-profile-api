package uk.gov.hmcts.ccd.data.userprofile;

import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionMapper;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileLight;

import java.util.Map;

@SuppressWarnings("PMD.ClassNamingConventions") // name is appropriate
public final class UserProfileMapper {

    private UserProfileMapper() {}

    static UserProfileLight entityToModel(UserProfileLightEntity userProfileEntity) {
        if (userProfileEntity == null) {
            return null;
        }
        UserProfileLight userProfileLight = new UserProfileLight();
        userProfileLight.setId(userProfileEntity.getId());
        userProfileLight.setWorkBasketDefaultCaseType(userProfileEntity.getWorkBasketDefaultCaseType());
        userProfileLight.setWorkBasketDefaultJurisdiction(userProfileEntity.getWorkBasketDefaultJurisdiction());
        userProfileLight.setWorkBasketDefaultState(userProfileEntity.getWorkBasketDefaultState());
        return userProfileLight;
    }

    static UserProfile entityToModel(UserProfileEntity userProfileEntity) {
        if (userProfileEntity == null) {
            return null;
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setId(userProfileEntity.getId());
        for (JurisdictionEntity jurisdictionEntity : userProfileEntity.getJurisdictions()) {
            userProfile.addJurisdiction(JurisdictionMapper.entityToModel(jurisdictionEntity));
        }
        userProfile.setWorkBasketDefaultCaseType(userProfileEntity.getWorkBasketDefaultCaseType());
        userProfile.setWorkBasketDefaultJurisdiction(userProfileEntity.getWorkBasketDefaultJurisdiction());
        userProfile.setWorkBasketDefaultState(userProfileEntity.getWorkBasketDefaultState());
        return userProfile;
    }

    static UserProfileEntity modelToEntity(UserProfile userProfile,
                                           Map<String, JurisdictionEntity> existingJurisdictions) {
        UserProfileEntity userProfileEntity = new UserProfileEntity();
        userProfileEntity.setId(userProfile.getId());
        for (Jurisdiction jurisdiction : userProfile.getJurisdictions()) {
            JurisdictionEntity jurisdictionEntity;
            if (existingJurisdictions.containsKey(jurisdiction.getId())) {
                jurisdictionEntity = existingJurisdictions.get(jurisdiction.getId());
            } else {
                jurisdictionEntity = JurisdictionMapper.modelToEntity(jurisdiction);
            }
            userProfileEntity.addJurisdiction(jurisdictionEntity);
        }
        userProfileEntity.setWorkBasketDefaultCaseType(userProfile.getWorkBasketDefaultCaseType());
        userProfileEntity.setWorkBasketDefaultJurisdiction(userProfile.getWorkBasketDefaultJurisdiction());
        userProfileEntity.setWorkBasketDefaultState(userProfile.getWorkBasketDefaultState());
        return userProfileEntity;
    }
}
