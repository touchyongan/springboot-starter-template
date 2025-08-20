package io.touchyongan.starter_template.common.exception.custom.impl;

import io.touchyongan.starter_template.common.exception.custom.ApiError;

public enum AuthError implements ApiError {
    AUTH_UNAUTHENTICATED("AUTH_UNAUTHENTICATED_ERROR", "error.auth.unauthenticated"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", "error.auth.token_expired"),
    AUTH_TOKEN_INVALID("AUTH_TOKEN_INVALID", "error.auth.token_invalid"),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED_ERROR", "error.auth.unauthorized");

    private final ApiError apiError;

    AuthError(final String errorCode,
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
