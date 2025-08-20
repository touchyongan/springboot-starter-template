package io.touchyongan.starter_template.common.exception.custom;

import io.touchyongan.starter_template.common.exception.custom.impl.InputValidationError;
import io.touchyongan.starter_template.common.exception.data.FieldError;
import lombok.Getter;

import java.util.List;

@Getter
public class InputValidationException extends BaseApiException {

    private final List<FieldError> errors;

    public InputValidationException(final List<FieldError> errors,
                                    final Object... args) {
        super(InputValidationError.VALIDATION_ERROR, args);
        this.errors = errors;
    }
}
