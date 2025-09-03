package io.touchyongan.starter_template.config;

import io.touchyongan.starter_template.config.custom.CustomAccessDenyException;
import io.touchyongan.starter_template.config.custom.CustomAuthenticationEntryPoint;
import io.touchyongan.starter_template.config.custom.OAuth2ClientAuthenticationSuccessHandler;
import io.touchyongan.starter_template.config.properties.CrossOriginProperties;
import io.touchyongan.starter_template.infrastructure.filter.JwtRequestFilter;
import io.touchyongan.starter_template.infrastructure.filter.OAuth2TempTokenFilter;
import io.touchyongan.starter_template.infrastructure.permission.CustomMethodSecurityExpressionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Set;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // Define all endpoints, you need to access without login here
    public static final Set<String> GET_ANONYMOUS_PATH = Set.of("/error", "/auth/oauth2-clients", "/health");
    public static final Set<String> POST_ANONYMOUS_PATH = Set.of("/auth/token", "/auth/refresh");

    private final JwtRequestFilter jwtRequestFilter;
    private final OAuth2TempTokenFilter oAuth2TempTokenFilter;
    private final CustomAccessDenyException customAccessDenyException;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final OAuth2ClientAuthenticationSuccessHandler oAuth2ClientAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                    req.requestMatchers(HttpMethod.GET, GET_ANONYMOUS_PATH.toArray(new String[0]))
                            .permitAll();
                    req.requestMatchers(HttpMethod.POST, POST_ANONYMOUS_PATH.toArray(new String[0]))
                            .permitAll();
                    // Comment code below for restrict user access other endpoint
                    //req.anyRequest().permitAll();
                    // Uncomment code below to protect all other endpoints required login
                    req.anyRequest().authenticated();
                })
                .exceptionHandling(c -> c.accessDeniedHandler(customAccessDenyException)
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(oAuth2TempTokenFilter, JwtRequestFilter.class)
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> {
                    oauth2.successHandler(oAuth2ClientAuthenticationSuccessHandler);
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public static CustomMethodSecurityExpressionHandler customMethodExpressionHandler() {
        return new CustomMethodSecurityExpressionHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(final CrossOriginProperties crossOriginProperties) {
        final var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(crossOriginProperties.getAllowOrigins());
        configuration.setAllowedMethods(crossOriginProperties.getAllowMethods());
        configuration.setAllowedHeaders(crossOriginProperties.getAllowHeaders());
        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
