package uk.gov.hmcts.ccd.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.ApplicationParams;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.model.UserProfileCollection;
import uk.gov.hmcts.ccd.endpoint.exception.BadRequestException;
import uk.gov.hmcts.ccd.endpoint.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static java.util.Collections.singletonList;

class FindUserProfileOperationTest {

    private static final String TEST_EMAIL_1 = "test1@example.com";
    private static final String TEST_JURISDICTION_1 = "JURISDICTION-1";
    private static final String TEST_CASE_TYPE_1 = "CASE-TYPE-1";

    private static final String TEST_EMAIL_2 = "test2@example.com";
    private static final String TEST_JURISDICTION_2 = "JURISDICTION-2";
    private static final String TEST_CASE_TYPE_2 = "CASE-TYPE-2";

    private static final String TEST_ENCODED_EMAIL = "test2%40example.com";
    private static final String TEST_UNKNOWN_EMAIL = "test3@example.com";
    private static final String TEST_INVALID_EMAIL = "test4.example.com";
    private static final String TEST_ACTIONED_BY_EMAIL = "pf4sd59ykc@example.com";

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ApplicationParams applicationParams;

    private FindUserProfileOperation findUserProfileOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        findUserProfileOperation = new FindUserProfileOperation(userProfileRepository, applicationParams);

        final UserProfile userProfile1 = createUserProfile(TEST_EMAIL_1, TEST_JURISDICTION_1, TEST_CASE_TYPE_1);
        final UserProfile userProfile2 = createUserProfile(TEST_EMAIL_2, TEST_JURISDICTION_2, TEST_CASE_TYPE_2);

        when(userProfileRepository.findById(TEST_EMAIL_1, TEST_ACTIONED_BY_EMAIL))
            .thenReturn(userProfile1);
        when(userProfileRepository.findById(TEST_EMAIL_2, TEST_ACTIONED_BY_EMAIL))
            .thenReturn(userProfile2);
        when(userProfileRepository.findById(TEST_UNKNOWN_EMAIL, TEST_ACTIONED_BY_EMAIL))
            .thenReturn(null);

        when(userProfileRepository.findAllByIds(asList(TEST_EMAIL_1, TEST_EMAIL_2), TEST_ACTIONED_BY_EMAIL))
            .thenReturn(asList(userProfile1, userProfile2));
        when(userProfileRepository.findAllByIds(asList(TEST_EMAIL_1, TEST_UNKNOWN_EMAIL), TEST_ACTIONED_BY_EMAIL))
            .thenReturn(singletonList(userProfile1));
        when(userProfileRepository.findAllByIds(singletonList(TEST_UNKNOWN_EMAIL), TEST_ACTIONED_BY_EMAIL))
            .thenReturn(new ArrayList<>());
    }

    @Nested
    class FindUserProfileTests {

        @Test
        void shouldReturnSingleUserProfileForGivenUid() {
            UserProfile userProfile = findUserProfileOperation.execute(TEST_EMAIL_1, TEST_ACTIONED_BY_EMAIL);

            assertEquals(TEST_EMAIL_1, userProfile.getId());
            assertEquals(TEST_JURISDICTION_1, userProfile.getWorkBasketDefaultJurisdiction());
            assertEquals(TEST_CASE_TYPE_1, userProfile.getWorkBasketDefaultCaseType());
        }

        @Test
        void shouldReturnSingleUserProfileForEncodedUid() {
            UserProfile userProfile = findUserProfileOperation.execute(TEST_ENCODED_EMAIL, TEST_ACTIONED_BY_EMAIL);

            assertEquals(TEST_EMAIL_2, userProfile.getId());
            assertEquals(TEST_JURISDICTION_2, userProfile.getWorkBasketDefaultJurisdiction());
            assertEquals(TEST_CASE_TYPE_2, userProfile.getWorkBasketDefaultCaseType());
        }

        @Test
        void shouldReturnNotFoundErrorForUnknownUid() {
            assertThrows(NotFoundException.class,
                () -> findUserProfileOperation.execute(TEST_UNKNOWN_EMAIL, TEST_ACTIONED_BY_EMAIL));
        }

        @Test
        public void shouldReturnBadRequestErrorForNullEmailId() {
            List<String> emailIds = null;
            assertThrows(BadRequestException.class,
                () -> findUserProfileOperation
                    .execute(emailIds, TEST_ACTIONED_BY_EMAIL));
        }

        @Test
        public void shouldReturnBadRequestErrorForNoEmailId() {
            assertThrows(BadRequestException.class,
                () -> findUserProfileOperation
                    .execute(new ArrayList<>(), TEST_ACTIONED_BY_EMAIL));
        }

        @Test
        public void shouldReturnBadRequestErrorForInvalidEmailId() {
            when(applicationParams.isUserProfileEmailValidationEnabled()).thenReturn(true);
            assertThrows(BadRequestException.class,
                () -> findUserProfileOperation
                    .execute(asList(TEST_EMAIL_1, TEST_INVALID_EMAIL), TEST_ACTIONED_BY_EMAIL));
        }

        @Test
        public void shouldReturnUserProfilesForValidEmailIds() {
            UserProfileCollection userProfileList = findUserProfileOperation
                .execute(asList(TEST_EMAIL_1, TEST_EMAIL_2), TEST_ACTIONED_BY_EMAIL);

            List<UserProfile> userProfiles = userProfileList.getUserProfiles();
            assertEquals(2, userProfiles.size());

            assertEquals(TEST_EMAIL_1, userProfiles.get(0).getId());
            assertEquals(TEST_JURISDICTION_1, userProfiles.get(0).getWorkBasketDefaultJurisdiction());
            assertEquals(TEST_CASE_TYPE_1, userProfiles.get(0).getWorkBasketDefaultCaseType());

            assertEquals(TEST_EMAIL_2, userProfiles.get(1).getId());
            assertEquals(TEST_JURISDICTION_2, userProfiles.get(1).getWorkBasketDefaultJurisdiction());
            assertEquals(TEST_CASE_TYPE_2, userProfiles.get(1).getWorkBasketDefaultCaseType());
        }

        @Test
        public void shouldReturnEmptyProfileForSingleUnknownEmailId() {
            UserProfileCollection userProfileList = findUserProfileOperation
                .execute(singletonList(TEST_UNKNOWN_EMAIL), TEST_ACTIONED_BY_EMAIL);

            List<UserProfile> userProfiles = userProfileList.getUserProfiles();
            assertTrue(userProfiles.isEmpty());
        }

        @Test
        public void shouldReturnEmptyProfilesForUnknownEmailIds() {
            UserProfileCollection userProfileList = findUserProfileOperation
                .execute(asList(TEST_EMAIL_1, TEST_UNKNOWN_EMAIL), TEST_ACTIONED_BY_EMAIL);

            List<UserProfile> userProfiles = userProfileList.getUserProfiles();
            assertEquals(1, userProfiles.size());

            assertEquals(TEST_EMAIL_1, userProfiles.get(0).getId());
            assertEquals(TEST_JURISDICTION_1, userProfiles.get(0).getWorkBasketDefaultJurisdiction());
            assertEquals(TEST_CASE_TYPE_1, userProfiles.get(0).getWorkBasketDefaultCaseType());
        }
    }

    private UserProfile createUserProfile(String id, String jurisdiction, String caseType) {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(id);
        userProfile.setWorkBasketDefaultJurisdiction(jurisdiction);
        userProfile.setWorkBasketDefaultCaseType(caseType);
        return userProfile;
    }
}
