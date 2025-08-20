package io.touchyongan.starter_template.common.exception.custom.impl;

import io.touchyongan.starter_template.common.exception.custom.ApiError;

public enum ResourceNotFoundError implements ApiError {
    NOT_FOUND("RESOURCE_VALIDATION_NOT_FOUND", "error.resource.not_found");

    private final ApiError apiError;

    ResourceNotFoundError(final String errorCode,
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
