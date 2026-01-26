package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.sobrecosto.port.out.ConfiguracionLaboralRepository;
import com.budgetpro.domain.finanzas.sobrecosto.service.CalcularSalarioRealService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio CalcularSalarioRealService como bean de Spring.
 */
@Configuration
public class CalcularSalarioRealServiceConfig {

    @Bean
    public CalcularSalarioRealService calcularSalarioRealService(ConfiguracionLaboralRepository configuracionLaboralRepository) {
        return new CalcularSalarioRealService(configuracionLaboralRepository);
    }
}
