package uk.gov.hmcts.ccd.data.userprofile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "user_profile_audit")
@Getter
@Setter
public class UserProfileAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "jurisdiction_id")
    private String jurisdictionId;

    @Column(name = "user_profile_id")
    private String userProfileId;

    @Column(name = "work_basket_default_jurisdiction")
    private String workBasketDefaultJurisdiction;

    @Column(name = "work_basket_default_case_type")
    private String workBasketDefaultCaseType;

    @Column(name = "work_basket_default_state")
    private String workBasketDefaultState;

    @Enumerated(value = STRING)
    private AuditAction action;

    @Column(name = "actioned_by")
    private String actionedBy;

    @Column(nullable = false, updatable = false, insertable = false)
    @CreatedDate
    @Setter(AccessLevel.NONE)
    private LocalDateTime timestamp;
}
