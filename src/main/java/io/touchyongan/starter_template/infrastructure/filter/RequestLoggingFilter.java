package io.touchyongan.starter_template.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.touchyongan.starter_template.config.properties.MaskPIIInfoProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Note: This class should order before CacheBodyFilter filter, to ensure audit trail can get request body correctly
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    private final ObjectMapper objectMapper;
    private final MaskPIIInfoProperties maskPIIInfoProperties;

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        return "OPTIONS".equals(request.getMethod());
    }

    public RequestLoggingFilter(final ObjectMapper objectMapper,
                                final MaskPIIInfoProperties maskPIIInfoProperties) {
        this.objectMapper = objectMapper;
        this.maskPIIInfoProperties = maskPIIInfoProperties;
        setIncludePayload(true);
        setIncludeQueryString(true);
        setMaxPayloadLength(Integer.MAX_VALUE);
        setIncludeClientInfo(true);
        setIncludeHeaders(false);
        setHeaderPredicate(header -> !maskPIIInfoProperties.getMaskHeaders().contains(header));
    }

    @Override
    protected void beforeRequest(final HttpServletRequest request,
                                 final String message) {
        MDC.put("UUID", UUID.randomUUID().toString());
        MDC.put("start", String.valueOf(System.currentTimeMillis()));
        log.info(message);
    }

    @Override
    protected void afterRequest(final HttpServletRequest request,
                                final String message) {
        final var finished = System.currentTimeMillis() - Long.parseLong(MDC.get("start"));
        MDC.put("finishedRequest", finished + " ms");
        log.info(message);
        MDC.clear();
    }

    @Override
    protected String getMessagePayload(final HttpServletRequest request) {
        final var payload = super.getMessagePayload(request);
        if (StringUtils.hasText(payload)) {
            try {
                final var maskBodies = maskPIIInfoProperties.getRequestBodyFields();
                final var body = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
                });
                for (final var maskField : maskBodies) {
                    if (body.containsKey(maskField)) {
                        body.put(maskField, "******");
                    }
                }
                return objectMapper.writeValueAsString(body);
            } catch (final JsonProcessingException e) {
                log.warn("Error convert request body", e);
            }
        }
        return payload;
    }

    @Override
    protected String createMessage(final HttpServletRequest request,
                                   final String prefix,
                                   final String suffix) {
        var message = super.createMessage(request, prefix, suffix);
        if (Objects.isNull(request.getQueryString())) {
            return message;
        }
        int index = 0;
        for (final var maskParamPattern : maskPIIInfoProperties.getMaskParamsPattern()) {
            final var matcher = maskParamPattern.matcher(message);
            message = matcher.replaceAll(maskPIIInfoProperties.getRequestBodyFields().get(index) + "=masked");
            index = index + 1;
        }
        return message;
    }
}
