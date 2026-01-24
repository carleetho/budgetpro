package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;

import java.util.UUID;

/**
 * Servicio de Dominio para gestionar el inventario.
 *
 * Responsabilidad:
 * - Registrar entradas de material por compras (vía InventarioSnapshotService, Authority by PO)
 * - Registrar salidas de material por consumos
 * - Asegurar que el stock físico se actualiza correctamente
 *
 * No persiste, solo orquesta la lógica de dominio.
 */
public class GestionInventarioService {

    private final InventarioRepository inventarioRepository;
    private final InventarioSnapshotService inventarioSnapshotService;
    private final BacklogService backlogService;

    public GestionInventarioService(InventarioRepository inventarioRepository,
                                    InventarioSnapshotService inventarioSnapshotService,
                                    BacklogService backlogService) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioSnapshotService = inventarioSnapshotService;
        this.backlogService = backlogService;
    }

    /**
     * Registra la entrada de material al inventario cuando se aprueba una compra.
     *
     * Para cada detalle: find-or-create vía InventarioSnapshotService (snapshot catálogo,
     * detección de cambio de unidad, Authority by PO), ingresar() y persistir.
     * 
     * Después de registrar la entrada, resuelve el backlog para ese recurso.
     *
     * @param compra La compra aprobada con sus detalles
     * @throws IllegalArgumentException si no hay bodega por defecto o el recurso no existe en catálogo
     */
    public void registrarEntradaPorCompra(Compra compra) {
        for (CompraDetalle detalle : compra.getDetalles()) {
            InventarioItem inventarioItem = inventarioSnapshotService.crearDesdeCompra(compra, detalle);

            inventarioItem.ingresar(
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getId().getValue(),
                    String.format("Entrada por compra #%s - %s", compra.getId().getValue(), compra.getProveedor())
            );

            inventarioRepository.save(inventarioItem);

            // Resolver backlog para este recurso cuando llega stock
            String unidad = detalle.getUnidad() != null && !detalle.getUnidad().isBlank()
                    ? detalle.getUnidad().trim()
                    : inventarioItem.getUnidadBase();
            backlogService.resolverBacklog(compra.getProyectoId(), detalle.getRecursoExternalId(), unidad);
        }
    }

    /**
     * Registra una salida de material por consumo.
     * 
     * @param proyectoId ID del proyecto
     * @param recursoId ID del recurso
     * @param cantidad Cantidad a egresar
     * @param referencia Descripción o referencia de la salida
     * @throws IllegalArgumentException si el inventario no existe o la referencia está vacía
     * @throws com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException si no hay suficiente stock
     */
    public void registrarSalidaPorConsumo(UUID proyectoId, UUID recursoId, java.math.BigDecimal cantidad,
                                         String referencia) {
        InventarioItem inventarioItem = inventarioRepository
                .findByProyectoIdAndRecursoId(proyectoId, recursoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Inventario no encontrado para proyecto %s y recurso %s",
                                    proyectoId, recursoId)));

        // Registrar la salida usando el método del agregado
        inventarioItem.egresar(cantidad, referencia);

        // Persistir el inventario (con sus movimientos nuevos)
        inventarioRepository.save(inventarioItem);
    }

}
