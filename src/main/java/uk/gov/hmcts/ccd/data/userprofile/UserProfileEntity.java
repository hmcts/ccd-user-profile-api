package uk.gov.hmcts.ccd.data.userprofile;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profile")
@NamedQueries({
    @NamedQuery(name = "UserProfileEntity.findAllByJurisdiction",
        query = "SELECT u FROM UserProfileEntity u WHERE :jurisdiction MEMBER OF u.jurisdictions"),
    @NamedQuery(name = "UserProfileEntity.findAll",
        query = "SELECT u FROM UserProfileEntity u"),
    @NamedQuery(name = "UserProfileEntity.findAllById",
        query = "SELECT u FROM UserProfileEntity u WHERE u.id IN (:ids)")
})
public class UserProfileEntity {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_profile_jurisdiction", //
        joinColumns = @JoinColumn(name = "user_profile_id", referencedColumnName = "id"), //
        inverseJoinColumns = @JoinColumn(name = "jurisdiction_id", referencedColumnName = "id"))
    private List<JurisdictionEntity> jurisdictions;

    @Column(name = "work_basket_default_jurisdiction")
    private String workBasketDefaultJurisdiction;

    @Column(name = "work_basket_default_case_type")
    private String workBasketDefaultCaseType;

    @Column(name = "work_basket_default_state")
    private String workBasketDefaultState;

    void addJurisdiction(JurisdictionEntity jurisdictionEntity) {
        if (jurisdictions == null) {
            jurisdictions = new ArrayList<>();
        }
        jurisdictions.add(jurisdictionEntity);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<JurisdictionEntity> getJurisdictions() {
        if (jurisdictions == null) {
            jurisdictions = new ArrayList<>();
        }
        return jurisdictions;
    }

    public void setJurisdictions(List<JurisdictionEntity> jurisdictions) {
        this.jurisdictions = jurisdictions;
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
