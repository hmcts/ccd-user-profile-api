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
    private String workBasketDefaultJurisdiction;

    @ApiModelProperty
    @JsonProperty("work_basket_default_case_type")
    private String workBasketDefaultCaseType;

    @ApiModelProperty
    @JsonProperty("work_basket_default_state")
    private String workBasketDefaultState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkBasketDefaultState() {
        return workBasketDefaultState;
    }

    public void setWorkBasketDefaultState(final String workBasketDefaultState) {
        this.workBasketDefaultState = workBasketDefaultState;
    }

    public String getWorkBasketDefaultCaseType() {
        return workBasketDefaultCaseType;
    }

    public void setWorkBasketDefaultCaseType(final String workBasketDefaultCaseType) {
        this.workBasketDefaultCaseType = workBasketDefaultCaseType;
    }

    public String getWorkBasketDefaultJurisdiction() {
        return workBasketDefaultJurisdiction;
    }

    public void setWorkBasketDefaultJurisdiction(final String workBasketDefaultJurisdiction) {
        this.workBasketDefaultJurisdiction = workBasketDefaultJurisdiction;
    }
}
