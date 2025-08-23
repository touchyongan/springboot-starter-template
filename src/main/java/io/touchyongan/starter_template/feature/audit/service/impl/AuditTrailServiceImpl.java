package io.touchyongan.starter_template.feature.audit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.touchyongan.starter_template.common.data.CustomPage;
import io.touchyongan.starter_template.feature.audit.annotation.RegisterEntityAction;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailData;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailFilter;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailFilterTemplate;
import io.touchyongan.starter_template.feature.audit.entity.ActionStatus;
import io.touchyongan.starter_template.feature.audit.entity.AuditTrail;
import io.touchyongan.starter_template.feature.audit.repository.AuditTrailRepository;
import io.touchyongan.starter_template.feature.audit.service.AuditTrailService;
import io.touchyongan.starter_template.feature.user.entity.AppUser;
import io.touchyongan.starter_template.feature.user.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@AllArgsConstructor
@Slf4j
public class AuditTrailServiceImpl implements AuditTrailService {
    private final AuditTrailRepository auditTrailRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final RegisterEntityAction registerEntityAction;

    @Transactional(readOnly = true)
    public AuditTrailFilterTemplate getFilterTemplate() {
        final var statuses = Arrays.stream(ActionStatus.values())
                .filter(s -> s != ActionStatus.UNKNOWN)
                .map(ActionStatus::getStatus)
                .toList();
        final var usernames = appUserRepository
                .findBySpecificationWithListOfSingleFieldProjection(null, String.class, AppUser.class, "username", Sort.Direction.ASC);
        return new AuditTrailFilterTemplate()
                .setEntityActions(registerEntityAction.getEntityAction())
                .setStatus(statuses)
                .setUsername(usernames);
    }

    @Transactional(readOnly = true)
    public CustomPage<AuditTrailData> getAllAuditTrails(final AuditTrailFilter filter) {
        final var spec = filter.getSpecification();
        final var pageable = filter.getPageable();
        return auditTrailRepository.findAllWithSpecification(spec, pageable, AuditTrailData.class, AuditTrail.class);
    }

    @Transactional(readOnly = true)
    public AuditTrailData getAuditTrailById(final Long auditTrailId) {
        return auditTrailRepository.findByIdWithProjection(auditTrailId, AuditTrailData.class, AuditTrail.class);
    }

    @Async
    @Override
    public void saveAuditLogAsync(final AuditTrail auditTrail) {
        try {
            auditTrailRepository.save(auditTrail);
        } catch (final Exception e) {
            var logJson = "";
            try {
                logJson = objectMapper.writeValueAsString(auditTrail);
            } catch (final JsonProcessingException ex) {
                log.warn("Error convert audit trail to json: ", e);
                throw new RuntimeException(ex);
            }
            log.warn("Error save audit trail: {}", logJson, e);
        }
    }
}
