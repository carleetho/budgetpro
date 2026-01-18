package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.logistica.compra.service.ProcesarCompraService;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear el bean de ProcesarCompraService.
 */
@Configuration
public class ProcesarCompraServiceConfig {

    @Bean
    public ProcesarCompraService procesarCompraService(PartidaRepository partidaRepository,
                                                       GestionInventarioService gestionInventarioService) {
        return new ProcesarCompraService(partidaRepository, gestionInventarioService);
    }
}
