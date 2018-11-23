package uk.gov.hmcts.ccd.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FindAllUserProfilesOperation {

    private final UserProfileRepository userProfileRepository;

    @Autowired
    public FindAllUserProfilesOperation(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public List<UserProfile> getAll(String jurisdictionId) {
        final Optional<List<UserProfile>> userProfiles =
            Optional.ofNullable(userProfileRepository.findAll(jurisdictionId));
        return userProfiles.orElse(Collections.emptyList());
    }

    public List<UserProfile> getAll() {
        final Optional<List<UserProfile>> userProfiles = Optional.ofNullable(userProfileRepository.findAll());
        return userProfiles.orElse(Collections.emptyList());
    }
}
