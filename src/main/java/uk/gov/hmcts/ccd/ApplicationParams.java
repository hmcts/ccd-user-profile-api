package uk.gov.hmcts.ccd;

import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class ApplicationParams {

    @Value("${user-profile.email.id.validation}")
    private Boolean userProfileEmailValidationEnabled;

    public Boolean isUserProfileEmailValidationEnabled() {
        return userProfileEmailValidationEnabled;
    }

}
