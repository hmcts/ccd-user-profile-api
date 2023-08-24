package uk.gov.hmcts.ccd.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Schema
public class UserProfileCollection {

    private List<UserProfile> userProfiles;

    public UserProfileCollection() {
    }

    public UserProfileCollection(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    @Schema
    @JsonProperty("user_profiles")
    public List<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public void setUserProfiles(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }
}
