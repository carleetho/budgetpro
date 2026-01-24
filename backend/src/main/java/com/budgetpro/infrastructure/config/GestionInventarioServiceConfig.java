package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import com.budgetpro.domain.logistica.inventario.service.InventarioSnapshotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para registrar GestionInventarioService e InventarioSnapshotService como beans.
 */
@Configuration
public class GestionInventarioServiceConfig {

    @Bean
    public InventarioSnapshotService inventarioSnapshotService(CatalogPort catalogPort,
                                                               InventarioRepository inventarioRepository,
                                                               DefaultBodegaPort defaultBodegaPort) {
        return new InventarioSnapshotService(catalogPort, inventarioRepository, defaultBodegaPort);
    }

    @Bean
    public GestionInventarioService gestionInventarioService(InventarioRepository inventarioRepository,
                                                             InventarioSnapshotService inventarioSnapshotService,
                                                             BacklogService backlogService) {
        return new GestionInventarioService(inventarioRepository, inventarioSnapshotService, backlogService);
    }
}
