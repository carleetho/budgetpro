package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.cronograma.service.CronogramaService;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.finanzas.presupuesto.service.PresupuestoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear el bean de PresupuestoService.
 */
@Configuration
public class PresupuestoServiceConfig {

    @Bean
    public PresupuestoService presupuestoService(
            PresupuestoRepository presupuestoRepository,
            ProgramaObraRepository programaObraRepository,
            CronogramaService cronogramaService,
            IntegrityHashService integrityHashService) {
        return new PresupuestoService(
                presupuestoRepository,
                programaObraRepository,
                cronogramaService,
                integrityHashService
        );
    }
}
