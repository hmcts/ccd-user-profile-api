package uk.gov.hmcts.ccd.domain.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.NotFoundException;

@Service
public class FindUserProfileOperation {

    private final UserProfileRepository userProfileRepository;

    @Autowired
    public FindUserProfileOperation(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(String userProfileId, String actionedBy) {
        final UserProfile userProfile = userProfileRepository.findById(userProfileId, actionedBy);
        return Optional.ofNullable(userProfile)
            .orElseThrow(() -> new NotFoundException("Cannot find user profile"));
    }
}

