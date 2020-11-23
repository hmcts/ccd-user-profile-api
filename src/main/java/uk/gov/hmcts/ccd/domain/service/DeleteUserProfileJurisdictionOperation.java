package uk.gov.hmcts.ccd.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import java.util.Optional;

@Service
public class DeleteUserProfileJurisdictionOperation {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserProfileJurisdictionOperation.class);
    private static final String WORKBASKET_DEFAULTS_ERROR = "Cannot delete user profile as the user's workbasket "
        + "defaults are set to the Jurisdiction the user is being deleted from. Please update the user's workbasket "
        + "default values to another Jurisdiction and try again.";
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public DeleteUserProfileJurisdictionOperation(final UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile deleteAssociation(final String userId, final String jurisdictionId, final String actionedBy) {
        final UserProfile userProfile = Optional.ofNullable(userProfileRepository.findById(userId, actionedBy))
            .orElseThrow(() -> new BadRequestException("User does not exist"));

        // Throw an exception if the user is not a member of any Jurisdictions
        if (userProfile.getJurisdictions() == null) {
            throw new BadRequestException("User is not a member of any Jurisdictions");
        }

        // Throw an exception if the Jurisdiction being removed matches the user's Workbasket default Jurisdiction.
        // The only case this is permitted is if the user belongs to only one Jurisdiction.
        if (userProfile.getJurisdictions().size() > 1
            && jurisdictionId.equals(userProfile.getWorkBasketDefaultJurisdiction())) {
            throw new BadRequestException(WORKBASKET_DEFAULTS_ERROR);
        }

        Jurisdiction jurisdiction =
            userProfile.getJurisdictions()
                .stream()
                .filter(j -> j.getId().equals(jurisdictionId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("User is not a member of the "
                    + jurisdictionId + " jurisdiction"));


        LOG.info("Deleting association to {} jurisdiction for User Profile ...", jurisdictionId
            .replaceAll("[\n|\r|\t]", "_"));
        return userProfileRepository.deleteJurisdictionFromUserProfile(userProfile, jurisdiction, actionedBy);
    }
}
