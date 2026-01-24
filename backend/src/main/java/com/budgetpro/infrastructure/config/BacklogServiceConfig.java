package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.logistica.backlog.port.out.RequerimientoCompraRepository;
import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.requisicion.port.out.RequisicionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar BacklogService como bean.
 */
@Configuration
public class BacklogServiceConfig {

    @Bean
    public BacklogService backlogService(RequerimientoCompraRepository requerimientoCompraRepository,
                                        RequisicionRepository requisicionRepository,
                                        InventarioRepository inventarioRepository) {
        return new BacklogService(requerimientoCompraRepository, requisicionRepository, inventarioRepository);
    }
}
