package com.budgetpro.infrastructure.config;

import com.budgetpro.shared.SystemActorIds;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/**
 * Configuración de auditoría para poblar created_by automáticamente.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfig {

    private static final UUID SYSTEM_USER_ID = SystemActorIds.EVENT_INFRA_SYSTEM_USER_UUID;

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> Optional.of(SYSTEM_USER_ID);
    }
}
