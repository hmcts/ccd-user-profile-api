package uk.gov.hmcts.ccd.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import java.util.Optional;

@Service
public class FindUserProfileOperation {

    private final UserProfileRepository userProfileRepository;

    @Autowired
    public FindUserProfileOperation(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(String userProfileId) {
        final UserProfile userProfile = userProfileRepository.findById(userProfileId);
        return Optional.ofNullable(userProfile)
            .orElseThrow(() -> new BadRequestException("No user exists with the Id '" + userProfileId + "'"));
    }
}
