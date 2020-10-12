package uk.gov.hmcts.ccd.data.userprofilejurisdiction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "user_profile_jurisdiction")
@NamedQueries({
    @NamedQuery(name = "UserProfileJurisdictionEntity.findAllByJurisdiction",
        query = "SELECT u FROM UserProfileJurisdictionEntity u WHERE u.jurisdictionId = :jurisdiction")
})
public class UserProfileJurisdictionEntity {

    @Id
    @Column(name = "user_profile_id")
    private String userProfileId;

    @Column(name = "jurisdiction_id")
    private String jurisdictionId;

    public String getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(String userProfileId) {
        this.userProfileId = userProfileId;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }
}
