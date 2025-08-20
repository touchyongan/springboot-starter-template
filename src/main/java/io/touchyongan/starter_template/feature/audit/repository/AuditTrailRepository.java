package io.touchyongan.starter_template.feature.audit.repository;

import io.touchyongan.starter_template.common.base.BaseRepository;
import io.touchyongan.starter_template.common.specification.BaseProjectionRepository;
import io.touchyongan.starter_template.feature.audit.entity.AuditTrail;

public interface AuditTrailRepository extends BaseRepository<AuditTrail>, BaseProjectionRepository<AuditTrail> {
}
