package uk.gov.hmcts.ccd.data.userprofile;

import javax.persistence.*;

/**
 * This entity differs from UserProfileEntity in a way it has no jurisdictions to speed up performance.
 */
@Entity
@Table(name = "user_profile")
@NamedQueries({
    @NamedQuery(name = "UserProfileLightEntity.findAll",
        query = "SELECT u FROM UserProfileLightEntity u"),
})
public class UserProfileLightEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "work_basket_default_jurisdiction")
    private String defaultJurisdiction;

    @Column(name = "work_basket_default_case_type")
    private String defaultCaseType;

    @Column(name = "work_basket_default_state")
    private String defaultState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultJurisdiction() {
        return defaultJurisdiction;
    }

    public void setDefaultJurisdiction(final String workBasketDefaultJurisdiction) {
        this.defaultJurisdiction = workBasketDefaultJurisdiction;
    }

    public String getDefaultCaseType() {
        return defaultCaseType;
    }

    public void setDefaultCaseType(final String workBasketDefaultCaseType) {
        this.defaultCaseType = workBasketDefaultCaseType;
    }

    public String getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(final String workBasketDefaultState) {
        this.defaultState = workBasketDefaultState;
    }
}
