package io.touchyongan.starter_template.common.exception.data;

public record FieldError(String field,
                         String errorCode,
                         String messageKey,
                         Object value,
                         Object... args) {
}
