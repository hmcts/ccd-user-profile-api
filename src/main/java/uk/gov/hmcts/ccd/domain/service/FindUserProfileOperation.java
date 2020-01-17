package uk.gov.hmcts.ccd.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.NotFoundException;

import java.util.Optional;

@Service
public class FindUserProfileOperation {

    private final UserProfileRepository userProfileRepository;
    private static final Logger LOG = LoggerFactory.getLogger(FindUserProfileOperation.class);

    @Autowired
    public FindUserProfileOperation(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(String userProfileId, String actionedBy) {
        LOG.info("Finding user with id {}", userProfileId);
        final UserProfile userProfile = userProfileRepository.findById(userProfileId, actionedBy);
        return Optional.ofNullable(userProfile)
            .orElseThrow(() -> new NotFoundException("Cannot find profile for user '" + userProfileId + "'"));
    }
}
