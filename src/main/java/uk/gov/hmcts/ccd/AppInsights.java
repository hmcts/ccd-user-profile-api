package uk.gov.hmcts.ccd;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import org.springframework.stereotype.Component;

@Component
public class AppInsights {
    private static final String PROFILE = "USER_PROFILE";
    private final TelemetryClient telemetry;

    public AppInsights(TelemetryClient telemetry) {
        this.telemetry = telemetry;
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
