package io.touchyongan.starter_template.feature.auth.service.impl;

import io.jsonwebtoken.security.SignatureException;
import io.touchyongan.starter_template.common.util.JwtUtil;
import io.touchyongan.starter_template.feature.auth.data.AuthResponseData;
import io.touchyongan.starter_template.feature.auth.data.LoginRequest;
import io.touchyongan.starter_template.feature.auth.data.RefreshTokenRequest;
import io.touchyongan.starter_template.feature.auth.service.AuthenticationService;
import io.touchyongan.starter_template.feature.auth.service.FailedLoginAttemptService;
import io.touchyongan.starter_template.feature.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final FailedLoginAttemptService failedLoginAttemptService;
    private final JwtUtil jwtUtil;

    @Transactional
    @Override
    public AuthResponseData login(final LoginRequest loginRequest) {
        final var username = loginRequest.getUsername();
        try {
            final var token = new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword());
            failedLoginAttemptService.applyDelay(username);
            authenticationManager.authenticate(token);
        } catch (final AuthenticationException e) {
            failedLoginAttemptService.loginFailed(username, e);
            throw e;
        }
        failedLoginAttemptService.loginSucceed(username);

        final var user = (AppUser) userDetailsService.loadUserByUsername(username);
        final var accessToken = jwtUtil.generateToken(user);
        final var refreshToken = jwtUtil.generateRefreshToken(Map.of("typ", "refreshToken"), user);

        final var token = new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword(), Collections.emptyList());
        SecurityContextHolder.getContext()
                .setAuthentication(token);

        return new AuthResponseData()
                .setUserId(user.getId())
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    @Transactional
    @Override
    public AuthResponseData refreshToken(final RefreshTokenRequest refreshTokenRequest) {
        try {
            final var username = jwtUtil.extractUsername(refreshTokenRequest.getRefreshToken());
            final var user = (AppUser) userDetailsService.loadUserByUsername(username);
            final var newAccessToken = jwtUtil.generateToken(user);
            final var newRefreshToken = jwtUtil.generateRefreshToken(Map.of("typ", "refreshToken"), user);
            return new AuthResponseData()
                    .setUserId(user.getId())
                    .setAccessToken(newAccessToken)
                    .setRefreshToken(newRefreshToken);
        } catch (final SignatureException e) {
            throw new SignatureException(e.getMessage());
        }
    }
}
