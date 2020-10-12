package uk.gov.hmcts.ccd.domain.service;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionRepository;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

@Service
public class UserProfileOperation {

    private final UserProfileRepository userProfileRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final CreateUserProfileOperation createUserProfileOperation;
    private static final Logger LOG = LoggerFactory.getLogger(UserProfileOperation.class);

    @Inject
    public UserProfileOperation(final UserProfileRepository userProfileRepository,
                                final JurisdictionRepository jurisdictionRepository,
                                final CreateUserProfileOperation createUserProfileOperation) {
        this.userProfileRepository = userProfileRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.createUserProfileOperation = createUserProfileOperation;
    }

    public void execute(final List<UserProfile> userProfiles, final String actionedBy) {
        for (UserProfile userProfile : userProfiles) {

            // ensure that jurisdiction entity exists
            final JurisdictionEntity jurisdictionFound = jurisdictionRepository //
                .findEntityById(userProfile.getWorkBasketDefaultJurisdiction());
            if (null == jurisdictionFound) {
                final JurisdictionEntity newJurisdictionEntity = new JurisdictionEntity();
                newJurisdictionEntity.setId(userProfile.getWorkBasketDefaultJurisdiction());
                LOG.info("Jurisdiction entity for {} not found. Creating one...", newJurisdictionEntity.getId());
                jurisdictionRepository.create(newJurisdictionEntity);
            }

            // We deals with lower case user profile Id only
            userProfile.setId(userProfile.getId().toLowerCase(Locale.UK));

            // finds user profile entity
            final UserProfile userProfileFound = userProfileRepository.findById(userProfile.getId(), actionedBy);

            if (null == userProfileFound) {
                createUserProfileOperation.execute(populateProfileJurisdiction(userProfile), actionedBy);
            } else {
                if (isUpdateRequired(userProfileFound, userProfile)) {
                    userProfileFound.setWorkBasketDefaultCaseType(userProfile.getWorkBasketDefaultCaseType());
                    userProfileFound.setWorkBasketDefaultJurisdiction(userProfile.getWorkBasketDefaultJurisdiction());
                    userProfileFound.setWorkBasketDefaultState(userProfile.getWorkBasketDefaultState());
                    userProfileRepository.updateUserProfile(userProfileFound, actionedBy);
                }
            }
        }
    }

    @VisibleForTesting
    boolean isUpdateRequired(final UserProfile userProfileFromRepository, final UserProfile userProfileFromDefintion) {
        return CollectionUtils.isEmpty(userProfileFromRepository.getJurisdictions()) // checkstyle line break
            || !areDefaultsEqual(userProfileFromRepository, userProfileFromDefintion);
    }

    private boolean areDefaultsEqual(final UserProfile userProfileFromRepository,
                                     final UserProfile userProfileFromDefintion) {
        return StringUtils.equals(userProfileFromRepository.getWorkBasketDefaultCaseType(),
                                  userProfileFromDefintion.getWorkBasketDefaultCaseType()) // checkstyle line break
            && StringUtils.equals(userProfileFromRepository.getWorkBasketDefaultJurisdiction(),
                                  userProfileFromDefintion.getWorkBasketDefaultJurisdiction()) // checkstyle line break
            && StringUtils.equals(userProfileFromRepository.getWorkBasketDefaultState(),
                                  userProfileFromDefintion.getWorkBasketDefaultState())// checkstyle line break
            && userProfileFromRepository.getJurisdictions()
            .stream()
            .anyMatch(j -> StringUtils.equals(
                j.getId(),
                userProfileFromDefintion.getWorkBasketDefaultJurisdiction()));
    }

    private UserProfile populateProfileJurisdiction(final UserProfile userProfile) {

        // creates jurisdiction
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(userProfile.getWorkBasketDefaultJurisdiction());

        // link user profile and jurisdiction
        userProfile.addJurisdiction(jurisdiction);

        return userProfile;
    }
}
