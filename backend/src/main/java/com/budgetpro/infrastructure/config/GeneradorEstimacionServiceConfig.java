package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.estimacion.service.GeneradorEstimacionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio GeneradorEstimacionService como bean de Spring.
 */
@Configuration
public class GeneradorEstimacionServiceConfig {

    @Bean
    public GeneradorEstimacionService generadorEstimacionService() {
        return new GeneradorEstimacionService();
    }
}
