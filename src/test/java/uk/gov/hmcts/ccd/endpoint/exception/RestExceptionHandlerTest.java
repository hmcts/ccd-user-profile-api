package uk.gov.hmcts.ccd.endpoint.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.CreateUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindUserProfileOperation;
import uk.gov.hmcts.ccd.AppInsights;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;
import uk.gov.hmcts.ccd.endpoint.userprofile.UserProfileEndpoint;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestExceptionHandlerTest {

    private static final String TEST_URL = "/user-profile/users";

    private static final String SQL_EXCEPTION_MESSAGE =
        "SQL Exception thrown during API operation";
    private static final String ERROR_RESPONSE_BODY =
        "{\"errorMessage\":\"SQL Exception thrown during API operation\"}";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));

    @Mock
    private CreateUserProfileOperation createUserProfileOperation;
    @Mock
    private UserProfileOperation userProfileOperation;
    @Mock
    private FindUserProfileOperation findUserProfileOperation;
    @Mock
    private AppInsights appInsights;

    private RestExceptionHandler classUnderTest;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        initMocks(this);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        classUnderTest = new RestExceptionHandler(appInsights);
        final UserProfileEndpoint controller = new UserProfileEndpoint(
            createUserProfileOperation, userProfileOperation, findUserProfileOperation, appInsights);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(classUnderTest)
            .build();
    }

    @Test
    public void handleSQLException_directCall() {
        SQLException exceptionThrown = new SQLException();
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleSQLException(exceptionThrown, request);

        verify(appInsights).trackException(exceptionThrown);
        assertEquals(500, result.getStatusCodeValue());
        assertEquals(Collections.singletonMap("errorMessage", SQL_EXCEPTION_MESSAGE), result.getBody());
    }

    @Test
    public void handleSQLException_shouldReturnHttpErrorResponse() throws Exception {
        UserProfile userProfile = new UserProfile();
        userProfile.setId("user1");

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("TEST1");

        userProfile.addJurisdiction(jurisdiction1);
        userProfile.setWorkBasketDefaultJurisdiction("TEST1");
        userProfile.setWorkBasketDefaultCaseType("Default-Case-Type");
        userProfile.setWorkBasketDefaultState("Default-state");

        doAnswer(
            apiCall -> {
                throw new SQLException();
            })
            .when(createUserProfileOperation)
            .execute(any(), eq("<UNKNOWN>"));

        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders
                .post(TEST_URL)
                .contentType(contentType)
                .header("actionedBy", "<UNKNOWN>")
                .content(mapper.writeValueAsBytes(userProfile)));

        result.andExpect(status().isInternalServerError());
        result.andExpect(content()
            .string(ERROR_RESPONSE_BODY));
    }
}
