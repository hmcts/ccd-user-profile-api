package uk.gov.hmcts.ccd.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthorizedConfiguration {

    @Value("#{'${user-profile.authorised.services}'.split(',')}")
    private List<String> services = new ArrayList<>();

    public List<String> getServices() {
        return this.services;
    }

}
