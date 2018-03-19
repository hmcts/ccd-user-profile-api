package uk.gov.hmcts.ccd.userprofile.tests.helper.idam;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Base64.getEncoder;

public class IdamHelper {

    private final Map<String, AuthenticatedUser> users = new HashMap<>();

    private final IdamApi idamApi;

    public IdamHelper(String idamBaseUrl) {
        idamApi = Feign.builder()
                       .encoder(new JacksonEncoder())
                       .decoder(new JacksonDecoder())
                       .target(IdamApi.class, idamBaseUrl);
    }

    public AuthenticatedUser authenticate(final String email, final String password) {
        return users.computeIfAbsent(email, e -> {
            final String basicAuth = getBasicAuthHeader(email, password);
            final IdamApi.AuthenticateUserResponse authResponse = idamApi.authenticateUser(basicAuth);
            final IdamApi.IdamUser user = idamApi.getUser(authResponse.getAccessToken());

            return new AuthenticatedUser(user.getId(), email, authResponse.getAccessToken(), user.getRoles());
        });
    }

    private String getBasicAuthHeader(final String username, final String password) {
        final String auth = username + ":" + password;
        return new String(getEncoder().encode(auth.getBytes(defaultCharset())), defaultCharset());
    }
}
