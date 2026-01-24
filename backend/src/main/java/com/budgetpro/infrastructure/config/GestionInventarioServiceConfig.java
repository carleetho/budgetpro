package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.port.out.*;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import com.budgetpro.domain.logistica.inventario.service.ImputacionService;
import com.budgetpro.domain.logistica.inventario.service.InventarioSnapshotService;
import com.budgetpro.domain.shared.port.out.SecurityPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar GestionInventarioService e
 * InventarioSnapshotService como beans.
 */
@Configuration
public class GestionInventarioServiceConfig {

    @Bean
    public InventarioSnapshotService inventarioSnapshotService(CatalogPort catalogPort,
            InventarioRepository inventarioRepository, DefaultBodegaPort defaultBodegaPort) {
        return new InventarioSnapshotService(catalogPort, inventarioRepository, defaultBodegaPort);
    }

    @Bean
    public ImputacionService imputacionService(PartidaRepository partidaRepository,
            ConsumoPartidaRepository consumoPartidaRepository) {
        return new ImputacionService(partidaRepository, consumoPartidaRepository);
    }

    @Bean
    public GestionInventarioService gestionInventarioService(InventarioRepository inventarioRepository,
            InventarioSnapshotService inventarioSnapshotService, BacklogService backlogService,
            PartidaValidator partidaValidator, AcPublisher acPublisher, BudgetAlertPublisher budgetAlertPublisher,
            SecurityPort securityPort) {
        return new GestionInventarioService(inventarioRepository, inventarioSnapshotService, backlogService,
                partidaValidator, acPublisher, budgetAlertPublisher, securityPort);
    }
}
