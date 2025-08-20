package io.touchyongan.starter_template.feature.audit.entity;

import lombok.Getter;

@Getter
public enum ActionStatus {
    SUCCESS("Success"),
    FAILURE("Failure"),
    UNKNOWN("Unknown");

    private final String status;

    ActionStatus(final String status) {
        this.status = status;
    }

    public static ActionStatus from(final String s) {
        for (final var status : ActionStatus.values()) {
            if (status.status.equalsIgnoreCase(s)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
