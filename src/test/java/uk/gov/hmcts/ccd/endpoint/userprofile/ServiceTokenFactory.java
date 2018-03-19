package uk.gov.hmcts.ccd.endpoint.userprofile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.auth.provider.service.token.ServiceTokenGenerator;

@Component
public class ServiceTokenFactory {

    private final String baseUrl;
    private final String secretKey;
    private final String microservice;
    private final ServiceTokenGenerator serviceTokenGenerator;

    @Autowired
    public ServiceTokenFactory(@Value("${auth.provider.service.client.baseUrl}") String baseUrl,
                               @Value("${auth.provider.service.client.key}") String secretKey,
                               @Value("${auth.provider.service.client.microservice}") String microservice,
                               ServiceTokenGenerator serviceTokenGenerator) {
        this.baseUrl = baseUrl;
        this.secretKey = secretKey;
        this.microservice = microservice;
        this.serviceTokenGenerator = serviceTokenGenerator;
    }

    public String validTokenForService() {
        return serviceTokenGenerator.generate();
    }
}
