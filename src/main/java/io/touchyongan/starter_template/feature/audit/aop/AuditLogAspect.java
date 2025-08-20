package io.touchyongan.starter_template.feature.audit.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.touchyongan.starter_template.common.util.AuthUtil;
import io.touchyongan.starter_template.common.util.RequestContextUtil;
import io.touchyongan.starter_template.feature.audit.entity.ActionStatus;
import io.touchyongan.starter_template.feature.audit.entity.AuditTrail;
import io.touchyongan.starter_template.feature.audit.service.AuditTrailService;
import io.touchyongan.starter_template.infrastructure.permission.CustomPreAuthorize;
import io.touchyongan.starter_template.config.properties.MaskPIIInfoProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.touchyongan.starter_template.common.util.RequestContextUtil.*;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {
    private static final Set<String> UNWANTED_AUDIT_PATH = Set.of("/v3/api-docs");

    private final MaskPIIInfoProperties maskPIIInfoProperties;
    private final ObjectMapper objectMapper;
    private final AuditTrailService auditTrailService;

    @Around(value = "within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
        final var servletAttribute = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(servletAttribute, "servletAttribute is null");
        final var request = servletAttribute.getRequest();
        final var auditTrail = new AuditTrail();
        auditTrail.setStatus(ActionStatus.SUCCESS);
        try {
            // Gather information before process request for log
            populateValueFromRequest(request, auditTrail);
            populateValueFromAnnotation(joinPoint, auditTrail);

            // Process request
            final var result = joinPoint.proceed();

            // gather information what effect in system
            populateValueFromContext(auditTrail);

            return result;
        } catch (final Exception e) {
            auditTrail.setStatus(ActionStatus.FAILURE);
            auditTrail.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            final var traceId = MDC.get("UUID");
            auditTrail.setTraceId(traceId);
            if (!isIgnoreSave(joinPoint, request)) {
                auditTrailService.saveAuditLogAsync(auditTrail);
            }
            RequestContextUtil.clear();
        }
    }

    private boolean isIgnoreSave(final ProceedingJoinPoint joinPoint,
                                 final HttpServletRequest request) {
        final var methodSignature = (MethodSignature) joinPoint.getSignature();
        final var targetMethod = methodSignature.getMethod();
        final var ignoreSaveAuditTrail = targetMethod.getAnnotation(IgnoreSaveAuditTrail.class);
        final var getMapping = targetMethod.getAnnotation(GetMapping.class);
        return Objects.nonNull(getMapping) ||
                Objects.nonNull(ignoreSaveAuditTrail) ||
                isIgnoreUnwantedAuditPath(request);
    }

    private boolean isIgnoreUnwantedAuditPath(final HttpServletRequest request) {
        return UNWANTED_AUDIT_PATH.stream()
                .anyMatch(ignorePath -> request.getRequestURI().startsWith(ignorePath));
    }

    private void populateValueFromRequest(final HttpServletRequest request,
                                          final AuditTrail auditTrail) throws IOException {
        final var params = getParameters(request);
        final var requestUrl = request.getRequestURI();
        final var requestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (StringUtils.hasText(requestBody)) {
            final var map = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {
            });
            for (final var maskField : maskPIIInfoProperties.getRequestBodyFields()) {
                if (map.containsKey(maskField)) {
                    map.put(maskField, "******");
                }
            }
            auditTrail.setRequestBody(map);
        }

        auditTrail.setHttpMethod(request.getMethod());
        auditTrail.setIp(RequestContextUtil.getIPAddress(request));
        auditTrail.setRequestUrl(requestUrl);
        auditTrail.setRequestParams(params);
    }

    private void populateValueFromAnnotation(final ProceedingJoinPoint joinPoint,
                                             final AuditTrail auditTrail) {
        final var methodSignature = (MethodSignature) joinPoint.getSignature();
        final var targetMethod = methodSignature.getMethod();
        final var customPreAuthorize = targetMethod.getAnnotation(CustomPreAuthorize.class);
        if (Objects.nonNull(customPreAuthorize)) {
            auditTrail.setAction(customPreAuthorize.action().toUpperCase());
            auditTrail.setEntity(customPreAuthorize.entity().toUpperCase());
        } else {
            final var logActionAnonymous = targetMethod.getAnnotation(LogActionAnonymous.class);
            if (Objects.nonNull(logActionAnonymous)) {
                auditTrail.setAction(logActionAnonymous.action().toUpperCase());
                auditTrail.setEntity(logActionAnonymous.entity().toUpperCase());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void populateValueFromContext(final AuditTrail auditTrail) {
        final var currentUser = AuthUtil.getCurrentUser();
        final var newValue = (Map<String, Object>) Optional.ofNullable(RequestContextUtil.get(KEY_NEW_VALUE)).orElse(Map.of());
        final var oldValue = (Map<String, Object>) Optional.ofNullable(RequestContextUtil.get(KEY_OLD_VALUE)).orElse(Map.of());
        final var resourceId = Optional.ofNullable(RequestContextUtil.get(KEY_RESOURCE_ID))
                .map(o -> Long.valueOf(o.toString()))
                .orElse(null);
        if (Objects.nonNull(currentUser)) {
            auditTrail.setUserId(currentUser.getId());
            auditTrail.setUsername(AuthUtil.getCurrentUsername());
        }
        auditTrail.setOldValue(oldValue);
        auditTrail.setNewValue(newValue);
        auditTrail.setResourceId(resourceId);
    }

    private String getParameters(final HttpServletRequest request) {
        final var params = new StringBuilder();
        if (isMultiPartContent(request)) {
            try {
                final var parts = request.getParts();
                for (final var part : parts) {
                    params.append(part.getName())
                            .append("=");
                    if (Objects.isNull(part.getContentType())) {
                        params.append(request.getParameter(part.getName()));
                    } else {
                        params.append(part.getSubmittedFileName());
                    }
                    params.append("&");
                }
            } catch (final IOException | ServletException e) {
                log.warn("Error get multipart...", e);
            }
        }
        final var parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final var paramName = parameterNames.nextElement();
            final String[] paramValues;
            if (maskPIIInfoProperties.getRequestBodyFields().contains(paramName)) {
                paramValues = new String[] { "masked" };
            } else {
                paramValues = request.getParameterValues(paramName);
            }
            for (final var paramValue : paramValues) {
                params.append(paramName)
                        .append("=")
                        .append(paramValue)
                        .append("&");
            }
        }
        return params.toString();
    }

    private boolean isMultiPartContent(final HttpServletRequest request) {
        return Objects.nonNull(request.getContentType()) &&
                request.getContentType().toLowerCase().startsWith("multipart/form-data");
    }

}
