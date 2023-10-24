package uk.gov.hmcts.ccd.data.userprofile;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.BaseTest;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionMapper;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.data.userprofile.AuditAction.UPDATE;

@Transactional
public class UserProfileAuditRepositoryIT extends BaseTest {

    private static final String ACTIONED_BY_EMAIL = "rtaashrie9j1otx@example.com";
    private static final String DEFAULT_CASE_TYPE = "workBasketDefaultCaseType";
    private static final String DEFAULT_STATE = "workBasketDefaultState";

    private static final String DEFAULT_JURISDICTION = "TEST";

    @Autowired
    private UserProfileAuditEntityRepository classUnderTest;

    @Autowired
    private EntityManager entityManager;

    private UserProfile userProfile;

    @BeforeEach
    public void setUp() {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(DEFAULT_JURISDICTION);
        saveJurisdictionClearAndFlushSession(jurisdiction);
        userProfile = createFullUserProfile("user@hmcts.net", jurisdiction.getId(),
                DEFAULT_CASE_TYPE, DEFAULT_STATE, "TEST2");
        saveUserProfileClearAndFlushSession(userProfile);
    }

    @Test
    public void updateUserProfileWhenItAlreadyExists() {

        AuditAction action = UPDATE;

        classUnderTest.createUserProfileAuditEntity(userProfile, action, ACTIONED_BY_EMAIL, DEFAULT_JURISDICTION);
        final List<UserProfileAuditEntity> audits = findUserProfitAudits("user@hmcts.net");
        assertEquals(1, audits.size());
        assertAuditEntry(audits.get(0),
                        UPDATE,
                        ACTIONED_BY_EMAIL,
                        DEFAULT_JURISDICTION,
                        DEFAULT_JURISDICTION,
                        DEFAULT_CASE_TYPE,
                        DEFAULT_STATE);
    }

    private UserProfile createFullUserProfile(final String id,
                                              final String defaultJurisdiction,
                                              final String workBasketDefaultCaseType,
                                              final String workBasketDefaultState,
                                              final String... jids) {
        final UserProfile userProfile = new UserProfile();
        userProfile.setId(id);
        userProfile.setWorkBasketDefaultJurisdiction(defaultJurisdiction);
        userProfile.setWorkBasketDefaultCaseType(workBasketDefaultCaseType);
        userProfile.setWorkBasketDefaultState(workBasketDefaultState);
        // If no Jurisdiction IDs are provided, set the User Profile's Jurisdictions to an empty list, to avoid a
        // NullPointerException when the User Profile is saved
        if (jids.length == 0) {
            userProfile.setJurisdictions(emptyList());
        } else {
            for (String jid : jids) {
                final Jurisdiction jurisdiction = new Jurisdiction();
                jurisdiction.setId(jid);
                userProfile.addJurisdiction(jurisdiction);
            }
        }
        return userProfile;
    }

    private void saveUserProfileClearAndFlushSession(final UserProfile userProfile) {
        Map<String, JurisdictionEntity> jurisdictions = new HashMap<>();
        for (Jurisdiction jurisdiction : userProfile.getJurisdictions()) {
            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setId(jurisdiction.getId());
            jurisdictions.put(jurisdiction.getId(), jurisdictionEntity);
        }
        entityManager.persist(UserProfileMapper.modelToEntity(userProfile, jurisdictions));
        entityManager.flush();
        entityManager.clear();
    }

    private void saveJurisdictionClearAndFlushSession(final Jurisdiction jurisdiction) {
        entityManager.persist(JurisdictionMapper.modelToEntity(jurisdiction));
        entityManager.flush();
        entityManager.clear();
    }

    private List<UserProfileAuditEntity> findUserProfitAudits(final String email) {
        return entityManager.createQuery(
            "select a from UserProfileAuditEntity a where userProfileId = :userProfileId order by a.timestamp ASC",
            UserProfileAuditEntity.class).setParameter("userProfileId", email).getResultList();
    }


    private void assertAuditEntry(final UserProfileAuditEntity auditObject,
                                  final AuditAction action,
                                  final String actionedBy,
                                  final String jurisdictionId,
                                  final String defaultJurisdiction,
                                  final String defaultCaseType,
                                  final String defaultState) {
        assertEquals(action, auditObject.getAction());
        assertEquals(actionedBy, auditObject.getActionedBy());
        assertEquals(jurisdictionId, auditObject.getJurisdictionId());
        assertEquals(defaultJurisdiction, auditObject.getWorkBasketDefaultJurisdiction());
        assertEquals(defaultCaseType, auditObject.getWorkBasketDefaultCaseType());
        assertEquals(defaultState, auditObject.getWorkBasketDefaultState());
    }
}
