package uk.gov.hmcts.ccd.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionRepository;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.Locale;

@Service
public class SaveUserProfileOperation {

    private static final Logger LOG = LoggerFactory.getLogger(SaveUserProfileOperation.class);
    private final UserProfileRepository userProfileRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final CreateUserProfileOperation createUserProfileOperation;

    @Autowired
    public SaveUserProfileOperation(final UserProfileRepository userProfileRepository,
                                    final JurisdictionRepository jurisdictionRepository,
                                    final CreateUserProfileOperation createUserProfileOperation) {
        this.userProfileRepository = userProfileRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.createUserProfileOperation = createUserProfileOperation;
    }

    public UserProfile saveUserProfile(final UserProfile userProfile, final String actionedBy) {
        final JurisdictionEntity existingJurisdiction = jurisdictionRepository.findEntityById(
            userProfile.getWorkBasketDefaultJurisdiction());
        if (existingJurisdiction == null) {
            final JurisdictionEntity newJurisdiction = new JurisdictionEntity();
            newJurisdiction.setId(userProfile.getWorkBasketDefaultJurisdiction());
            LOG.info("No Jurisdiction entity for {} found. Creating new Jurisdiction...", newJurisdiction.getId()
                .replaceAll("[\n|\r|\t]", "_"));
            jurisdictionRepository.create(newJurisdiction);
        }

        // Ensure the User Profile ID (i.e. email address) is in lowercase
        userProfile.setId(userProfile.getId().toLowerCase(Locale.UK));

        final UserProfile foundUserProfile = userProfileRepository.findById(userProfile.getId(), actionedBy);

        if (foundUserProfile == null) {
            return createUserProfileOperation.execute(userProfile, actionedBy);
        } else {
            // Attempt to update the user profile (UserProfileRepository will throw an exception if the user is being
            // added to a Jurisdiction they already belong to)
            return userProfileRepository.updateUserProfileOnCreate(userProfile, actionedBy);
        }
    }
}
