package io.touchyongan.starter_template.feature.auth.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AuthResponseData {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
