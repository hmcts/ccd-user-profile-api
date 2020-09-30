package uk.gov.hmcts.ccd.data.userprofile;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.repository.PostgreSQLEnumType;


@Entity
@Table(name = "user_profile_audit")
@TypeDef(
    name = "pgsql_auditaction_enum",
    typeClass = PostgreSQLEnumType.class,
    parameters = @Parameter(name = "type", value = "uk.gov.hmcts.ccd.data.userprofile.AuditAction")
)
public class UserProfileAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_profile_audit_id_seq")
    @Getter
    private Integer id;

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
