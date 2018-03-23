package uk.gov.hmcts.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

@Component
public class AppInsights extends AbstractAppInsights {
    private static final String PROFILE = "USER_PROFILE";

    public AppInsights(TelemetryClient telemetry) {
        super(telemetry);
    }

    public void trackRequest(java.time.Duration duration, boolean success) {
        RequestTelemetry rt = new RequestTelemetry();
        rt.setName(PROFILE);
        rt.setSuccess(success);
        rt.setDuration(new Duration(duration.toMillis()));
        telemetry.trackRequest(rt);
    }

    public void trackException(Exception e) {
        telemetry.trackException(e);
    }
}
