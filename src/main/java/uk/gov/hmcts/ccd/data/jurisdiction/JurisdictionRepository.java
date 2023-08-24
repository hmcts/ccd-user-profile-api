package uk.gov.hmcts.ccd.data.jurisdiction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

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
