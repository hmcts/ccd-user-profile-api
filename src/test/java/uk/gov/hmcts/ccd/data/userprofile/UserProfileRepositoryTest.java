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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@Transactional
public class UserProfileRepositoryTest {

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
    public void createUserProfileWhenItAlreadyExists() {
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User already exists with Id " + userProfile.getId());
        classUnderTest.createUserProfile(userProfile);
    }

    @Test
    public void createUserProfileWhenItDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST", "TEST", "TEST2");
        final UserProfile retrievedUserProfile = classUnderTest.createUserProfile(userProfile);
        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals("TEST", retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
    }

    @Test
    public void updateUserProfileWhenItDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST");
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User does not exist with Id " + userProfile.getId());
        classUnderTest.updateUserProfile(userProfile);
    }

    @Test
    public void updateUserProfileWhenItAlreadyExists() {
        final UserProfile userProfile = createUserProfile("user@hmcts.net", "TEST3");
        userProfile.setWorkBasketDefaultCaseType("Test case");
        userProfile.setWorkBasketDefaultState("Create case");
        final UserProfile retrievedUserProfile = classUnderTest.updateUserProfile(userProfile);
        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals("TEST3", retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        assertEquals("Test case", retrievedUserProfile.getWorkBasketDefaultCaseType());
        assertEquals("Create case", retrievedUserProfile.getWorkBasketDefaultState());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST2", retrievedUserProfile.getJurisdictions().get(0).getId());
        assertEquals("TEST3", retrievedUserProfile.getJurisdictions().get(1).getId());
    }

    @Test
    public void findUserProfileById() {
        final UserProfile userProfile = classUnderTest.findById("user@hmcts.net");
        assertEquals("user@hmcts.net", userProfile.getId());
        assertEquals("TEST", userProfile.getWorkBasketDefaultJurisdiction());
        assertEquals(1, userProfile.getJurisdictions().size());
    }

    @Test
    public void findUserProfileByIdCaseSensitive() {
        final UserProfile userProfile = classUnderTest.findById("User@HMCTS.net");
        assertEquals("User@HMCTS.net", userProfile.getId());
        assertEquals("TEST", userProfile.getWorkBasketDefaultJurisdiction());
        assertNull(userProfile.getJurisdictions());
    }

    @Test
    public void findAllUserProfiles() {
        final List<UserProfile> userProfiles = classUnderTest.findAll();
        assertEquals(4, userProfiles.size());
    }

    @Test
    public void findAllUserProfilesByJurisdiction() {
        final List<UserProfile> userProfiles = classUnderTest.findAll("TEST2");
        assertEquals(1, userProfiles.size());
        assertEquals("user@hmcts.net", userProfiles.get(0).getId());
    }

    @Test
    public void updateUserProfileOnCreateWhenItDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST");
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User does not exist with ID " + userProfile.getId());
        classUnderTest.updateUserProfileOnCreate(userProfile);
    }

    @Test
    public void updateUserProfileOnCreateWhenItAlreadyExists() {
        final UserProfile userProfile = createUserProfile("user@hmcts.net", "TEST3");
        userProfile.setWorkBasketDefaultCaseType("Test case");
        userProfile.setWorkBasketDefaultState("Create case");
        final UserProfile retrievedUserProfile = classUnderTest.updateUserProfileOnCreate(userProfile);
        assertEquals(userProfile.getId(), retrievedUserProfile.getId());
        assertEquals("TEST3", retrievedUserProfile.getWorkBasketDefaultJurisdiction());
        assertEquals("Test case", retrievedUserProfile.getWorkBasketDefaultCaseType());
        assertEquals("Create case", retrievedUserProfile.getWorkBasketDefaultState());
        assertEquals(2, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST2", retrievedUserProfile.getJurisdictions().get(0).getId());
        assertEquals("TEST3", retrievedUserProfile.getJurisdictions().get(1).getId());
    }

    @Test
    public void updateUserProfileOnCreateWhenUserAlreadyBelongsToJurisdiction() {
        final UserProfile userProfile = createUserProfile("user@hmcts.net", "TEST2");
        userProfile.setWorkBasketDefaultCaseType("Test case");
        userProfile.setWorkBasketDefaultState("Create case");
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User with ID " + userProfile.getId() + " is already a member of the "
            + userProfile.getWorkBasketDefaultJurisdiction() + " jurisdiction");
        classUnderTest.updateUserProfileOnCreate(userProfile);
    }

    @Test
    public void deleteJurisdictionWhenUserProfileDoesNotExist() {
        final UserProfile userProfile = createUserProfile("user2@hmcts.net", "TEST");
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(userProfile.getWorkBasketDefaultJurisdiction());
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("User does not exist with ID " + userProfile.getId());
        classUnderTest.deleteJurisdictionFromUserProfile(userProfile, jurisdiction);
    }

    @Test
    public void deleteJurisdictionWhenUserBelongsToMultiple() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST5");
        final UserProfile retrievedUserProfile =
            classUnderTest.deleteJurisdictionFromUserProfile(userProfileWithMultipleJurisdictions, jurisdiction);
        assertEquals(1, retrievedUserProfile.getJurisdictions().size());
        assertEquals("TEST6", retrievedUserProfile.getJurisdictions().get(0).getId());
    }

    @Test
    public void deleteJurisdictionWhenUserBelongsToSingle() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST2");
        final UserProfile retrievedUserProfile =
            classUnderTest.deleteJurisdictionFromUserProfile(userProfile, jurisdiction);
        // The list of Jurisdictions will be null, rather than an empty list, because the entity is mapped back to a
        // model object and the mapper ignores the now empty JurisdictionEntity list
        assertNull(retrievedUserProfile.getJurisdictions());
        assertNull(retrievedUserProfile.getWorkBasketDefaultJurisdiction());
    }

    private UserProfile createUserProfile(final String id, final String defaultJurisdiction, final String... jids) {
        final UserProfile userProfile = new UserProfile();
        userProfile.setId(id);
        userProfile.setWorkBasketDefaultJurisdiction(defaultJurisdiction);
        // If no Jurisdiction IDs are provided, set the User Profile's Jurisdictions to an empty list, to avoid a
        // NullPointerException when the User Profile is saved
        if (jids.length == 0) {
            userProfile.setJurisdictions(Collections.emptyList());
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
}
