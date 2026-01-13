package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.sobrecosto.port.out.AnalisisSobrecostoRepository;
import com.budgetpro.domain.finanzas.sobrecosto.service.CalculadoraPrecioVentaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio CalculadoraPrecioVentaService como bean de Spring.
 */
@Configuration
public class CalculadoraPrecioVentaServiceConfig {

    @Bean
    public CalculadoraPrecioVentaService calculadoraPrecioVentaService(AnalisisSobrecostoRepository analisisSobrecostoRepository) {
        return new CalculadoraPrecioVentaService(analisisSobrecostoRepository);
    }
}
