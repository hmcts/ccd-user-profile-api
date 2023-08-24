package uk.gov.hmcts.ccd.endpoint.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.CreateUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindUserProfileOperation;
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
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RestExceptionHandlerTest {

    private static final String TEST_URL = "/user-profile/users";

    private static final String ERROR_MESSAGE = "Error during execution";
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

    private RestExceptionHandler classUnderTest;

    private UserProfile userProfile;

    private MockMvc mockMvc;

    private ServletWebRequest committedResponse;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("TEST1");

        userProfile = new UserProfile();
        userProfile.setId("user1");
        userProfile.addJurisdiction(jurisdiction1);
        userProfile.setWorkBasketDefaultJurisdiction("TEST1");
        userProfile.setWorkBasketDefaultCaseType("Default-Case-Type");
        userProfile.setWorkBasketDefaultState("Default-state");

        classUnderTest = new RestExceptionHandler();
        final UserProfileEndpoint controller = new UserProfileEndpoint(
            createUserProfileOperation, userProfileOperation, findUserProfileOperation);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(classUnderTest)
            .build();
    }

    @Test
    public void handleBadRequestException_directCall() {
        BadRequestException exceptionThrown = new BadRequestException(ERROR_MESSAGE);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleException(exceptionThrown, request);

        assertEquals(400, result.getStatusCodeValue());
        assertEquals(ERROR_MESSAGE, result.getBody().toString());
    }

    @Test
    public void handleBadRequestException_shouldReturnHttpErrorResponse() throws Exception {
        BadRequestException exceptionThrown = new BadRequestException(ERROR_MESSAGE);

        doAnswer(
            apiCall -> {
                throw exceptionThrown;
            })
            .when(createUserProfileOperation)
            .execute(any(), eq("<UNKNOWN>"));

        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders
                .post(TEST_URL)
                .contentType(contentType)
                .header("actionedBy", "<UNKNOWN>")
                .content(mapper.writeValueAsBytes(userProfile)));

        result.andExpect(status().isBadRequest());
        result.andExpect(content()
            .string(ERROR_MESSAGE));
    }

    @Test
    public void handleNotFoundException_directCall() {
        NotFoundException exceptionThrown = new NotFoundException(ERROR_MESSAGE);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleException(exceptionThrown, request);

        assertEquals(404, result.getStatusCodeValue());
        assertEquals(ERROR_MESSAGE, result.getBody().toString());
    }

    @Test
    public void handleNotFoundException_shouldReturnHttpErrorResponse() throws Exception {
        NotFoundException exceptionThrown = new NotFoundException(ERROR_MESSAGE);

        doAnswer(
            apiCall -> {
                throw exceptionThrown;
            })
            .when(createUserProfileOperation)
            .execute(any(), eq("<UNKNOWN>"));

        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders
                .post(TEST_URL)
                .contentType(contentType)
                .header("actionedBy", "<UNKNOWN>")
                .content(mapper.writeValueAsBytes(userProfile)));

        result.andExpect(status().isNotFound());
        result.andExpect(content()
            .string(ERROR_MESSAGE));
    }

    @Test
    public void handleException_directCall() {
        RuntimeException exceptionThrown = new RuntimeException(ERROR_MESSAGE);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleException(exceptionThrown, request);

        assertEquals(500, result.getStatusCodeValue());
        assertEquals(ERROR_MESSAGE, result.getBody().toString());
    }

    @Test
    public void handleException_shouldReturnHttpErrorResponse() throws Exception {
        RuntimeException exceptionThrown = new RuntimeException(ERROR_MESSAGE);

        doAnswer(
            apiCall -> {
                throw exceptionThrown;
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
            .string(ERROR_MESSAGE));
    }

    @Test
    public void handleSQLException_directCall() {
        SQLException exceptionThrown = new SQLException();
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleSQLException(exceptionThrown, request);

        assertEquals(500, result.getStatusCodeValue());
        assertEquals(Collections.singletonMap("errorMessage", SQL_EXCEPTION_MESSAGE), result.getBody());
    }

    @Test
    public void handleSQLException_shouldReturnHttpErrorResponse() throws Exception {
        SQLException exceptionThrown = new SQLException();

        doAnswer(
            apiCall -> {
                throw exceptionThrown;
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
