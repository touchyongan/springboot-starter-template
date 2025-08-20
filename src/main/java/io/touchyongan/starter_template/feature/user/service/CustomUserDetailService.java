package io.touchyongan.starter_template.feature.user.service;

import io.touchyongan.starter_template.feature.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final var lower = Optional.ofNullable(username)
                .map(String::toLowerCase)
                .orElseThrow(() -> usernameNotFoundException(username));
        return appUserRepository.findByUsernameJoinFetch(lower)
                .orElseThrow(() -> usernameNotFoundException(username));
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameWithPermission(final String username) throws UsernameNotFoundException {
        final var lower = Optional.ofNullable(username)
                .map(String::toLowerCase)
                .orElseThrow(() -> usernameNotFoundException(username));
        final var user = appUserRepository.findByUsernameJoinFetch(lower)
                .orElseThrow(() -> usernameNotFoundException(username));
        user.getAuthorities();
        return user;
    }

    private UsernameNotFoundException usernameNotFoundException(final String username) {
        return new UsernameNotFoundException("User not found: %s".formatted(username));
    }
}
