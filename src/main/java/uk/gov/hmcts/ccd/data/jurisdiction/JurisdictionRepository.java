package uk.gov.hmcts.ccd.data.jurisdiction;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
public class JurisdictionRepository {

    @PersistenceContext
    private EntityManager em;

    public JurisdictionEntity findEntityById(String jurisdictionId) {
        return em.find(JurisdictionEntity.class, jurisdictionId);
    }

    @Transactional
    public void create(JurisdictionEntity entity) {
        em.persist(entity);
    }
}
