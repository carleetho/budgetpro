package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.cronograma.service.CalculoCronogramaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio CalculoCronogramaService como bean de Spring.
 */
@Configuration
public class CalculoCronogramaServiceConfig {

    @Bean
    public CalculoCronogramaService calculoCronogramaService() {
        return new CalculoCronogramaService();
    }
}
