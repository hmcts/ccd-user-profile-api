package uk.gov.hmcts.ccd.endpoint.userprofile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.CreateUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping(value = "/user-profile")
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

    @Transactional
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new User Profile")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created User Profile"),
            @ApiResponse(code = 400, message = "Unable to create User Profile")
    })
    public UserProfile createUserProfile(@RequestBody final UserProfile userProfile) {
        return createUserProfileOperation.execute(userProfile);
    }

    @Transactional
    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Update a new User Profile",
        notes = "a user profile or jurisdiction is created if it does not exist")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Updated User Profile defaults")
    })
    public void populateUserProfiles(@RequestBody final List<UserProfile> userProfiles) {
        userProfileOperation.execute(userProfiles);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get a user profile")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Found user profile default settings"),
        @ApiResponse(code = 400, message = "Unable to find User Profile")
    })
    public UserProfile userProfileGet(@RequestParam("uid") final String uid) {
        return findUserProfileOperation.execute(uid);
    }
}
