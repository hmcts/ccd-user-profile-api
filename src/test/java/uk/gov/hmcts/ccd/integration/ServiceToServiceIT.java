package uk.gov.hmcts.ccd.integration;

import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.UserProfileApplication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.GET;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UserProfileApplication.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(locations = {"classpath:integration_tests.properties","classpath:test.properties"})
@Ignore
public class ServiceToServiceIT {

    private static final String SERVICE_TOKEN = "ServiceToken";
    private static final String INVALID_SERVICE_TOKEN = "InvalidServiceToken";
    private static final String URL_S2S_DETAILS = "/s2s/details";
    private static final String VALID_IDAM_TOKEN = "Bearer UserAuthToken";
    private static final String BEARER = "Bearer ";
    private static final String HEADER_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String APPLICATION_JSON = "application/json";
    private static final String URL_USER_PROFILE = "/user-profile/users";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldPassServiceAuthorizationWhenValidServiceToken() {

        final ResponseEntity<String>
            response =
            restTemplate.exchange(URL_USER_PROFILE, GET, validRequestEntity(), String.class);
        assertHappyPath(response);
        verify(getRequestedFor(urlEqualTo(URL_S2S_DETAILS)).withHeader(AUTHORIZATION,
                                                                       equalTo(BEARER + SERVICE_TOKEN)));
    }

    @Test
    public void shouldFailServiceAuthorizationWhenInvalidServiceToken() {

        stubFor(get(urlEqualTo(URL_S2S_DETAILS)).withHeader(AUTHORIZATION, equalTo(BEARER + INVALID_SERVICE_TOKEN))
                                                .willReturn(aResponse().withStatus(SC_UNAUTHORIZED)));

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_SERVICE_AUTHORIZATION, INVALID_SERVICE_TOKEN);
        headers.add(AUTHORIZATION, VALID_IDAM_TOKEN);
        headers.add(CONTENT_TYPE, APPLICATION_JSON);

        final ResponseEntity<String>
            response =
            restTemplate.exchange(URL_USER_PROFILE, GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCodeValue(), is(SC_FORBIDDEN));
        verify(getRequestedFor(urlEqualTo(URL_S2S_DETAILS)).withHeader(AUTHORIZATION,
                                                                      equalTo(BEARER + INVALID_SERVICE_TOKEN)));
    }

    private HttpEntity<?> validRequestEntity() {
        return new HttpEntity<>(validHeaders());
    }

    private void assertHappyPath(final ResponseEntity<String> response) {
        MatcherAssert.assertThat(response.getStatusCodeValue(), not(401));
        MatcherAssert.assertThat(response.getStatusCodeValue(), not(403));
    }

    private HttpHeaders validHeaders() {
        return buildHeaders(VALID_IDAM_TOKEN);
    }

    private HttpHeaders buildHeaders(final String idamToken) {
        HttpHeaders headers = buildHeaders();
        headers.add(AUTHORIZATION, idamToken);
        return headers;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_SERVICE_AUTHORIZATION, "ServiceToken");
        headers.add(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }
}
