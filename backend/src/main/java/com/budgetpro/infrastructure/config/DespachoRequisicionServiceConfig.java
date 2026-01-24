package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.service.ImputacionService;
import com.budgetpro.domain.logistica.requisicion.port.out.RequisicionRepository;
import com.budgetpro.domain.logistica.requisicion.service.DespachoRequisicionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar DespachoRequisicionService como bean.
 */
@Configuration
public class DespachoRequisicionServiceConfig {

    @Bean
    public DespachoRequisicionService despachoRequisicionService(RequisicionRepository requisicionRepository,
            InventarioRepository inventarioRepository, DefaultBodegaPort defaultBodegaPort,
            BacklogService backlogService, ImputacionService imputacionService) {
        return new DespachoRequisicionService(requisicionRepository, inventarioRepository, defaultBodegaPort,
                backlogService, imputacionService);
    }
}
