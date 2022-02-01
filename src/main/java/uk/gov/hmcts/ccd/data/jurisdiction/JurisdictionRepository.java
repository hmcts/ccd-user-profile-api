package uk.gov.hmcts.ccd.data.jurisdiction;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class JurisdictionRepository {

    @PersistenceContext
    private EntityManager em;

    public JurisdictionEntity findEntityById(String jurisdictionId) {
        return em.find(JurisdictionEntity.class, jurisdictionId);
    }

    public void create(JurisdictionEntity entity) {
        em.persist(entity);
    }
}
