package io.touchyongan.starter_template.feature.auth.controller;

import io.touchyongan.starter_template.feature.audit.aop.LogActionAnonymous;
import io.touchyongan.starter_template.feature.auth.data.AuthResponseData;
import io.touchyongan.starter_template.feature.auth.data.LoginRequest;
import io.touchyongan.starter_template.feature.auth.data.RefreshTokenRequest;
import io.touchyongan.starter_template.feature.auth.service.AuthenticationService;
import io.touchyongan.starter_template.feature.user.data.AppUserConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @LogActionAnonymous(entity = AppUserConstant.ENTITY, action = AppUserConstant.LOGIN)
    public ResponseEntity<AuthResponseData> login(@Validated @RequestBody final LoginRequest request) {
        final var result = authenticationService.login(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @LogActionAnonymous(entity = AppUserConstant.ENTITY, action = AppUserConstant.REFRESH_TOKEN)
    public ResponseEntity<AuthResponseData> refreshToken(@Validated @RequestBody final RefreshTokenRequest request) {
        final var result = authenticationService.refreshToken(request);
        return ResponseEntity.ok(result);
    }
}
