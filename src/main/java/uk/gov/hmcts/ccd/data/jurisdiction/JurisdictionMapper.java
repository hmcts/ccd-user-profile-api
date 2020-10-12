package uk.gov.hmcts.ccd.data.jurisdiction;

import uk.gov.hmcts.ccd.domain.model.Jurisdiction;

public final class JurisdictionMapper {

    private JurisdictionMapper() {
    }

    public static Jurisdiction entityToModel(JurisdictionEntity jurisdictionEntity) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId(jurisdictionEntity.getId());
        return jurisdiction;
    }

    public static JurisdictionEntity modelToEntity(Jurisdiction jurisdiction) {
        JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
        jurisdictionEntity.setId(jurisdiction.getId());
        return jurisdictionEntity;
    }
}
