package uk.gov.hmcts.ccd.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ApiModel
public class UserProfileCollection {

    private List<UserProfile> userProfiles;

    public UserProfileCollection() {
    }

    public UserProfileCollection(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    @ApiModelProperty
    @JsonProperty("user_profiles")
    public List<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public void setUserProfiles(List<UserProfile> userProfiles) {
        this.userProfiles = userProfiles;
    }
}
