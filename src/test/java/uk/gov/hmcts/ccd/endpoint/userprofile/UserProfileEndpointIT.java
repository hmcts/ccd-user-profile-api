package uk.gov.hmcts.ccd.endpoint.userprofile;

import com.fasterxml.jackson.databind.JsonNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ccd.BaseTest;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileEntity;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressFBWarnings // added for avoiding overhead of null checks. CCD Policy: allowed only for test
public class UserProfileEndpointIT extends BaseTest {

    private static final String CREATE_USER_PROFILE = "/user-profile/users";
    private static final String FIND_PROFILE_FOR_USER_1 = "/user-profile/users?uid=USER1";
    private static final String FIND_JURISDICTION_FOR_USER_1 = "/user-profile/users?uid=user1";
    private static final String FIND_JURISDICTION_FOR_USER_2 = "/user-profile/users?uid=user2%2Ba%40example.com";
    private static final String USER_PROFILE_USERS_DEFAULTS = "/users";
    private static final String GET_ALL_USER_PROFILES_FOR_JURISDICTION = "/users?jurisdiction=TEST1";
    private static final String GET_ALL_USER_PROFILES = "/users";
    private static final String SAVE_USER_PROFILE = "/users/save";
    private static final String DELETE_NON_DEFAULT_JURISDICTION = "/users?uid=user1&jid=TEST1";
    private static final String DELETE_DEFAULT_JURISDICTION = "/users?uid=user1&jid=TEST2";
    private static final String DELETE_JURISDICTION_NOT_IN_USER_LIST = "/users?uid=user5&jid=TEST1";
    private static final String DELETE_SOLE_JURISDICTION = "/users?uid=user5&jid=TEST2";

    private static final String USER_ID_1 = "user1";
    private static final String USER_ID_2 = "user2+a@example.com";
    private static final String USER_ID_3 = "user3";
    private static final String JURISDICTION_ID_1 = "TEST1";
    private static final String JURISDICTION_ID_2 = "TEST2";
    private static final String JURISDICTION_ID_3 = "TEST3";

    private static final String GET_USER_PROFILE_QUERY = "SELECT * FROM user_profile where id = ?";
    private static final String COUNT_JURISDICTION_QUERY = "SELECT count(1) FROM jurisdiction where id = ?";
    private static final String COUNT_USER_PROFILE_JURISDICTION_QUERY
        = "SELECT count(1) FROM user_profile_jurisdiction where user_profile_id = ? and jurisdiction_id = ?";
    private static final String COUNT_USER_PROFILE_ALL_JURISDICTIONS_QUERY
        = "SELECT COUNT(1) FROM user_profile_jurisdiction WHERE user_profile_id = ?";
    private static final String COUNT_ALL_JURISDICTIONS_QUERY = "SELECT COUNT(1) FROM jurisdiction";

    private static final String DELETE_WORKBASKET_DEFAULT_JURISDICTION_ERROR = "Cannot delete user profile as the "
        + "user's workbasket defaults are set to the Jurisdiction the user is being deleted from. Please update the "
        + "user's workbasket default values to another Jurisdiction and try again.";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        template = new JdbcTemplate(db);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/init_db.sql", "classpath:sql/create_jurisdiction.sql"})
    public void createUserProfileTest() throws Exception {
        // Given - a valid User Profile linked to 3 Jurisdictions
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID_1);

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId(JURISDICTION_ID_1);
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId(JURISDICTION_ID_2);
        Jurisdiction jurisdiction3 = new Jurisdiction();
        jurisdiction3.setId(JURISDICTION_ID_3);

        userProfile.addJurisdiction(jurisdiction1);
        userProfile.addJurisdiction(jurisdiction2);
        userProfile.addJurisdiction(jurisdiction3);

        populateUserProfileDefaults(userProfile, JURISDICTION_ID_1);

        // When - trying to Create the User Profile
        // Then - assert that the User Profile is correctly saved
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals("Unexpected response status", 201, mvcResult.getResponse().getStatus());
        JsonNode createdUserProfile = mapper.readTree(mvcResult.getResponse().getContentAsString());
        assertEquals("Unexpected User Profile Id", USER_ID_1, createdUserProfile.get("id").asText());

        int rows = JdbcTestUtils.countRowsInTableWhere(template,
                "user_profile", "id = '" + USER_ID_1 + "'");
        assertEquals("Unexpected number of Users", 1, rows);

