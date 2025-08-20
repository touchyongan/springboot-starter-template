package io.touchyongan.starter_template.feature.audit.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.touchyongan.starter_template.feature.audit.entity.ActionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditTrailData {
    private Long id;
    private String action;
    private String entity;
    private Long resourceId;
    private String httpMethod;
    private String requestUrl;
    private String requestParams;
    private Map<String, Object> requestBody;
    private String ip;
    private Long userId;
    private String username;
    private ActionStatus status;
    private Map<String, Object> newValue;
    private Map<String, Object> oldValue;
    private String errorMessage;
    private LocalDateTime createdAt;

    public String getStatus() {
        return Optional.ofNullable(status)
                .map(ActionStatus::getStatus)
                .orElse(null);
    }

    public String getCreatedAt() {
        return Optional.ofNullable(createdAt)
                .map(LocalDateTime::toString)
                .orElse(null);
    }
}
