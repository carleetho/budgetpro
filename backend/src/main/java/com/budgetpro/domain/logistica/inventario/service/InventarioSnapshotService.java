package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;

import java.util.Objects;

/**
 * Servicio de dominio que captura snapshots desde el catálogo e implementa
 * "Authority by PO" para manejo automático de cambios de unidad.
 *
 * <p>No persiste; orquesta find-or-create y delega persistencia a GestionInventarioService.
 * Mantiene PMP independiente por variante de unidad (KG vs LIBRAS coexisten).
 */
public final class InventarioSnapshotService {

    private static final String DEFAULT_CATALOG_SOURCE = "MOCK";

    private final CatalogPort catalogPort;
    private final InventarioRepository inventarioRepository;
    private final DefaultBodegaPort defaultBodegaPort;
    private final String catalogSource;

    public InventarioSnapshotService(CatalogPort catalogPort,
                                     InventarioRepository inventarioRepository,
                                     DefaultBodegaPort defaultBodegaPort) {
        this(catalogPort, inventarioRepository, defaultBodegaPort, DEFAULT_CATALOG_SOURCE);
    }

    public InventarioSnapshotService(CatalogPort catalogPort,
                                     InventarioRepository inventarioRepository,
                                     DefaultBodegaPort defaultBodegaPort,
                                     String catalogSource) {
        this.catalogPort = Objects.requireNonNull(catalogPort, "catalogPort");
        this.inventarioRepository = Objects.requireNonNull(inventarioRepository, "inventarioRepository");
        this.defaultBodegaPort = Objects.requireNonNull(defaultBodegaPort, "defaultBodegaPort");
        this.catalogSource = catalogSource != null && !catalogSource.isBlank() ? catalogSource : DEFAULT_CATALOG_SOURCE;
    }

    /**
     * Find-or-create con detección de cambio de unidad.
     * Obtiene snapshot del catálogo, compara unidad de la compra vs catálogo,
     * y auto-crea nuevo InventarioItem cuando hay cambio de unidad (Authority by PO).
     *
     * @param compra  Compra aprobada
     * @param detalle Detalle de la compra
     * @return InventarioItem existente o recién creado (sin persistir)
     */
    public InventarioItem crearDesdeCompra(Compra compra, CompraDetalle detalle) {
        BodegaId bodegaId = defaultBodegaPort.getDefaultForProject(compra.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay bodega por defecto para el proyecto " + compra.getProyectoId()));

        RecursoSnapshot snapshot = catalogPort.fetchRecurso(detalle.getRecursoExternalId(), catalogSource);

        String unidadCatalog = snapshot.unidad().trim();
        String unidadPO = detalle.getUnidad() != null && !detalle.getUnidad().isBlank()
                ? detalle.getUnidad().trim()
                : unidadCatalog;

        boolean mismatch = !unidadPO.equalsIgnoreCase(unidadCatalog);

        if (mismatch) {
            // Authority by PO: auto-crear nuevo ítem con unidad de la compra.
            // No bloquear; catálogo y PO divergen, se prioriza la PO.
            InventarioId id = InventarioId.generate();
            return InventarioItem.crearConSnapshot(
                    id,
                    compra.getProyectoId(),
                    detalle.getRecursoExternalId(),
                    bodegaId,
                    snapshot.nombre(),
                    snapshot.tipo().name(),
                    unidadPO
            );
        }

        // Misma unidad: find-or-create con unidad del catálogo.
        String unidadToUse = unidadCatalog;
        var existing = inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                compra.getProyectoId(),
                detalle.getRecursoExternalId(),
                unidadToUse,
                bodegaId
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        InventarioId id = InventarioId.generate();
        return InventarioItem.crearConSnapshot(
                id,
                compra.getProyectoId(),
                detalle.getRecursoExternalId(),
                bodegaId,
                snapshot.nombre(),
                snapshot.tipo().name(),
                unidadToUse
        );
    }
}
