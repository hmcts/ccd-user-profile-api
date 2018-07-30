package uk.gov.hmcts.ccd.data.userprofile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Repository
public class UserProfileRepository {

    private final JurisdictionRepository jurisdictionRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public UserProfileRepository(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        if (null != findEntityById(userProfile.getId())) {
            throw new BadRequestException("User already exists with Id " + userProfile.getId());
        }

        Map<String, JurisdictionEntity> existingJurisdictions = new HashMap<>();
        for (Jurisdiction jurisdiction : userProfile.getJurisdictions()) {
            JurisdictionEntity jurisdictionEntity = jurisdictionRepository.findEntityById(jurisdiction.getId());
            if (null != jurisdictionEntity) {
                existingJurisdictions.put(jurisdiction.getId(), jurisdictionEntity);
            }
        }

        UserProfileEntity userProfileEntity = UserProfileMapper.modelToEntity(userProfile, existingJurisdictions);
        em.persist(userProfileEntity);
        return UserProfileMapper.entityToModel(userProfileEntity);
    }

    /**
     * Saves the user profile entity.
     * @param userProfile user profile
     * @return updated user profile
     */
    public UserProfile updateUserProfile(final UserProfile userProfile) {

        final UserProfileEntity userProfileEntity = findEntityById(userProfile.getId());
        if (null == userProfileEntity) {
            throw new BadRequestException("User does not exist with Id " + userProfile.getId());
        }

        userProfileEntity.setWorkBasketDefaultCaseType(userProfile.getWorkBasketDefaultCaseType());
        userProfileEntity.setWorkBasketDefaultJurisdiction(userProfile.getWorkBasketDefaultJurisdiction());
        userProfileEntity.setWorkBasketDefaultState(userProfile.getWorkBasketDefaultState());

        final JurisdictionEntity jurisdictionEntity = jurisdictionRepository
            .findEntityById(userProfile.getWorkBasketDefaultJurisdiction());
        if (0 == userProfileEntity.getJurisdictions()
            .stream()
            .filter(j -> j.getId().equals(userProfile.getWorkBasketDefaultJurisdiction()))
            .count()) {
            userProfileEntity.addJurisdiction(jurisdictionEntity);
        }

        em.persist(userProfileEntity);
        return UserProfileMapper.entityToModel(userProfileEntity);
    }

    public UserProfile findById(String id) {
        return UserProfileMapper.entityToModel(findEntityById(id));
    }

    private UserProfileEntity findEntityById(String id) {
        return em.find(UserProfileEntity.class, id.toLowerCase());
    }

    public List<UserProfile> findAll(String jurisdictionId) {
        TypedQuery<UserProfileEntity> query = em.createNamedQuery("UserProfileEntity.findAllByJurisdiction",
            UserProfileEntity.class);
        query.setParameter("jurisdiction", findJurisdictionEntityById(jurisdictionId));
        return query.getResultList()
            .stream()
            .map(UserProfileMapper::entityToModel)
            .collect(Collectors.toList());
    }

    public List<UserProfile> findAll() {
        TypedQuery<UserProfileEntity> query = em.createNamedQuery("UserProfileEntity.findAll", UserProfileEntity.class);
        return query.getResultList()
            .stream()
            .map(UserProfileMapper::entityToModel)
            .collect(Collectors.toList());
    }

    private JurisdictionEntity findJurisdictionEntityById(String jurisdictionId) {
        return em.find(JurisdictionEntity.class, jurisdictionId);
    }
}
