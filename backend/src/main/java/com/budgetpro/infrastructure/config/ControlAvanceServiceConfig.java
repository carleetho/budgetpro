package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.avance.port.out.AvanceFisicoRepository;
import com.budgetpro.domain.finanzas.avance.service.ControlAvanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio ControlAvanceService como bean de Spring.
 */
@Configuration
public class ControlAvanceServiceConfig {

    @Bean
    public ControlAvanceService controlAvanceService(AvanceFisicoRepository avanceFisicoRepository) {
        return new ControlAvanceService(avanceFisicoRepository);
    }
}
