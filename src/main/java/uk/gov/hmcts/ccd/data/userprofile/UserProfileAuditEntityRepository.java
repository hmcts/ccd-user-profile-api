package uk.gov.hmcts.ccd.data.userprofile;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
public class UserProfileAuditEntityRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void createUserProfileAuditEntity(UserProfile userProfile,
                                             AuditAction action,
                                             String actionedBy,
                                             String jurisdiction) {

        final UserProfileAuditEntity entity = new UserProfileAuditEntity();
        entity.setAction(action);
        entity.setActionedBy(actionedBy);
        entity.setJurisdictionId(jurisdiction);
        entity.setUserProfileId(userProfile.getId());
        entity.setWorkBasketDefaultJurisdiction(userProfile.getWorkBasketDefaultJurisdiction());
        entity.setWorkBasketDefaultCaseType(userProfile.getWorkBasketDefaultCaseType());
        entity.setWorkBasketDefaultState(userProfile.getWorkBasketDefaultState());
        em.persist(entity);
    }
}
