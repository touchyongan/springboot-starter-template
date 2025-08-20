package io.touchyongan.starter_template.infrastructure.jpa;

import io.touchyongan.starter_template.feature.user.entity.AppUser;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<AppUser> {

    @Override
    public Optional<AppUser> getCurrentAuditor() {
        return Optional.empty();
    }
}
