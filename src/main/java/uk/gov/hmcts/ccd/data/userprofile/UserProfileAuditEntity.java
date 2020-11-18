package uk.gov.hmcts.ccd.data.userprofile;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.ccd.repository.PostgreSQLEnumType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "user_profile_audit")
@TypeDef(
    name = "pgsql_auditaction_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.data.userprofile.AuditAction")
)
public class UserProfileAuditEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Getter
    private String id;

    @Column(name = "jurisdiction_id")
    @Getter
    @Setter
    private String jurisdictionId;

    @Column(name = "user_profile_id")
    @Getter
    @Setter
    private String userProfileId;

    @Column(name = "work_basket_default_jurisdiction")
    @Getter
    @Setter
    private String workBasketDefaultJurisdiction;

    @Column(name = "work_basket_default_case_type")
    @Getter
    @Setter
    private String workBasketDefaultCaseType;

    @Column(name = "work_basket_default_state")
    @Getter
    @Setter
    private String workBasketDefaultState;

    @Getter
    @Setter
    @Type(type = "pgsql_auditaction_enum")
    private AuditAction action;

    @Column(name = "actioned_by")
    @Getter
    @Setter
    private String actionedBy;

    @Column(nullable = false, updatable = false, insertable = false)
    @CreationTimestamp
    @Getter
    private LocalDateTime timestamp;
}
