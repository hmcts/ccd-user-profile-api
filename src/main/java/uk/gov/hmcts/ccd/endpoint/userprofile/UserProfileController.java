package uk.gov.hmcts.ccd.endpoint.userprofile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileLight;
import uk.gov.hmcts.ccd.domain.service.DeleteUserProfileJurisdictionOperation;
import uk.gov.hmcts.ccd.domain.service.FindAllUserProfilesOperation;
import uk.gov.hmcts.ccd.domain.service.SaveUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
class UserProfileController {
    private final FindAllUserProfilesOperation findAllUserProfilesOperation;
    private final UserProfileOperation userProfileOperation;
    private final SaveUserProfileOperation saveUserProfileOperation;
    private final DeleteUserProfileJurisdictionOperation deleteUserProfileJurisdictionOperation;

    @Autowired
    public UserProfileController(FindAllUserProfilesOperation findAllUserProfilesOperation,
                                 UserProfileOperation userProfileOperation,
                                 SaveUserProfileOperation saveUserProfileOperation,
                                 DeleteUserProfileJurisdictionOperation deleteUserProfileJurisdictionOperation) {
        this.findAllUserProfilesOperation = findAllUserProfilesOperation;
        this.userProfileOperation = userProfileOperation;
        this.saveUserProfileOperation = saveUserProfileOperation;
        this.deleteUserProfileJurisdictionOperation = deleteUserProfileJurisdictionOperation;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get User Profiles",
                  description = "Optional filtering of results via \"jurisdiction\" request parameter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found User Profiles")
    })
    public List<UserProfileLight> getUserProfiles(@Parameter(name = "Jurisdiction ID") @RequestParam("jurisdiction")
                                                 final Optional<String> jurisdictionOptional,
                                                  @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                                 final String actionedBy) {
        Instant start = Instant.now();
        List<UserProfileLight> allUserProfiles = jurisdictionOptional
            .map(j -> findAllUserProfilesOperation.getAllLight(j, actionedBy))
            .orElseGet(findAllUserProfilesOperation::getAllLight);

        final Duration between = Duration.between(start, Instant.now());
        long betweenInMils = between.toMillis();
        log.info("Found {} entries", allUserProfiles.size());
        log.info("Time to execute the query: {} milliseconds", betweenInMils);

        return allUserProfiles;
    }

    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a new User Profile",
                  description = "A User Profile or Jurisdiction is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated User Profile defaults")
    })
    public void populateUserProfiles(@RequestBody final List<UserProfile> userProfiles,
                                     @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                     final String actionedBy) {
        userProfileOperation.execute(userProfiles, actionedBy);
    }

    @RequestMapping(value = "/users/save", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Save a User Profile",
                  description = "A User Profile and/or Jurisdiction is created if it does not exist."
                      + "Behaves exactly the same as the `PUT` /users endpoint, except that an HTTP 400 "
                      + "Bad Request is returned if the user already belongs to the given Jurisdiction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Saved User Profile"),
        @ApiResponse(responseCode = "400", description = "User Profile does not exist, or user already "
            + "belongs to given Jurisdiction")
    })
    public UserProfile saveUserProfile(@RequestBody final UserProfile userProfile,
                                       @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                       final String actionedBy) {
        return saveUserProfileOperation.saveUserProfile(userProfile, actionedBy);
    }

    @RequestMapping(value = "/users", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete the association to a Jurisdiction from a User Profile",
                  description = "Deletes the Jurisdiction unless it matches the user's Workbasket default Jurisdiction "
                    + "AND the user currently belongs to more than one Jurisdiction. If the Jurisdiction being deleted "
                    + "is the user's sole Jurisdiction, removal is permitted. In addition, their Workbasket defaults "
                    + "are set to null, since they no longer belong to any Jurisdictions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deleted Jurisdiction from User Profile"),
        @ApiResponse(responseCode = "400", description = "User Profile does not exist, user is not a member "
                                    + "of specified Jurisdiction, or user's Workbasket defaults are for "
                                    + "the Jurisdiction being removed")
    })
    public void deleteJurisdictionFromUserProfile(@Parameter(name = "User Profile ID")
                                                  @RequestParam("uid")
                                                  final String uid,
                                                  @Parameter(name = "Jurisdiction ID")
                                                  @RequestParam("jid")
                                                  final String jid,
                                                  @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                                      final String actionedBy) {
        deleteUserProfileJurisdictionOperation.deleteAssociation(uid, jid, actionedBy);
    }
}