        rows = JdbcTestUtils.countRowsInTableWhere(template,
                "user_profile_jurisdiction",
                "user_profile_id = '" + USER_ID_1
                        + "' and jurisdiction_id in ('"
                        + JURISDICTION_ID_1 + "', '"
                        + JURISDICTION_ID_2 + "', '"
                        + JURISDICTION_ID_3
                        + "')");
        assertEquals("Unexpected number of User/Jurisdiction joins", 3, rows);

        rows = JdbcTestUtils.countRowsInTable(template, "jurisdiction");
        assertEquals("Unexpected number of Jurisdictions", 3, rows);

        final int
            auditRows =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "user_profile_id = '" + USER_ID_1 + "'");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = {"classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql"})
    public void createUsingExistingJurisdictions() throws Exception {
        // Given - a pre-existing UserProfile with Id = user1 which has 3
        // Jurisdictions associated.
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID_3);

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId(JURISDICTION_ID_1);
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId(JURISDICTION_ID_2);

        userProfile.addJurisdiction(jurisdiction1);
        userProfile.addJurisdiction(jurisdiction2);

        populateUserProfileDefaults(userProfile, JURISDICTION_ID_1);

        // When - attempting to create a new UserProfile against the existing
        // Jurisdictions
        // Then - assert that the UserProfile is created as expected
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals("Unexpected response status", 201, mvcResult.getResponse().getStatus());
        JsonNode createdUserProfile = mapper.readTree(mvcResult.getResponse().getContentAsString());
        assertEquals("Unexpected User Profile Id", USER_ID_3, createdUserProfile.get("id").asText());

        int rows = JdbcTestUtils.countRowsInTableWhere(template,
                "user_profile", "id = '" + USER_ID_3 + "'");
        assertEquals("Unexpected number of Users", 1, rows);

        rows = JdbcTestUtils.countRowsInTableWhere(template,
                "user_profile_jurisdiction",
                "user_profile_id = '" + USER_ID_3 + "'");
        assertEquals("Unexpected number of User/Jurisdiction joins", 2, rows);

        final int
            auditRows =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "user_profile_id = '" + USER_ID_3 + "'");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = {"classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql"})
    public void createExistingUserProfile() throws Exception {
        // Given - a pre-existing UserProfile with Id = user1
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID_1);

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId(JURISDICTION_ID_1);

        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId(JURISDICTION_ID_2);

        userProfile.addJurisdiction(jurisdiction1);
        userProfile.addJurisdiction(jurisdiction2);

        // When - attempting to create a new UserProfile with Id = user1
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals(201, mvcResult.getResponse().getStatus());
        JsonNode createdUserProfile = mapper.readTree(mvcResult.getResponse().getContentAsString());
        assertEquals(USER_ID_1, createdUserProfile.get("id").asText());

        int rows = JdbcTestUtils.countRowsInTableWhere(template,
            "user_profile_jurisdiction",
            "user_profile_id = '" + USER_ID_1 + "'");

        assertEquals(2, rows);

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:sql/init_db.sql" })
    public void createUserProfileNullId() throws Exception {
        // Given - a UserProfile with a null Id
        UserProfile userProfile = new UserProfile();

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId(JURISDICTION_ID_1);

        userProfile.addJurisdiction(jurisdiction1);

        // When - attempting to create the new UserProfile
        // Then - assert that the creation fails with the expected exception
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message",
                     "A User Profile must have an Id",
                     mvcResult.getResolvedException().getMessage());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:sql/init_db.sql" })
    public void createUserProfileNoJurisdictions() throws Exception {
        // Given - a User Profile with an empty list of Jurisdictions
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID_1);
        userProfile.setJurisdictions(new ArrayList<>());

        // When - attempting to create the User Profile
        // Then - assert that the expected error is returned
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message", "A User Profile must have at least one associated Jurisdiction",
                mvcResult.getResolvedException().getMessage());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:sql/init_db.sql" })
    public void createUserProfileNullJurisdictions() throws Exception {
        // Given - a User Profile with an empty list of Jurisdictions
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID_1);

        // When - attempting to create the User Profile
        // Then - assert that the expected error is returned
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message", "A User Profile must have at least one associated Jurisdiction",
                mvcResult.getResponse().getContentAsString());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:sql/init_db.sql" })
    public void createUserProfileNullJurisdictionId() throws Exception {
        // Given - a Jurisdiction with a null Id
        UserProfile userProfile = new UserProfile();
        userProfile.setId(USER_ID_1);

        Jurisdiction jurisdiction1 = new Jurisdiction();

        userProfile.addJurisdiction(jurisdiction1);

        // When - attempting to create the new UserProfile
        // Then - assert that the creation fails with the expected exception
        final MvcResult mvcResult = mockMvc.perform(
                post(CREATE_USER_PROFILE)
                        .contentType(contentType)
                        .content(mapper.writeValueAsBytes(userProfile)))
                .andReturn();

        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message",
                     "A Jurisdiction must have an Id",
                     mvcResult.getResponse().getContentAsString());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = {"classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql"})
    public void getUserProfile() throws Exception {
        // Given - a User Profile (id = user1) with 3 Jurisdictions (TEST1,
        // TEST2 and TEST3)
        // When - attempting to find Jurisdictions for the User Profile
        final MvcResult mvcResult = mockMvc.perform(get(FIND_JURISDICTION_FOR_USER_1)).andReturn();

        assertEquals("Unexpected response status", 200, mvcResult.getResponse().getStatus());

        final UserProfile profile = mapper.readValue(mvcResult.getResponse().getContentAsString(), UserProfile.class);

        // Then correct user profile information is returned
        assertEquals(USER_ID_1, profile.getId());
        assertEquals("TEST2", profile.getWorkBasketDefaultJurisdiction());
        assertEquals("state", profile.getWorkBasketDefaultState());
        assertEquals("case", profile.getWorkBasketDefaultCaseType());

        // and all 3 jurisdictions
        List<Jurisdiction> jurisdictionsFromResponse = profile.getJurisdictions();
        assertEquals("Unexpected number of Jurisdictions", 3, jurisdictionsFromResponse.size());

        String[] expectedJurisdictionsArray = { JURISDICTION_ID_1, JURISDICTION_ID_2, JURISDICTION_ID_3 };
        List<String> expectedJurisdictions = Arrays.asList(expectedJurisdictionsArray);
        assertTrue("Unexpected Jurisdiction Id",
                   expectedJurisdictions.contains(jurisdictionsFromResponse.get(0).getId()));
        assertTrue("Unexpected Jurisdiction Id",
                   expectedJurisdictions.contains(jurisdictionsFromResponse.get(1).getId()));
        assertTrue("Unexpected Jurisdiction Id",
                   expectedJurisdictions.contains(jurisdictionsFromResponse.get(2).getId()));

        final int
            auditRows =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "user_profile_id = '" + USER_ID_1 + "'");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = {"classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql"})
    public void noJurisdictionsForUserProfile() throws Exception {
        // Given - a User Profile (id = user2+a@example.com) with 0 Jurisdictions
        int rows = JdbcTestUtils.countRowsInTableWhere(template,
                "user_profile_jurisdiction",
                "user_profile_id = '" + USER_ID_2 + "'");
        assertEquals("Unexpected number of User/Jurisdiction joins", 0, rows);

        // When - attempting to find Jurisdictions for the User Profile
        // Then - assert that no Jurisdiction id's are found
        final MvcResult mvcResult = mockMvc.perform(get(FIND_JURISDICTION_FOR_USER_2)).andReturn();

        assertEquals("Unexpected response status", 200, mvcResult.getResponse().getStatus());

        final UserProfile profile = mapper.readValue(mvcResult.getResponse().getContentAsString(), UserProfile.class);
        assertNull(profile.getJurisdictions());
        assertEquals(USER_ID_2, profile.getId());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:sql/init_db.sql" })
    public void getJurisdictionsForNonExistentUserProfile() throws Exception {
        // Given - there is no User Profile with id = user1
        // When - attempting to find Jurisdictions for a User Profile with id =
        // user1
        // Then - assert that the expected error is returned
        final MvcResult mvcResult = mockMvc.perform(get(FIND_PROFILE_FOR_USER_1)).andReturn();

        assertEquals("Unexpected response status", 404, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message", "Cannot find profile for user 'USER1'",

            mvcResult.getResponse().getContentAsString());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void updateUserProfileDefaults() throws Exception {

        // Given a list of user profile defaults
        List<UserProfile> userProfiles = new ArrayList<>();

        // user exists, jurisdiction does not
        userProfiles.add(createUserProfile("user1", "case type", "TEST4", "Issued"));

        // user does not exist, jurisdiction exists
        userProfiles.add(createUserProfile("User2@hmcts.net", "case type", "TEST3", "Created"));

        // user does not exist, jurisdiction does not exist
        userProfiles.add(createUserProfile("uSer3@hmcts.net", "case type", "TEST5", "Issued"));

        // user exists and jurisdiction exists
        userProfiles.add(createUserProfile("USER2", "case type", "TEST2", "Issued"));

        // When an update user profile end point is invoked
        mockMvc.perform(put(USER_PROFILE_USERS_DEFAULTS)
            .contentType(contentType)
            .content(mapper.writeValueAsBytes(userProfiles)))
            .andExpect(status().is(200))
            .andReturn();

        // Then the user profile defaults are stored in the database
        final UserProfileEntity userProfileEntity1 = template.queryForObject(
            GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user1");

        assertEquals("user1", userProfileEntity1.getId());
        assertEquals("case type", userProfileEntity1.getWorkBasketDefaultCaseType());
        assertEquals("TEST4", userProfileEntity1.getWorkBasketDefaultJurisdiction());
        assertEquals("Issued", userProfileEntity1.getWorkBasketDefaultState());

        final UserProfileEntity userProfileEntity2 = template.queryForObject(
            GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user2@hmcts.net");

        assertEquals("user2@hmcts.net", userProfileEntity2.getId());
        assertEquals("case type", userProfileEntity2.getWorkBasketDefaultCaseType());
        assertEquals("TEST3", userProfileEntity2.getWorkBasketDefaultJurisdiction());
        assertEquals("Created", userProfileEntity2.getWorkBasketDefaultState());

        final UserProfileEntity userProfileEntity3 = template.queryForObject(
            GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user3@hmcts.net");

        assertEquals("user3@hmcts.net", userProfileEntity3.getId());
        assertEquals("case type", userProfileEntity3.getWorkBasketDefaultCaseType());
        assertEquals("TEST5", userProfileEntity3.getWorkBasketDefaultJurisdiction());
        assertEquals("Issued", userProfileEntity3.getWorkBasketDefaultState());

        final UserProfileEntity userProfileEntity4 = template.queryForObject(
            GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user2");

        assertEquals("user2", userProfileEntity4.getId());
        assertEquals("case type", userProfileEntity4.getWorkBasketDefaultCaseType());
        assertEquals("TEST2", userProfileEntity4.getWorkBasketDefaultJurisdiction());
        assertEquals("Issued", userProfileEntity4.getWorkBasketDefaultState());

        assertEquals(1, template.queryForObject(COUNT_JURISDICTION_QUERY, Integer.class, "TEST4").intValue());
        assertEquals(1, template.queryForObject(COUNT_JURISDICTION_QUERY, Integer.class, "TEST5").intValue());

        assertEquals(1, template.queryForObject(COUNT_USER_PROFILE_JURISDICTION_QUERY, Integer.class,
            "user1", "TEST4").intValue());
        assertEquals(1, template.queryForObject(COUNT_USER_PROFILE_JURISDICTION_QUERY, Integer.class,
            "user2@hmcts.net", "TEST3").intValue());
        assertEquals(1, template.queryForObject(COUNT_USER_PROFILE_JURISDICTION_QUERY, Integer.class,
            "user3@hmcts.net", "TEST5").intValue());
        assertEquals(1, template.queryForObject(COUNT_USER_PROFILE_JURISDICTION_QUERY, Integer.class,
            "user2", "TEST2").intValue());

        // And existing user profile jurisdiction link is intact
        assertEquals(1, template.queryForObject(COUNT_USER_PROFILE_JURISDICTION_QUERY, Integer.class,
            "user1", "TEST1").intValue());


        final int auditRow1 = JdbcTestUtils.countRowsInTableWhere(template,
                                                                  "user_profile_audit",
                                                                  "action = 'READ' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 1, auditRow1);

        final int
            auditRow2 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'READ' and user_profile_id = 'user2@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 0, auditRow2);

        final int
            auditRow3 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'READ' and user_profile_id = 'user3@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 0, auditRow3);

        final int auditRow4 = JdbcTestUtils.countRowsInTableWhere(template,
                                                                  "user_profile_audit",
                                                                  "action = 'READ' and user_profile_id = 'user2'");
        assertEquals("Unexpected number of audit roles", 0, auditRow4);

        final int
            auditRowUpdate1 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'UPDATE' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 1, auditRowUpdate1);

        final int
            auditRowUpdate2 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'UPDATE' and user_profile_id = 'user2@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 0, auditRowUpdate2);

        final int
            auditRowUpdate3 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'UPDATE' and user_profile_id = 'user3@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 0, auditRowUpdate3);

        final int
            auditRowUpdate4 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'UPDATE' and user_profile_id = 'user2'");
        assertEquals("Unexpected number of audit roles", 0, auditRowUpdate4);

        final int
            auditRowCreate2 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'CREATE' and user_profile_id = 'user2@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 1, auditRowCreate2);

        final int
            auditRowCreate3 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'CREATE' and user_profile_id = 'user3@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 1, auditRowCreate3);

        final int
            auditRowCreate4 =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'CREATE' and user_profile_id = 'user2+a@example.com'");
        assertEquals("Unexpected number of audit roles", 0, auditRowCreate4);

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void getAllUserProfilesForJurisdiction() throws Exception {
        // Given there are two User Profiles with Jurisdiction "TEST1"
        // When attempting to find all User Profiles with the above Jurisdiction
        final MvcResult mvcResult = mockMvc.perform(get(GET_ALL_USER_PROFILES_FOR_JURISDICTION)).andReturn();

        assertEquals("Unexpected response status", 200, mvcResult.getResponse().getStatus());

        final List<UserProfile> userProfiles =
            Arrays.asList(mapper.readValue(mvcResult.getResponse().getContentAsString(), UserProfile[].class));

        // Then a list with the two User Profiles is returned
        assertEquals("Unexpected number of User Profiles", 2, userProfiles.size());


        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void getAllUserProfiles() throws Exception {
        // Given there are four User Profiles in total
        // When attempting to find all User Profiles
        final MvcResult mvcResult = mockMvc.perform(get(GET_ALL_USER_PROFILES)).andReturn();

        assertEquals("Unexpected response status", 200, mvcResult.getResponse().getStatus());

        final List<UserProfile> userProfiles =
            Arrays.asList(mapper.readValue(mvcResult.getResponse().getContentAsString(), UserProfile[].class));

        // Then a list with four User Profiles is returned
        assertEquals("Unexpected number of User Profiles", 4, userProfiles.size());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:sql/init_db.sql" })
    public void createUserProfileForNonExistentJurisdiction() throws Exception {
        // Given the Jurisdiction JURISDICTION_ID_1 does not exist
        // When attempting to create a User Profile for the above Jurisdiction
        UserProfile userProfile = createUserProfile("user@hmcts.net",
                                                    "defaultCaseType",
                                                    JURISDICTION_ID_1,
                                                    "defaultCaseState");
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(JURISDICTION_ID_1);
        userProfile.addJurisdiction(jurisdiction);
        mockMvc.perform(
            put(SAVE_USER_PROFILE)
                .contentType(contentType)
                .content(mapper.writeValueAsBytes(userProfile)))
            .andExpect(status().is(200));

        // Then the Jurisdiction "TEST1" is created
        assertEquals(1,
            template.queryForObject(COUNT_JURISDICTION_QUERY, Integer.class, JURISDICTION_ID_1).intValue());

        // And the User Profile is created
        final UserProfileEntity userProfileEntity =
            template.queryForObject(GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user@hmcts.net");
        assertEquals("user@hmcts.net", userProfileEntity.getId());
        assertEquals("TEST1", userProfileEntity.getWorkBasketDefaultJurisdiction());

        final int
            auditRows =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'CREATE' and user_profile_id = 'user@hmcts.net'");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void updateUserProfileForNonExistentJurisdiction() throws Exception {
        // Given the Jurisdiction "TEST4" does not exist
        // When attempting to create a User Profile, for an existing user, for the above Jurisdiction
        UserProfile userProfile = createUserProfile("user1", null, "TEST4", null);
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST4");
        userProfile.addJurisdiction(jurisdiction);
        mockMvc.perform(
            put(SAVE_USER_PROFILE)
                .contentType(contentType)
                .content(mapper.writeValueAsBytes(userProfile)))
            .andExpect(status().is(200));

        // Then the Jurisdiction "TEST4" is created
        assertEquals(1,
            template.queryForObject(COUNT_JURISDICTION_QUERY, Integer.class, "TEST4").intValue());

        // And the User Profile is updated; the user should now belong to four Jurisdictions instead of three
        final UserProfileEntity userProfileEntity =
            template.queryForObject(GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user1");
        assertEquals("user1", userProfileEntity.getId());
        assertEquals("TEST4", userProfileEntity.getWorkBasketDefaultJurisdiction());
        assertEquals(4,
            template.queryForObject(COUNT_USER_PROFILE_ALL_JURISDICTIONS_QUERY, Integer.class, "user1")
                .intValue());

        final int auditRowRead = JdbcTestUtils.countRowsInTableWhere(template,
                                                                     "user_profile_audit",
                                                                     "action = 'READ' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 1, auditRowRead);

        final int auditRowUpdate = JdbcTestUtils.countRowsInTableWhere(template,
                                                                     "user_profile_audit",
                                                                     "action = 'UPDATE' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 1, auditRowUpdate);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void updateUserProfileForExistingUserAndJurisdiction() throws Exception {
        // Given the Jurisdiction "TEST3" exists
        // When attempting to create a User Profile, for an existing user, for the above Jurisdiction
        UserProfile userProfile = createUserProfile("user4", "defaultCaseType", JURISDICTION_ID_3, "defaultCaseState");
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(JURISDICTION_ID_3);
        userProfile.addJurisdiction(jurisdiction);
        mockMvc.perform(
            put(SAVE_USER_PROFILE)
                .contentType(contentType)
                .content(mapper.writeValueAsBytes(userProfile)))
            .andExpect(status().is(200));

        // Then no new Jurisdiction is created (the total number remains the same)
        assertEquals(3,
            template.queryForObject(COUNT_ALL_JURISDICTIONS_QUERY, Integer.class).intValue());

        // And the User Profile is updated; the user should now belong to two Jurisdictions instead of one
        final UserProfileEntity userProfileEntity =
            template.queryForObject(GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user4");
        assertEquals("user4", userProfileEntity.getId());
        assertEquals("TEST3", userProfileEntity.getWorkBasketDefaultJurisdiction());
        assertEquals(2,
            template.queryForObject(COUNT_USER_PROFILE_ALL_JURISDICTIONS_QUERY, Integer.class, "user4")
                .intValue());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void updateUserProfileWithSameJurisdiction() throws Exception {
        // Given the Jurisdiction "TEST2" exists, and the user "user1" already belongs to that Jurisdiction
        // When attempting to update the User Profile for that user, with Jurisdiction "TEST2"
        UserProfile userProfile = createUserProfile("user1", null, "TEST2", null);
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("TEST2");
        userProfile.addJurisdiction(jurisdiction);
        final MvcResult mvcResult = mockMvc.perform(
            put(SAVE_USER_PROFILE)
                .contentType(contentType)
                .content(mapper.writeValueAsBytes(userProfile)))
            .andReturn();

        // Then an HTTP 400 (Bad Request) status should be returned
        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message", "User with ID user1 is already a member of the "
            + "TEST2 jurisdiction", mvcResult.getResponse().getContentAsString());

        final int auditRowRead = JdbcTestUtils.countRowsInTableWhere(template,
                                                                       "user_profile_audit",
                                                                       "action = 'READ' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 0, auditRowRead);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void deleteJurisdictionThatIsNotWorkbasketDefault() throws Exception {
        // Given the user "user1" belongs to three Jurisdictions: "TEST1", "TEST2", and "TEST3"
        // And the Jurisdiction "TEST1" is NOT the user's Workbasket default Jurisdiction
        // When deleting the Jurisdiction "TEST1" from the User Profile
        mockMvc.perform(
            delete(DELETE_NON_DEFAULT_JURISDICTION))
            .andExpect(status().is(204));

        // Then the user should now belong to two Jurisdictions instead of three
        final UserProfileEntity userProfileEntity =
            template.queryForObject(GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user1");
        assertEquals("user1", userProfileEntity.getId());
        assertEquals("TEST2", userProfileEntity.getWorkBasketDefaultJurisdiction());
        assertEquals(2,
            template.queryForObject(COUNT_USER_PROFILE_ALL_JURISDICTIONS_QUERY, Integer.class, "user1")
                .intValue());

        final int auditRowRead = JdbcTestUtils.countRowsInTableWhere(template,
                                                                     "user_profile_audit",
                                                                     "action = 'READ' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 1, auditRowRead);

        final int
            auditRowUpdate =
            JdbcTestUtils.countRowsInTableWhere(template,
                                                "user_profile_audit",
                                                "action = 'DELETE' and user_profile_id = 'user1'");
        assertEquals("Unexpected number of audit roles", 1, auditRowUpdate);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void deleteJurisdictionThatIsWorkbasketDefault() throws Exception {
        // Given the user "user1" belongs to three Jurisdictions: "TEST1", "TEST2", and "TEST3"
        // And the Jurisdiction "TEST2" is the user's Workbasket default Jurisdiction
        // When deleting the Jurisdiction "TEST2" from the User Profile
        final MvcResult mvcResult = mockMvc.perform(
            delete(DELETE_DEFAULT_JURISDICTION))
            .andReturn();

        // Then an HTTP 400 (Bad Request) status should be returned
        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message", DELETE_WORKBASKET_DEFAULT_JURISDICTION_ERROR,
            mvcResult.getResponse().getContentAsString());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void deleteJurisdictionThatIsNotInUserProfile() throws Exception {
        // Given the user "user5" belongs to one Jurisdiction: "TEST2"
        // When deleting the Jurisdiction "TEST1" from the User Profile
        final MvcResult mvcResult = mockMvc.perform(
            delete(DELETE_JURISDICTION_NOT_IN_USER_LIST))
            .andReturn();

        // Then an HTTP 400 (Bad Request) status should be returned
        assertEquals("Unexpected response status", 400, mvcResult.getResponse().getStatus());
        assertEquals("Unexpected response message", "User with ID user5 is not a member of the TEST1 "
            + "jurisdiction", mvcResult.getResponse().getContentAsString());

        final int auditRows = JdbcTestUtils.countRowsInTable(template, "user_profile_audit");
        assertEquals("Unexpected number of audit roles", 0, auditRows);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, // checkstyle line break
        scripts = { "classpath:sql/init_db.sql", "classpath:sql/create_user_profile.sql" })
    public void deleteSoleJurisdictionForUserProfile() throws Exception {
        // Given the user "user5" belongs to one Jurisdiction: "TEST2"
        // When deleting the Jurisdiction "TEST2" from the User Profile
        mockMvc.perform(
            delete(DELETE_SOLE_JURISDICTION))
            .andExpect(status().is(204));

        // Then the user should now belong to zero Jurisdictions instead of one
        // And all Workbasket defaults should be null
        final UserProfileEntity userProfileEntity =
            template.queryForObject(GET_USER_PROFILE_QUERY, this::mapUserProfileData, "user5");
        assertEquals("user5", userProfileEntity.getId());
        assertEquals(0,
            template.queryForObject(COUNT_USER_PROFILE_ALL_JURISDICTIONS_QUERY, Integer.class, "user5")
                .intValue());
        assertNull(userProfileEntity.getWorkBasketDefaultJurisdiction());
        assertNull(userProfileEntity.getWorkBasketDefaultCaseType());
        assertNull(userProfileEntity.getWorkBasketDefaultState());

        final int auditRows = JdbcTestUtils.countRowsInTableWhere(template,
                                                                  "user_profile_audit",
                                                                  "action = 'DELETE' and user_profile_id = 'user5'");
        assertEquals("Unexpected number of audit roles", 1, auditRows);
    }

    private static UserProfile createUserProfile(final String id,
                                                 final String caseType,
                                                 final String jurisdiction,
                                                 final String state) {
        final UserProfile userDefault = new UserProfile();
        userDefault.setId(id.toLowerCase(Locale.UK));
        userDefault.setWorkBasketDefaultCaseType(caseType);
        userDefault.setWorkBasketDefaultJurisdiction(jurisdiction);
        userDefault.setWorkBasketDefaultState(state);
        return userDefault;
    }

    private void populateUserProfileDefaults(final UserProfile userProfile, final String defaultJurisdiction) {
        userProfile.setWorkBasketDefaultJurisdiction(defaultJurisdiction);
        userProfile.setWorkBasketDefaultCaseType("Default-Case-Type");
        userProfile.setWorkBasketDefaultState("Default-state");
    }

}
