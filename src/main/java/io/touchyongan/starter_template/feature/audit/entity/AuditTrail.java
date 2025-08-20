package io.touchyongan.starter_template.feature.audit.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_trail")
@EntityListeners(value = AuditingEntityListener.class)
@Getter
@Setter
public class AuditTrail extends CustomPersistable {

    @Column(name = "action")
    private String action;

    @Column(name = "entity")
    private String entity;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "request_params")
    private String requestParams;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "request_body")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> requestBody;

    // Answer who is performed action on system
    @Column(name = "ip")
    private String ip;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    // Answer what is the effect of event
    @Column(name = "status")
    private ActionStatus status;

    @Column(name = "new_value")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> newValue;

    @Column(name = "old_value")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> oldValue;

    @Column(name = "error_message")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
