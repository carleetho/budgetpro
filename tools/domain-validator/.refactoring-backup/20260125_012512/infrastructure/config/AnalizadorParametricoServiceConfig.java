package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.alertas.service.AnalizadorParametricoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del bean AnalizadorParametricoService.
 */
@Configuration
public class AnalizadorParametricoServiceConfig {
    
    @Bean
    public AnalizadorParametricoService analizadorParametricoService() {
        return new AnalizadorParametricoService();
    }
}
