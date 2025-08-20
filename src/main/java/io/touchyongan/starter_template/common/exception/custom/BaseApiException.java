package io.touchyongan.starter_template.common.exception.custom;

import lombok.Getter;

@Getter
public class BaseApiException extends RuntimeException {
    private final String errorCode;
    private final String messageKey;
    private final Object[] args;

    public BaseApiException(final ApiError apiError,
                            final Object... args) {
        this.errorCode = apiError.getErrorCode();
        this.messageKey = apiError.getMessageKey();
        this.args = args;
    }

}
