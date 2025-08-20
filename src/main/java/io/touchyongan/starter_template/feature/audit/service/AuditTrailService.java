package io.touchyongan.starter_template.feature.audit.service;

import io.touchyongan.starter_template.common.data.CustomPage;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailData;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailFilter;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailFilterTemplate;
import io.touchyongan.starter_template.feature.audit.entity.AuditTrail;

public interface AuditTrailService {

    AuditTrailFilterTemplate getFilterTemplate();

    CustomPage<AuditTrailData> getAllAuditTrails(final AuditTrailFilter filter);

    AuditTrailData getAuditTrailById(final Long auditTrailId);

    void saveAuditLogAsync(final AuditTrail auditTrail);
}
