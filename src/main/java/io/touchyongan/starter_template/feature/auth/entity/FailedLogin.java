package io.touchyongan.starter_template.feature.auth.entity;

import io.touchyongan.starter_template.common.base.CustomPersistable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "failed_login")
@Entity
@Getter
@Setter
public class FailedLogin extends CustomPersistable {

    @Column(name = "username")
    private String username;

    @Column(name = "ip")
    private String ip;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "is_still_in_attempt")
    private boolean isStillInAttempt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;
}
