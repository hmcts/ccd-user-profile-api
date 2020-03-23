package uk.gov.hmcts.ccd.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Component;

@Component
@ApiModel
public class UserProfileLight {

    private String id;

    @ApiModelProperty
    @JsonProperty("work_basket_default_jurisdiction")
    private String defaultJurisdiction;

    @ApiModelProperty
    @JsonProperty("work_basket_default_case_type")
    private String defaultCaseType;

    @ApiModelProperty
    @JsonProperty("work_basket_default_state")
    private String defaultState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(final String defaultState) {
        this.defaultState = defaultState;
    }

    public String getDefaultCaseType() {
        return defaultCaseType;
    }

    public void setDefaultCaseType(final String defaultCaseType) {
        this.defaultCaseType = defaultCaseType;
    }

    public String getDefaultJurisdiction() {
        return defaultJurisdiction;
    }

    public void setDefaultJurisdiction(final String defaultJurisdiction) {
        this.defaultJurisdiction = defaultJurisdiction;
    }
}
