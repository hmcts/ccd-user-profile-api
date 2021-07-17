package uk.gov.hmcts.ccd.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileCollection;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;
import uk.gov.hmcts.ccd.endpoint.exception.NotFoundException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@Service
public class FindUserProfileOperation {

    private final UserProfileRepository userProfileRepository;

    @Autowired
    public FindUserProfileOperation(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile execute(String userProfileId, String actionedBy) {
        String decodedUid = UriUtils.decode(userProfileId, "UTF-8");
        final UserProfile userProfile = userProfileRepository.findById(decodedUid, actionedBy);
        return Optional.ofNullable(userProfile)
            .orElseThrow(() -> new NotFoundException("Cannot find user profile"));
    }

    public UserProfileCollection execute(List<String> userProfileIds, String actionedBy) {
        validateRequest(userProfileIds);
        final List<UserProfile> userProfiles = userProfileIds.stream()
            .map(userProfileId -> userProfileRepository.findById(userProfileId, actionedBy))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return new UserProfileCollection(userProfiles);
    }

    private void validateRequest(List<String> userProfileIds) {
        if (userProfileIds == null || userProfileIds.isEmpty()
            || userProfileIds.stream().anyMatch(emailId -> !isValidEmailAddress(emailId))) {
            throw new BadRequestException("Email Id(s) not valid");
        }
    }

    private boolean isValidEmailAddress(final String emailId) {
        try {
            new InternetAddress(emailId).validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}

