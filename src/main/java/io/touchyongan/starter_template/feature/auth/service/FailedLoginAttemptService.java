package io.touchyongan.starter_template.feature.auth.service;

import org.springframework.security.core.AuthenticationException;

public interface FailedLoginAttemptService {

    void applyDelay(String username);

    void loginFailed(String username,
                     AuthenticationException authException);

    void loginSucceed(String username);
}
