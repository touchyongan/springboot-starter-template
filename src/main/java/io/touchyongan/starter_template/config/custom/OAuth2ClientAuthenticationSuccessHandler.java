package io.touchyongan.starter_template.config.custom;

import io.touchyongan.starter_template.feature.auth.service.OAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2ClientAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2UserService oAuth2UserService;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {
        oAuth2UserService.handleOAuth2UserOnLoginSuccess(request, response, authentication);
    }
}
