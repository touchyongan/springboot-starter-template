package io.touchyongan.starter_template.feature.auth.service;

import io.touchyongan.starter_template.feature.auth.data.AuthResponseData;
import io.touchyongan.starter_template.feature.auth.data.LoginRequest;
import io.touchyongan.starter_template.feature.auth.data.RefreshTokenRequest;

public interface AuthenticationService {

    AuthResponseData login(LoginRequest loginRequest);

    AuthResponseData refreshToken(RefreshTokenRequest refreshTokenRequest);
}
