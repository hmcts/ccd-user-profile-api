package uk.gov.hmcts.ccd.endpoint.userprofile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.hmcts.ccd.domain.service.CreateUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/user-profile")
public class UserProfileEndpoint {
    private final CreateUserProfileOperation createUserProfileOperation;
    private final UserProfileOperation userProfileOperation;
    private final FindUserProfileOperation findUserProfileOperation;

    @Autowired
    public UserProfileEndpoint(CreateUserProfileOperation createUserProfileOperation,
                               UserProfileOperation userProfileOperation,
                               FindUserProfileOperation findUserProfileOperation) {
        this.createUserProfileOperation = createUserProfileOperation;
        this.userProfileOperation = userProfileOperation;
        this.findUserProfileOperation = findUserProfileOperation;
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new User Profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created User Profile"),
        @ApiResponse(responseCode = "400", description = "Unable to create User Profile")
    })
    public UserProfile createUserProfile(@RequestBody final UserProfile userProfile,
                                         @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                         final String actionedBy) {
        return createUserProfileOperation.execute(userProfile, actionedBy);
    }

    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a new User Profile",
        description = "a user profile or jurisdiction is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Updated User Profile defaults")
    })
    public void populateUserProfiles(@RequestBody final List<UserProfile> userProfiles,
                                     @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                     final String actionedBy) {
        userProfileOperation.execute(userProfiles, actionedBy);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found user profile(s) default settings"),
        @ApiResponse(responseCode = "400", description = "Bad request - Email Id(s) not valid")
    })
    public Object getUserProfiles(@RequestParam(value = "uid", required = false)
                                  final String uid,
                                  @RequestHeader(value = "email-ids-users-to-find", required = false)
                                  final List<String> emailIds,
                                  @RequestHeader(value = "actionedBy", defaultValue = "<UNKNOWN>")
                                  final String actionedBy) {
        Instant start = Instant.now();
        Object responsePayload;

        if (emailIds == null) {
            responsePayload = findUserProfileOperation.execute(uid, actionedBy);
        } else {
            responsePayload = findUserProfileOperation.execute(emailIds, actionedBy);
        }
        return responsePayload;
    }
}
