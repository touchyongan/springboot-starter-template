package io.touchyongan.starter_template.feature.auth.data;

import io.touchyongan.starter_template.common.exception.custom.impl.InputValidationError;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = InputValidationError.MsgKey.FIELD_NOT_BLANK_KEY)
    private String username;
    @NotBlank(message = InputValidationError.MsgKey.FIELD_NOT_BLANK_KEY)
    private String password;
}
