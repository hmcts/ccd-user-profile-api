package uk.gov.hmcts.ccd.domain.model;

import org.springframework.stereotype.Component;

@Component
public class Jurisdiction {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
