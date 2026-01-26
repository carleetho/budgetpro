package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.reajuste.service.CalculadorReajusteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del bean CalculadorReajusteService.
 */
@Configuration
public class CalculadorReajusteServiceConfig {
    
    @Bean
    public CalculadorReajusteService calculadorReajusteService() {
        return new CalculadorReajusteService();
    }
}
