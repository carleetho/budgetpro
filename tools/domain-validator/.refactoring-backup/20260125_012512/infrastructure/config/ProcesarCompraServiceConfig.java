package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityAuditLog;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.logistica.compra.service.ProcesarCompraService;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import com.budgetpro.infrastructure.observability.IntegrityEventLogger;
import com.budgetpro.infrastructure.observability.IntegrityMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear el bean de ProcesarCompraService.
 * 
 * Incluye dependencias para validación de integridad criptográfica del presupuesto.
 */
@Configuration
public class ProcesarCompraServiceConfig {

    @Bean
    public ProcesarCompraService procesarCompraService(PartidaRepository partidaRepository,
                                                       PresupuestoRepository presupuestoRepository,
                                                       IntegrityHashService integrityHashService,
                                                       IntegrityAuditLog auditLog,
                                                       GestionInventarioService gestionInventarioService,
                                                       IntegrityEventLogger eventLogger,
                                                       IntegrityMetrics metrics) {
        return new ProcesarCompraService(
                partidaRepository,
                presupuestoRepository,
                integrityHashService,
                auditLog,
                gestionInventarioService,
                eventLogger,
                metrics
        );
    }
}
