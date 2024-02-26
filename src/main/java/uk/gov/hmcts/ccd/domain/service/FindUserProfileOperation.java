package uk.gov.hmcts.ccd.domain.service;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.ccd.ApplicationParams;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileCollection;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;
import uk.gov.hmcts.ccd.endpoint.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class FindUserProfileOperation {

    private final UserProfileRepository userProfileRepository;
    private final ApplicationParams applicationParams;

    @Autowired
    public FindUserProfileOperation(UserProfileRepository userProfileRepository,
                                    ApplicationParams applicationParams) {
        this.userProfileRepository = userProfileRepository;
        this.applicationParams = applicationParams;
    }

    @Transactional
    public UserProfile execute(String userProfileId, String actionedBy) {
        String decodedUid = UriUtils.decode(userProfileId, "UTF-8");
        final UserProfile userProfile = userProfileRepository.findById(decodedUid, actionedBy);
        return Optional.ofNullable(userProfile)
            .orElseThrow(() -> new NotFoundException("Cannot find user profile"));
    }

    @Transactional
    public UserProfileCollection execute(List<String> emailIds, String actionedBy) {
        validateRequest(emailIds);
        return new UserProfileCollection(userProfileRepository.findAllByIds(emailIds, actionedBy));
    }

    private void validateRequest(List<String> emailIds) {
        if (emailIds == null || emailIds.isEmpty()
            || (applicationParams.isUserProfileEmailValidationEnabled()
            && emailIds.stream().anyMatch(emailId -> !isValidEmailAddress(emailId)))) {
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

