package io.touchyongan.starter_template.feature.audit.controller;

import io.touchyongan.starter_template.common.data.ApiResponse;
import io.touchyongan.starter_template.common.data.CustomPage;
import io.touchyongan.starter_template.common.specification.PaginationRequest;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailConstant;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailData;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailFilter;
import io.touchyongan.starter_template.feature.audit.data.AuditTrailFilterTemplate;
import io.touchyongan.starter_template.feature.audit.service.AuditTrailService;
import io.touchyongan.starter_template.infrastructure.permission.CustomPreAuthorize;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("audit-trails")
@AllArgsConstructor
public class AuditTrailController {
    private final AuditTrailService auditTrailService;

    @GetMapping(value = "/template", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AuditTrailFilterTemplate>> getFilterTemplate() {
        final var result = auditTrailService.getFilterTemplate();
        final var response = new ApiResponse<>(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @CustomPreAuthorize(action = AuditTrailConstant.READ, entity = AuditTrailConstant.ENTITY)
    public ResponseEntity<ApiResponse<CustomPage<AuditTrailData>>> getAllAuditTrails(
            @RequestParam(name = "action", required = false) final String action,
            @RequestParam(value = "entity", required = false) final String entity,
            @RequestParam(value = "resourceId", required = false) final List<Long> resourceId,
            @RequestParam(value = "username", required = false) final String username,
            @RequestParam(value = "status", required = false) final String status,
            @RequestParam(value = "startDate", required = false) final LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) final LocalDateTime endDate,
            @RequestParam(value = "sortFields", required = false) List<String> sortFields,
            @RequestParam(value = "sortDirections", required = false) List<String> sortDirections,
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "size", defaultValue = "10") final int size
    ) {
        sortFields = Optional.ofNullable(sortFields).orElse(List.of("id"));
        sortDirections = Optional.ofNullable(sortDirections).orElse(List.of("DESC"));
        final var pageRequest = PaginationRequest.newPaginationRequest(page, size, sortFields, sortDirections);
        PaginationRequest.validateValidFields(AuditTrailFilter.SUPPORTED_FIELDS, sortFields);
        AuditTrailFilter.validateStatus(status);
        final var filter = new AuditTrailFilter();
        filter.setPaginationRequest(pageRequest);
        filter.setEntity(entity);
        filter.setAction(action);
        filter.setUsername(username);
        filter.setResourceIds(resourceId);
        filter.setStatus(status);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        final var result = auditTrailService.getAllAuditTrails(filter);
        final var response = new ApiResponse<>(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "{auditTrailId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CustomPreAuthorize(action = AuditTrailConstant.READ, entity = AuditTrailConstant.ENTITY)
    public ResponseEntity<ApiResponse<AuditTrailData>> getAuditTrailByDetails(@PathVariable("auditTrailId") final Long auditTrailId) {
        final var result = auditTrailService.getAuditTrailById(auditTrailId);
        final var response = new ApiResponse<>(result);
        return ResponseEntity.ok(response);
    }
}
