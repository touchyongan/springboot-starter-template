package io.touchyongan.starter_template.feature.auth.controller;

import io.touchyongan.starter_template.common.data.ApiResponse;
import io.touchyongan.starter_template.feature.audit.aop.LogActionAnonymous;
import io.touchyongan.starter_template.feature.auth.data.AuthResponseData;
import io.touchyongan.starter_template.feature.auth.data.LoginRequest;
import io.touchyongan.starter_template.feature.auth.data.OAuthClientUrlData;
import io.touchyongan.starter_template.feature.auth.data.RefreshTokenRequest;
import io.touchyongan.starter_template.feature.auth.service.AuthenticationService;
import io.touchyongan.starter_template.feature.user.data.AppUserConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/oauth2-clients")
    public ResponseEntity<ApiResponse<List<OAuthClientUrlData>>> getOAuthClients(final HttpServletRequest request) {
        final var result = new ArrayList<OAuthClientUrlData>();

        if (clientRegistrationRepository instanceof InMemoryClientRegistrationRepository repo) {
            for (final var registration : repo) {
                final var dto = new OAuthClientUrlData();
                dto.setProvider(registration.getRegistrationId());
                dto.setAuthorizationUrl(buildOAuthClientUrl(request, registration));
                result.add(dto);
            }
        }

        return ResponseEntity.ok(new ApiResponse<>(result));
    }

    private String buildOAuthClientUrl(final HttpServletRequest request,
                                       final ClientRegistration registration) {
        return request.getScheme() + "://" +
                request.getServerName() +
                (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort()) +
                request.getContextPath() +
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI +
                "/" + registration.getRegistrationId();
    }

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
