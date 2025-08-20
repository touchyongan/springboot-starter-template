package io.touchyongan.starter_template.common.exception.custom.impl;

import io.touchyongan.starter_template.common.exception.custom.ApiError;

public enum GeneralError implements ApiError {
    INVALID_ENUM("GENERAL_VALIDATION_INVALID_ENUM", "error.general.invalid_enum");

    private final ApiError apiError;

    GeneralError(final String errorCode,
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
}
