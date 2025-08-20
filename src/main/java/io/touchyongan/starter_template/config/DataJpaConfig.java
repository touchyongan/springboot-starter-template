package io.touchyongan.starter_template.config;

import io.touchyongan.starter_template.feature.user.entity.AppUser;
import io.touchyongan.starter_template.infrastructure.jpa.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class DataJpaConfig {

    @Bean(name = "auditorAware")
    public AuditorAware<AppUser> auditorAware() {
        return new AuditorAwareImpl();
    }
}
