package uk.gov.hmcts.ccd.endpoint.userprofile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.AppInsights;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.SaveUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindAllUserProfilesOperation;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
class UserProfileController {
    private final FindAllUserProfilesOperation findAllUserProfilesOperation;
    private final UserProfileOperation userProfileOperation;
    private final SaveUserProfileOperation saveUserProfileOperation;
    private final AppInsights appInsights;

    @Autowired
    public UserProfileController(FindAllUserProfilesOperation findAllUserProfilesOperation,
                                 UserProfileOperation userProfileOperation,
                                 SaveUserProfileOperation saveUserProfileOperation,
                                 AppInsights appInsights) {
        this.findAllUserProfilesOperation = findAllUserProfilesOperation;
        this.userProfileOperation = userProfileOperation;
        this.saveUserProfileOperation = saveUserProfileOperation;
        this.appInsights = appInsights;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get User Profiles",
                  notes = "Optional filtering of results via \"jurisdiction\" request parameter")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Found User Profiles"),
        @ApiResponse(code = 400, message = "Unable to find User Profiles")
    })
    public List<UserProfile> getUserProfiles(@ApiParam(value = "Jurisdiction ID")
                                             @RequestParam("jurisdiction")
                                             final Optional<String> jurisdictionOptional) {
        Instant start = Instant.now();
        List<UserProfile> userProfiles = jurisdictionOptional.map(findAllUserProfilesOperation::getAll)
            .orElse(findAllUserProfilesOperation.getAll());
        final Duration between = Duration.between(start, Instant.now());
        appInsights.trackRequest(between, true);
        return userProfiles;
    }

    @Transactional
    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Update a new User Profile",
        notes = "A User Profile or Jurisdiction is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Updated User Profile defaults")
    })
    public void populateUserProfiles(@RequestBody final List<UserProfile> userProfiles) {
        userProfileOperation.execute(userProfiles);
    }

    @Transactional
    @RequestMapping(value = "/users/save", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Save a User Profile",
                  notes = "A User Profile and/or Jurisdiction is created if it does not exist. Behaves exactly the "
                    + "same as the `PUT` /users endpoint, except that an HTTP 400 Bad Request is returned if the user "
                    + "already belongs to the given Jurisdiction")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Saved User Profile"),
        @ApiResponse(code = 400, message = "User Profile does not exist, or user already belongs to given Jurisdiction")
    })
    public UserProfile saveUserProfile(@RequestBody final UserProfile userProfile) {
        return saveUserProfileOperation.saveUserProfile(userProfile);
    }
}
