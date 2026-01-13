package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear el bean de CalculoPresupuestoService.
 * 
 * Este servicio de dominio no es un bean de Spring por defecto,
 * así que lo configuramos manualmente aquí.
 */
@Configuration
public class CalculoPresupuestoServiceConfig {

    @Bean
    public CalculoPresupuestoService calculoPresupuestoService(PartidaRepository partidaRepository,
                                                               ApuRepository apuRepository) {
        return new CalculoPresupuestoService(partidaRepository, apuRepository);
    }
}
