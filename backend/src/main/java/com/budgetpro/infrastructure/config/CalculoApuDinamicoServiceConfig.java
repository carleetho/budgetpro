package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.catalogo.service.CalculoApuDinamicoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear el bean de CalculoApuDinamicoService.
 * 
 * Este servicio de dominio debe ser agnóstico del framework (Hexagonal Architecture),
 * por lo que se configura manualmente aquí en la capa de infraestructura.
 */
@Configuration
public class CalculoApuDinamicoServiceConfig {

    @Bean
    public CalculoApuDinamicoService calculoApuDinamicoService() {
        return new CalculoApuDinamicoService();
    }
}
