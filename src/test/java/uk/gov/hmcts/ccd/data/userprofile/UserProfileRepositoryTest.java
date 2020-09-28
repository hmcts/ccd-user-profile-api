package uk.gov.hmcts.ccd.data.userprofile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.data.SanityCheckApplication;
import uk.gov.hmcts.ccd.data.TestConfiguration;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionEntity;
import uk.gov.hmcts.ccd.data.jurisdiction.JurisdictionMapper;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.data.userprofile.AuditAction.CREATE;
import static uk.gov.hmcts.ccd.data.userprofile.AuditAction.DELETE;
import static uk.gov.hmcts.ccd.data.userprofile.AuditAction.READ;
import static uk.gov.hmcts.ccd.data.userprofile.AuditAction.UPDATE;
import static uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository.NOT_APPLICABLE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@Transactional
public class UserProfileRepositoryTest {

    private static final String ACTIONED_BY_EMAIL = "rtaashrie9j1otx@example.com";
    private static final String DEFAULT_CASE_TYPE = "workBasketDefaultCaseType";
    private static final String DEFAULT_STATE = "workBasketDefaultState";

    @Autowired
    private UserProfileRepository classUnderTest;

    @Autowired
    private EntityManager entityManager;

    private UserProfile userProfile;
    private UserProfile userProfileWithMultipleJurisdictions;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST");
        saveJurisdictionClearAndFlushSession(jurisdiction);
        userProfile = createUserProfile("user@hmcts.net", jurisdiction.getId(), "TEST2");
        saveUserProfileClearAndFlushSession(userProfile);
        final Jurisdiction anotherJurisdiction = new Jurisdiction();
        anotherJurisdiction.setId("TEST3");
        saveJurisdictionClearAndFlushSession(anotherJurisdiction);
        final Jurisdiction yetAnotherJurisdiction = new Jurisdiction();
        yetAnotherJurisdiction.setId("TEST4");
        saveJurisdictionClearAndFlushSession(yetAnotherJurisdiction);
        final UserProfile anotherUserProfile =
            createUserProfile("user3@hmcts.net", yetAnotherJurisdiction.getId());
        saveUserProfileClearAndFlushSession(anotherUserProfile);
        userProfileWithMultipleJurisdictions =
            createUserProfile("user4@hmcts.net", jurisdiction.getId(), "TEST5", "TEST6");
        saveUserProfileClearAndFlushSession(userProfileWithMultipleJurisdictions);
        final UserProfile mixedCaseUserProfile =
            createUserProfile("User@HMCTS.net", jurisdiction.getId());
        saveUserProfileClearAndFlushSession(mixedCaseUserProfile);
    }

    @Test
    public void shouldUpdateUserProfileIfAlreadyExists() {
        final Jurisdiction anotherJurisdiction = new Jurisdiction();
        anotherJurisdiction.setId("TEST3");
        this.userProfile.addJurisdiction(anotherJurisdiction);
        UserProfile retrievedUserProfile = classUnderTest.createUserProfile(this.userProfile, ACTIONED_BY_EMAIL);

        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST2", retrievedUserProfile.getJurisdictions().get(0).getId());
        assertEquals("TEST3", retrievedUserProfile.getJurisdictions().get(1).getId());

        final List<UserProfileAuditEntity> audits = findUserProfitAudits("user@hmcts.net");
        assertThat(audits.size(), is(1));
        assertThat(audits.get(0).getAction(), is(UPDATE));
    }

    @Test
    public void createUserProfileWhenItDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST", "TEST", "TEST2");
        assertEquals(0, countUserProfitAudits());
        final UserProfile retrievedUserProfile = classUnderTest.createUserProfile(userProfile, ACTIONED_BY_EMAIL);
        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals("TEST", retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
        assertAuditEntry("user2@hmcts.net",
                         CREATE,
                         ACTIONED_BY_EMAIL,
                         "TEST",
                         "TEST",
                         DEFAULT_CASE_TYPE,
                         DEFAULT_STATE);
    }

    @Test
    public void updateUserProfileWhenItDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST");
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User does not exist");
        classUnderTest.updateUserProfile(userProfile, "exceptionexpected@example.com");
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void updateUserProfileWhenItAlreadyExists() {
        final UserProfile userProfile = createUserProfile("user@hmcts.net", "TEST3");
        userProfile.setWorkBasketDefaultCaseType("Test case");
        userProfile.setWorkBasketDefaultState("Create case");
        final UserProfile retrievedUserProfile = classUnderTest.updateUserProfile(userProfile, ACTIONED_BY_EMAIL);
        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals("TEST3", retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        assertEquals("Test case", retrievedUserProfile.getWorkBasketDefaultCaseType());
        assertEquals("Create case", retrievedUserProfile.getWorkBasketDefaultState());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST2", retrievedUserProfile.getJurisdictions().get(0).getId());
        assertEquals("TEST3", retrievedUserProfile.getJurisdictions().get(1).getId());
        final List<UserProfileAuditEntity> audits = findUserProfitAudits("user@hmcts.net");
        assertThat(audits.size(), is(1));
        assertAuditEntry(audits.get(0),
                         UPDATE,
                         ACTIONED_BY_EMAIL,
                         "TEST",
                         "TEST",
                         DEFAULT_CASE_TYPE,
                         DEFAULT_STATE);
    }

    @Test
    public void findUserProfileById() {
        assertEquals(0, countUserProfitAudits());
        final UserProfile userProfile = classUnderTest.findById("user@hmcts.net", ACTIONED_BY_EMAIL);
        assertEquals("user@hmcts.net", userProfile.getId());
        assertEquals("TEST", userProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(1, userProfile.getJurisdictions().size());
        assertAuditEntry("user@hmcts.net",
                         READ,
                         ACTIONED_BY_EMAIL,
                         "TEST",
                         "TEST",
                         DEFAULT_CASE_TYPE,
                         DEFAULT_STATE);
    }

    @Test
    public void findUserProfileByIdWithNullDefaultJurisdiction() {
        final String email = "icnfadso6fnulljurisdiction@example.com";
        assertEquals(0, countUserProfitAudits());
        final UserProfile another = shallowCopyUserProfile(userProfile, email);
        another.setWorkBasketDefaultJurisdiction(null);
        saveUserProfileClearAndFlushSession(another);
        final UserProfile userProfile = classUnderTest.findById(email, ACTIONED_BY_EMAIL);
        assertEquals(email, userProfile.getId());
        assertNull(userProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void findUserProfileByIdWithNullDefaultCaseType() {
        final String email = "icnfadso6fnullcasetype@example.com";
        assertEquals(0, countUserProfitAudits());
        final UserProfile another = shallowCopyUserProfile(userProfile, email);
        another.setWorkBasketDefaultCaseType(null);
        saveUserProfileClearAndFlushSession(another);
        final UserProfile found = classUnderTest.findById(email, ACTIONED_BY_EMAIL);
        assertEquals(email, found.getId());
        assertEquals("TEST", found.getWorkBasketDefaultJurisdiction());
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void findUserProfileByIdWithNullDefaultState() {
        final String email = "icnfadso6fnullstate@example.com";
        assertEquals(0, countUserProfitAudits());
        final UserProfile another = shallowCopyUserProfile(userProfile, email);
        another.setWorkBasketDefaultState(null);
        saveUserProfileClearAndFlushSession(another);
        final UserProfile found = classUnderTest.findById(email, ACTIONED_BY_EMAIL);
        assertEquals(email, found.getId());
        assertEquals("TEST", found.getWorkBasketDefaultJurisdiction());
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void findUserProfileByIdCaseSensitive() {
        final UserProfile userProfile = classUnderTest.findById("User@HMCTS.net", ACTIONED_BY_EMAIL);
        assertEquals("User@HMCTS.net", userProfile.getId());
        assertEquals("TEST", userProfile.getWorkBasketDefaultJurisdiction());
        assertNull(userProfile.getJurisdictions());
        assertAuditEntry("User@HMCTS.net",
                         READ,
                         ACTIONED_BY_EMAIL,
                         "TEST",
                         "TEST",
                         DEFAULT_CASE_TYPE,
                         DEFAULT_STATE);
    }

    @Test
    public void findAllUserProfiles() {
        final List<UserProfile> userProfiles = classUnderTest.findAll();
        assertEquals(4, userProfiles.size());
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void findAllUserProfilesByJurisdiction() {
        final List<UserProfile> userProfiles = classUnderTest.findAll("TEST2", ACTIONED_BY_EMAIL);
        assertEquals(1, userProfiles.size());
        assertEquals("user@hmcts.net", userProfiles.get(0).getId());
        assertEquals(1, countUserProfitAudits());
        assertAuditEntry(NOT_APPLICABLE,
                         READ,
                         ACTIONED_BY_EMAIL,
                         "TEST2",
                         NOT_APPLICABLE,
                         NOT_APPLICABLE,
                         NOT_APPLICABLE);
    }

    @Test
    public void updateUserProfileOnCreateWhenItDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST");
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User does not exist");
        classUnderTest.updateUserProfileOnCreate(userProfile, ACTIONED_BY_EMAIL);
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void updateUserProfileOnCreateWhenItAlreadyExists() {
        final UserProfile userProfile = createUserProfile("user@hmcts.net", "TEST3");
        userProfile.setWorkBasketDefaultCaseType("Test case");
        userProfile.setWorkBasketDefaultState("Create case");
        final UserProfile retrievedUserProfile = classUnderTest.updateUserProfileOnCreate(userProfile,
                                                                                          ACTIONED_BY_EMAIL);
        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals("TEST3", retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        assertEquals("Test case", retrievedUserProfile.getWorkBasketDefaultCaseType());
        assertEquals("Create case", retrievedUserProfile.getWorkBasketDefaultState());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST2", retrievedUserProfile.getJurisdictions().get(0).getId());
        assertEquals("TEST3", retrievedUserProfile.getJurisdictions().get(1).getId());

        final List<UserProfileAuditEntity> audits = findUserProfitAudits("user@hmcts.net");
        assertThat(audits.size(), is(1));
        assertAuditEntry(audits.get(0), UPDATE, ACTIONED_BY_EMAIL, "TEST", "TEST", DEFAULT_CASE_TYPE, DEFAULT_STATE);
    }

    @Test
    public void updateUserProfileOnCreateWhenUserAlreadyBelongsToJurisdiction() {
        final UserProfile userProfile = createUserProfile("user@hmcts.net", "TEST2");
        userProfile.setWorkBasketDefaultCaseType("Test case");
        userProfile.setWorkBasketDefaultState("Create case");
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User is already a member of the "
            + userProfile.getWorkBasketDefaultJurisdiction() + " jurisdiction");
        classUnderTest.updateUserProfileOnCreate(userProfile, ACTIONED_BY_EMAIL);
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void deleteJurisdictionWhenUserProfileDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST");
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(userProfile.getWorkBasketDefaultJurisdiction());
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User does not exist");
        classUnderTest.deleteJurisdictionFromUserProfile(userProfile, jurisdiction, ACTIONED_BY_EMAIL);
        assertEquals(0, countUserProfitAudits());
    }

    @Test
    public void deleteJurisdictionWhenUserBelongsToMultiple() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST5");
        final UserProfile
            retrievedUserProfile =
            classUnderTest.deleteJurisdictionFromUserProfile(userProfileWithMultipleJurisdictions,
                                                             jurisdiction,
                                                             ACTIONED_BY_EMAIL);
        assertEquals(1, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST6", retrievedUserProfile.getJurisdictions().get(0).getId());
        final List<UserProfileAuditEntity> audits = findUserProfitAudits(userProfileWithMultipleJurisdictions.getId());
        assertThat(audits.size(), is(1));
        assertAuditEntry(audits.get(0), DELETE, ACTIONED_BY_EMAIL, "TEST", "TEST", DEFAULT_CASE_TYPE, DEFAULT_STATE);
    }

    @Test
    public void deleteJurisdictionWhenUserBelongsToSingle() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST2");
        final UserProfile
            retrievedUserProfile =
            classUnderTest.deleteJurisdictionFromUserProfile(userProfile, jurisdiction, ACTIONED_BY_EMAIL);
        // The list of Jurisdictions will be null, rather than an empty list, because the entity is mapped back to a
        // model object and the mapper ignores the now empty JurisdictionEntity list
        assertNull(retrievedUserProfile.getJurisdictions());
        assertNull(retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        final List<UserProfileAuditEntity> audits = findUserProfitAudits(userProfile.getId());
        assertThat(audits.size(), is(1));
        assertAuditEntry(audits.get(0), DELETE, ACTIONED_BY_EMAIL, "TEST", "TEST", DEFAULT_CASE_TYPE, DEFAULT_STATE);
    }

    private UserProfile createUserProfile(final String id, final String defaultJurisdiction, final String... jids) {
        return createFullUserProfile(id, defaultJurisdiction, DEFAULT_CASE_TYPE, DEFAULT_STATE, jids);
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

    private UserProfile shallowCopyUserProfile(final UserProfile orig, final String email) {
        final UserProfile newProfile = new UserProfile();
        newProfile.setId(email);
        newProfile.setWorkBasketDefaultJurisdiction(orig.getWorkBasketDefaultJurisdiction());
        newProfile.setWorkBasketDefaultCaseType(orig.getWorkBasketDefaultCaseType());
        newProfile.setWorkBasketDefaultState(orig.getWorkBasketDefaultState());
        newProfile.setJurisdictions(emptyList());
        return newProfile;
    }

    private List<UserProfileAuditEntity> findUserProfitAudits(final String email) {
        return entityManager.createQuery(
            "select a from UserProfileAuditEntity a where userProfileId = :userProfileId order by a.timestamp ASC",
            UserProfileAuditEntity.class).setParameter("userProfileId", email).getResultList();
    }

    private long countUserProfitAudits() {
        return entityManager.createQuery(
            "select count(a) from UserProfileAuditEntity a", Long.class).getSingleResult();
    }

    private void assertAuditEntry(final String email,
                                  final AuditAction action,
                                  final String actionedBy,
                                  final String jurisdictionId,
                                  final String defaultJurisdiction,
                                  final String defaultCaseType,
                                  final String defaultState) {
        final UserProfileAuditEntity audit = findUserProfitAudits(email).get(0);
        assertAuditEntry(audit, action, actionedBy, jurisdictionId, defaultJurisdiction, defaultCaseType, defaultState);
    }

    private void assertAuditEntry(final UserProfileAuditEntity auditObject,
                                  final AuditAction action,
                                  final String actionedBy,
                                  final String jurisdictionId,
                                  final String defaultJurisdiction,
                                  final String defaultCaseType,
                                  final String defaultState) {
        assertThat(auditObject.getAction(), is(action));
        assertThat(auditObject.getActionedBy(), is(actionedBy));
        assertThat(auditObject.getJurisdictionId(), is(jurisdictionId));
        assertThat(auditObject.getWorkBasketDefaultJurisdiction(), is(defaultJurisdiction));
        assertThat(auditObject.getWorkBasketDefaultCaseType(), is(defaultCaseType));
        assertThat(auditObject.getWorkBasketDefaultState(), is(defaultState));
    }
}
