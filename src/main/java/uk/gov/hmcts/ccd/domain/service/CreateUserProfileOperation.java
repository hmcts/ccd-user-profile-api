package uk.gov.hmcts.ccd.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

@Service
public class CreateUserProfileOperation {
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public CreateUserProfileOperation(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(UserProfile userProfile, String actionedBy) {
        if (null == userProfile.getId()) {
            throw new BadRequestException("A User Profile must have an Id");
        }

        if (userProfile.getJurisdictions() == null || userProfile.getJurisdictions().isEmpty()) {
            throw new BadRequestException("A User Profile must have at least one associated Jurisdiction");
        }

        for (Jurisdiction jurisdiction : userProfile.getJurisdictions()) {
            if (jurisdiction.getId() == null) {
                throw new BadRequestException("A Jurisdiction must have an Id");
            }
        }
        return userProfileRepository.createUserProfile(userProfile, actionedBy);
    }
}
