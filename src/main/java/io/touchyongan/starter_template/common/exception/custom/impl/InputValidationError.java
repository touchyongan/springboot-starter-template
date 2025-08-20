package io.touchyongan.starter_template.common.exception.custom.impl;

import io.touchyongan.starter_template.common.exception.custom.ApiError;

public enum InputValidationError implements ApiError {
    VALIDATION_ERROR("INPUT_VALIDATION_EXIST", "error.validation.exist"),
    INVALID_SORT_FIELD("INPUT_VALIDATION_INVALID_SORT_FIELD", "error.validation.invalid_sort_field"),
    INVALID_SORT_DIRECTION("INPUT_VALIDATION_INVALID_SORT_DIRECTION", "error.validation.invalid_sort_direction"),
    FIELD_NOT_BLANK("INPUT_VALIDATION_NOT_BLANK", MsgKey.FIELD_NOT_BLANK_KEY);

    private final ApiError apiError;

    InputValidationError(final String errorCode,
                         final String messageKey) {
        this.apiError = new ApiErrorImpl(errorCode, messageKey);
    }

    @Override
    public String getErrorCode() {
        return apiError.getErrorCode();
    }

    @Override
    public String getMessageKey() {
        return apiError.getMessageKey();
    }

    public static InputValidationError fromMessageKey(final String msgKey) {
        for (final var val : InputValidationError.values()) {
            if (val.getMessageKey().equals(msgKey)) {
                return val;
            }
        }
        return null;
    }

    // Declare constant message key here, so we can reuse with annotation validation
    public static class MsgKey {
        public static final String FIELD_NOT_BLANK_KEY = "error.validation.input_not_blank";
    }
}
