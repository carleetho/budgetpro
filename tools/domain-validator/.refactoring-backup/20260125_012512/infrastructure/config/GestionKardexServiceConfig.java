package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.logistica.almacen.service.GestionKardexService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del bean GestionKardexService.
 */
@Configuration
public class GestionKardexServiceConfig {
    
    @Bean
    public GestionKardexService gestionKardexService() {
        return new GestionKardexService();
    }
}
