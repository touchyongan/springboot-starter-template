package io.touchyongan.starter_template.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoopbackIpRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        if (request.getServerName().equals("localhost") && request.getHeader("host") != null) {
            final var uri = UriComponentsBuilder.fromUriString(request.getRequestURI())
                    .scheme(request.getScheme())
                    .host("127.0.0.1")
                    .port(request.getServerPort())
                    .build();
            response.sendRedirect(uri.toUriString());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
