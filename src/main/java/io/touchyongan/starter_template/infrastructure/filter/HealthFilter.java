package io.touchyongan.starter_template.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HealthFilter extends OncePerRequestFilter {
    private static final String HEALTH_ENDPOINT = "/health";

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        response.setStatus(200);
        response.getWriter()
                .write("OK");
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        return !PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.GET, HEALTH_ENDPOINT)
                .matches(request);
    }
}
