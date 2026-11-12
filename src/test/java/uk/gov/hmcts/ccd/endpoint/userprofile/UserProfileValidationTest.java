package uk.gov.hmcts.ccd.endpoint.userprofile;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.data.userprofile.UserProfileRepository;
import uk.gov.hmcts.ccd.domain.model.Jurisdiction;
import uk.gov.hmcts.ccd.domain.model.UserProfile;
import uk.gov.hmcts.ccd.domain.service.DeleteUserProfileJurisdictionOperation;
import uk.gov.hmcts.ccd.endpoint.exception.RestExceptionHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileValidationTest {
    @Test
    void explainsHowToResolveWorkbasketDefaultDeletionThroughHttp() throws Exception {
        final UserProfileRepository repository = mock(UserProfileRepository.class);
        UserProfile profile = new UserProfile();
        profile.setId("user1");
        profile.addJurisdiction(new Jurisdiction("TEST1"));
        profile.addJurisdiction(new Jurisdiction("TEST2"));
        profile.setWorkBasketDefaultJurisdiction("TEST1");
        when(repository.findById("user1", "<UNKNOWN>")).thenReturn(profile);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new UserProfileController(
                null, null, null, new DeleteUserProfileJurisdictionOperation(repository)))
            .setControllerAdvice(new RestExceptionHandler()).build();
        String message = "Cannot delete user profile as the user's workbasket "
            + "defaults are set to the Jurisdiction the user is being deleted from. "
            + "Please update the user's workbasket "
            + "default values to another Jurisdiction and try again.";

        mvc.perform(delete("/users").param("uid", "user1").param("jid", "TEST1"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(message))
            .andExpect(header().string("Message", message));
    }
}
