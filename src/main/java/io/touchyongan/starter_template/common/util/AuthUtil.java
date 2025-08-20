package io.touchyongan.starter_template.common.util;

import io.touchyongan.starter_template.feature.user.entity.AppUser;
import io.touchyongan.starter_template.feature.user.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AuthUtil {

    private AuthUtil() {
    }

    public static AppUser getCurrentUser() {
        final var principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .orElse(null);
        if (Objects.isNull(principal)) {
            return null;
        }
        if (principal instanceof final AppUser users) {
            return users;
        }
        return null;
    }

    public static AppUser getCurrentUserDefaultEmpty() {
        return Optional.ofNullable(getCurrentUser())
                .orElseGet(AppUser::new);
    }

    public static String getCurrentUsername() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var principal = Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .orElse(null);
        if (Objects.isNull(principal)) {
            return null;
        }

        if (principal instanceof final AppUser users) {
            return users.getUsername();
        }
        return null;
    }

    public static List<Long> getCurrentUserRoles() {
        return Optional.ofNullable(getCurrentUser())
                .map(AppUser::getRoles)
                .stream()
                .flatMap(Collection::stream)
                .map(Role::getId)
                .toList();
    }
}
