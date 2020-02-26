package uk.gov.hmcts.ccd.endpoint.userprofile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.ccd.AppInsights;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.CreateUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.transaction.Transactional;

@RestController
@RequestMapping("/user-profile")
public class UserProfileEndpoint {
    private final CreateUserProfileOperation createUserProfileOperation;
    private final UserProfileOperation userProfileOperation;
    private final FindUserProfileOperation findUserProfileOperation;
    private final AppInsights appInsights;

    @Autowired
    public UserProfileEndpoint(CreateUserProfileOperation createUserProfileOperation,
                               UserProfileOperation userProfileOperation,
                               FindUserProfileOperation findUserProfileOperation,
                               AppInsights appInsights) {
        this.createUserProfileOperation = createUserProfileOperation;
        this.userProfileOperation = userProfileOperation;
        this.findUserProfileOperation = findUserProfileOperation;
        this.appInsights = appInsights;
    }

    @Transactional
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a new User Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created User Profile"),
            @ApiResponse(code = 400, message = "Unable to create User Profile")
    })
    public UserProfile createUserProfile(@RequestBody final UserProfile userProfile,
                                         @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                         final String actionedBy) {
        return createUserProfileOperation.execute(userProfile, actionedBy);
    }

    @Transactional
    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Update a new User Profile",
        notes = "a user profile or jurisdiction is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Updated User Profile defaults")
    })
    public void populateUserProfiles(@RequestBody final List<UserProfile> userProfiles,
                                     @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                     final String actionedBy) {
        userProfileOperation.execute(userProfiles, actionedBy);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Get a user profile")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Found user profile default settings"),
        @ApiResponse(code = 400, message = "Unable to find User Profile")
    })
    public UserProfile userProfileGet(@RequestParam("uid") final String uid,
                                      @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                      final String actionedBy) {
        Instant start = Instant.now();
        String decodedUid = UriUtils.decode(uid, "UTF-8");
        UserProfile userProfile = findUserProfileOperation.execute(decodedUid, actionedBy);
        final Duration between = Duration.between(start, Instant.now());
        appInsights.trackRequest(between, true);
        return userProfile;
    }
}
