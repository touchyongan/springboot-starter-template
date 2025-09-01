package io.touchyongan.starter_template.infrastructure.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.touchyongan.starter_template.common.util.JwtUtil;
import io.touchyongan.starter_template.feature.auth.data.AuthResponseData;
import io.touchyongan.starter_template.feature.user.entity.AppUser;
import io.touchyongan.starter_template.feature.user.service.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OAuth2TempTokenFilter extends OncePerRequestFilter {
    private static final String OAUTH2_CLIENT_EXCHANGE_JWT_TOKEN = "/oauth2/exchange";

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        try {
            final var token = request.getParameter("token");
            final var username = jwtUtil.extractUsername(token, "Bearer");
            final var tokenType = (String) jwtUtil.extractSpecificClaim(token, "Bearer", c -> c.get("tokenType"));

            if (Objects.nonNull(username) && Objects.equals("OAuth2User", tokenType)) {
                final var userDetails = (AppUser) userDetailsService.loadUserByUsername(username);
                final var accessToken = jwtUtil.generateToken(userDetails);
                final var refreshToken = jwtUtil.generateRefreshToken(Map.of("typ", "refreshToken"), userDetails);
                final var auth = new AuthResponseData();
                auth.setUserId(userDetails.getId());
                auth.setAccessToken(accessToken);
                auth.setRefreshToken(refreshToken);

                response.setStatus(200);
                response.setHeader("Content-Type", "application/json");
                response.getWriter()
                        .write(objectMapper.writeValueAsString(auth));
                return;
            }
        } catch (final Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        return !PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.GET, OAUTH2_CLIENT_EXCHANGE_JWT_TOKEN)
                .matches(request);
    }
}
