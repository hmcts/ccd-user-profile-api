package uk.gov.hmcts.ccd.endpoint.userprofile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ccd.AppInsights;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.FindAllUserProfilesOperation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
public class UserProfileController {
    private final FindAllUserProfilesOperation findAllUserProfilesOperation;
    private final AppInsights appInsights;

    @Autowired
    public UserProfileController(FindAllUserProfilesOperation findAllUserProfilesOperation,
                                 AppInsights appInsights) {
        this.findAllUserProfilesOperation = findAllUserProfilesOperation;
        this.appInsights = appInsights;
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get user profiles",
                  notes = "Optional filtering of results via \"jurisdiction\" request parameter")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Found user profiles"),
        @ApiResponse(code = 400, message = "Unable to find user profiles")
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

}
