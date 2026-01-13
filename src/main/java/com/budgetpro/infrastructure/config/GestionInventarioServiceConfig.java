package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar el servicio de dominio GestionInventarioService como bean de Spring.
 */
@Configuration
public class GestionInventarioServiceConfig {

    @Bean
    public GestionInventarioService gestionInventarioService(InventarioRepository inventarioRepository) {
        return new GestionInventarioService(inventarioRepository);
    }
}
