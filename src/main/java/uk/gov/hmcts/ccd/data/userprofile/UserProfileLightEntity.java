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
    private String workBasketDefaultJurisdiction;

    @Column(name = "work_basket_default_case_type")
    private String workBasketDefaultCaseType;

    @Column(name = "work_basket_default_state")
    private String workBasketDefaultState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkBasketDefaultJurisdiction() {
        return workBasketDefaultJurisdiction;
    }

    public void setWorkBasketDefaultJurisdiction(final String workBasketDefaultJurisdiction) {
        this.workBasketDefaultJurisdiction = workBasketDefaultJurisdiction;
    }

    public String getWorkBasketDefaultCaseType() {
        return workBasketDefaultCaseType;
    }

    public void setWorkBasketDefaultCaseType(final String workBasketDefaultCaseType) {
        this.workBasketDefaultCaseType = workBasketDefaultCaseType;
    }

    public String getWorkBasketDefaultState() {
        return workBasketDefaultState;
    }

    public void setWorkBasketDefaultState(final String workBasketDefaultState) {
        this.workBasketDefaultState = workBasketDefaultState;
    }
}
