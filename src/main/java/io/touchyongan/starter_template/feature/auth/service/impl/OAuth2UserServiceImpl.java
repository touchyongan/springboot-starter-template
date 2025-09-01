package io.touchyongan.starter_template.feature.auth.service.impl;

import io.touchyongan.starter_template.common.util.JwtUtil;
import io.touchyongan.starter_template.feature.auth.service.OAuth2UserService;
import io.touchyongan.starter_template.feature.user.entity.AppUser;
import io.touchyongan.starter_template.feature.user.entity.UserInfo;
import io.touchyongan.starter_template.feature.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl implements OAuth2UserService {
    private final static long TEMP_TOKEN_TIME = 45_000L;

    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;

    @Value("${oauth2.client.ui-redirect-url}")
    private String uiRedirectUrl;

    public void handleOAuth2UserOnLoginSuccess(final HttpServletRequest request,
                                               final HttpServletResponse response,
                                               final Authentication authentication) throws IOException {
        // https://developers.google.com/identity/openid-connect/openid-connect
        // https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html
        final var oauth2User = (OAuth2User) authentication.getPrincipal();
        final var attributes = oauth2User.getAttributes();

        final var email = (String) attributes.get("email");
        final var isExist = appUserRepository.isExist(email);
        if (!isExist) {
            final var firstName = (String) attributes.get("given_name");
            final var lastName = (String) attributes.get("family_name");

            final var userinfo = new UserInfo();
            userinfo.setFirstname(firstName);
            userinfo.setLastname(lastName);

            final var appuser = new AppUser();
            appuser.setEmail(email);
            appuser.setUsername(email);
            appuser.setUserInfo(userinfo);
            appuser.setEnabled(true);
            // If you want to set a default role, you can set it here
            appUserRepository.save(appuser);
        }

        final var token = jwtUtil.generateTempToken(email, TEMP_TOKEN_TIME);
        final var redirectUrl = uiRedirectUrl + "?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
