package io.touchyongan.starter_template.common.exception.custom.impl;

import io.touchyongan.starter_template.common.exception.custom.ApiError;

public class ApiErrorImpl implements ApiError {

    private final String errorCode;
    private final String messageKey;

    public ApiErrorImpl(final String errorCode,
                        final String messageKey) {
        this.errorCode = errorCode;
        this.messageKey = messageKey;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
