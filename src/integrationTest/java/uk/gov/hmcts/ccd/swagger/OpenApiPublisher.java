package uk.gov.hmcts.ccd.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.ccd.BaseTest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OpenApiPublisher extends BaseTest {

    @DisplayName("Generate OpenAPI documentation")
    @Test
    public void generateDocs() throws Exception {
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs"));
        byte[] specs = perform
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/swagger-specs.json"))) {
            outputStream.write(specs);
        }

    }
}
