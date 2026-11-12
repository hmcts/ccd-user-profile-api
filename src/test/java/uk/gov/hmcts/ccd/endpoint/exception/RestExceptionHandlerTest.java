package uk.gov.hmcts.ccd.endpoint.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

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
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.CreateUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.FindUserProfileOperation;
import uk.gov.hmcts.ccd.domain.service.UserProfileOperation;
import uk.gov.hmcts.ccd.endpoint.userprofile.UserProfileEndpoint;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
        assertEquals(ERROR_MESSAGE, result.getBody());
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
        result.andExpect(content().string(ERROR_MESSAGE));
        result.andExpect(header().string("Message", ERROR_MESSAGE));
    }

    @Test
    public void handleNotFoundException_directCall() {
        NotFoundException exceptionThrown = new NotFoundException(ERROR_MESSAGE);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleException(exceptionThrown, request);

        assertEquals(404, result.getStatusCodeValue());
        assertEquals(ERROR_MESSAGE, result.getBody());
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
        result.andExpect(content().string(ERROR_MESSAGE));
        result.andExpect(header().string("Message", ERROR_MESSAGE));
    }

    @Test
    public void handleException_directCall() {
        RuntimeException exceptionThrown = new RuntimeException(ERROR_MESSAGE);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleException(exceptionThrown, request);

        assertEquals(500, result.getStatusCodeValue());
        assertEquals("Internal Server Error", result.getBody());
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
        result.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
            .string("Internal Server Error"));
        result.andExpect(jsonPath("$.code").doesNotExist());
        result.andExpect(header().doesNotExist("Message"));
    }

    @Test
    public void handleSQLException_directCall() {
        SQLException exceptionThrown = new SQLException();
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Object> result = classUnderTest.handleSQLException(exceptionThrown, request);

        assertEquals(500, result.getStatusCodeValue());
        assertEquals("Internal Server Error", ((Map<?, ?>) result.getBody()).get("errorMessage"));
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
        result.andExpect(jsonPath("$.errorMessage").value("Internal Server Error"));
        result.andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void preservesCreationValidationThroughHttp() throws Exception {
        UserProfileEndpoint endpoint = new UserProfileEndpoint(
            new CreateUserProfileOperation(mock(UserProfileRepository.class)),
            userProfileOperation, findUserProfileOperation);
        MockMvc validationMvc = MockMvcBuilders.standaloneSetup(endpoint)
            .setControllerAdvice(classUnderTest).build();
        String[][] cases = {
            {"{}", "A User Profile must have an Id"},
            {"{\"id\":\"user1\"}", "A User Profile must have at least one associated Jurisdiction"},
            {"{\"id\":\"user1\",\"jurisdictions\":[]}",
                "A User Profile must have at least one associated Jurisdiction"},
            {"{\"id\":\"user1\",\"jurisdictions\":[{}]}", "A Jurisdiction must have an Id"}
        };
        for (String[] validation : cases) {
            validationMvc.perform(MockMvcRequestBuilders.post(TEST_URL)
                    .contentType(contentType).content(validation[0]))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(validation[1]))
                .andExpect(header().string("Message", validation[1]));
        }
    }

    @Test
    void retainsOriginalExceptionAtInfoWithoutLeakingItInTheResponse() {
        Logger logger = (Logger) LoggerFactory.getLogger(RestExceptionHandler.class);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            logger.setLevel(Level.INFO);
            RuntimeException original = new RuntimeException("PRIVATE support diagnostic");
            ResponseEntity<Object> result = classUnderTest.handleException(original, mock(WebRequest.class));
            assertEquals(500, result.getStatusCode().value());
            assertEquals("PRIVATE support diagnostic", appender.list.get(0).getThrowableProxy().getMessage());
            assertEquals(false, result.getBody().toString().contains("PRIVATE"));
        } finally {
            logger.setLevel(previousLevel);
            logger.detachAppender(appender);
            appender.stop();
        }
    }

}
