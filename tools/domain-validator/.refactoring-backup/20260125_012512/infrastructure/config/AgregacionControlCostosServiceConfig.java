package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import com.budgetpro.domain.finanzas.control.service.AgregacionControlCostosService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio AgregacionControlCostosService como bean de Spring.
 */
@Configuration
public class AgregacionControlCostosServiceConfig {

    @Bean
    public AgregacionControlCostosService agregacionControlCostosService(ApuRepository apuRepository) {
        return new AgregacionControlCostosService(apuRepository);
    }
}
