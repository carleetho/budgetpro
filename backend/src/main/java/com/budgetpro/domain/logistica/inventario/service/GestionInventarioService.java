package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.inventario.event.BudgetAlertEvent;
import com.budgetpro.domain.logistica.inventario.event.BudgetAlertEvent.BudgetAlertSeverity;
import com.budgetpro.domain.logistica.inventario.event.MaterialConsumed;
import com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException;
import com.budgetpro.domain.logistica.inventario.exception.ExcesoRecepcionException;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;
import com.budgetpro.domain.logistica.inventario.port.out.AcPublisher;
import com.budgetpro.domain.logistica.inventario.port.out.BudgetAlertPublisher;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.port.out.PartidaValidator;
import com.budgetpro.domain.shared.port.out.SecurityPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de Dominio para gestionar el inventario.
 *
 * Responsabilidad: - Registrar entradas de material por compras (vía
 * InventarioSnapshotService, Authority by PO) - Registrar salidas de material
 * por consumos - Asegurar que el stock físico se actualiza correctamente
 *
 * No persiste, solo orquesta la lógica de dominio.
 */
public class GestionInventarioService {

    private final InventarioRepository inventarioRepository;
    private final InventarioSnapshotService inventarioSnapshotService;
    private final BacklogService backlogService;
    private final PartidaValidator partidaValidator;
    private final AcPublisher acPublisher;
    private final BudgetAlertPublisher budgetAlertPublisher;
    private final SecurityPort securityPort;

    public GestionInventarioService(InventarioRepository inventarioRepository,
            InventarioSnapshotService inventarioSnapshotService, BacklogService backlogService,
            PartidaValidator partidaValidator, AcPublisher acPublisher, BudgetAlertPublisher budgetAlertPublisher,
            SecurityPort securityPort) {
        this.inventarioRepository = inventarioRepository;
        this.inventarioSnapshotService = inventarioSnapshotService;
        this.backlogService = backlogService;
        this.partidaValidator = partidaValidator;
        this.acPublisher = acPublisher;
        this.budgetAlertPublisher = budgetAlertPublisher;
        this.securityPort = securityPort;
    }

    /**
     * Registra la entrada de material al inventario cuando se aprueba una compra.
     *
     * Permite recepción parcial o total. Verifica tolerancia del 5% sobre lo
     * ordenado.
     *
     * @param compra              La compra aprobada
     * @param cantidadesRecibidas Mapa de CompraDetalleId -> Cantidad Recibida
     * @throws ExcesoRecepcionException si la cantidad recibida supera el 105% de lo
     *                                  ordenado
     * @throws IllegalArgumentException si los argumentos son inválidos
     */
    public void registrarEntradaPorCompra(Compra compra, Map<UUID, BigDecimal> cantidadesRecibidas) {
        for (CompraDetalle detalle : compra.getDetalles()) {
            UUID detalleId = detalle.getId().getValue();
            if (!cantidadesRecibidas.containsKey(detalleId)) {
                continue; // No se recibe nada para este detalle
            }

            BigDecimal cantidadRecibida = cantidadesRecibidas.get(detalleId);
            BigDecimal cantidadOrdenada = detalle.getCantidad();

            // Validar tolerancia (Max 105%)
            BigDecimal limiteTolerancia = cantidadOrdenada.multiply(new BigDecimal("1.05"));
            if (cantidadRecibida.compareTo(limiteTolerancia) > 0) {
                throw new ExcesoRecepcionException(
                        String.format("Exceso de recepción para el detalle %s. Ordenado: %s, Recibido: %s (Max: %s)",
                                detalle.getRecursoExternalId(), cantidadOrdenada, cantidadRecibida, limiteTolerancia));
            }

            InventarioItem inventarioItem = inventarioSnapshotService.crearDesdeCompra(compra, detalle);

            inventarioItem.ingresar(cantidadRecibida, detalle.getPrecioUnitario(), detalleId,
                    String.format("Entrada por compra #%s - %s", compra.getId().getValue(), compra.getProveedor()));

            inventarioRepository.save(inventarioItem);

            // Resolver backlog para este recurso cuando llega stock
            String unidad = detalle.getUnidad() != null && !detalle.getUnidad().isBlank() ? detalle.getUnidad().trim()
                    : inventarioItem.getUnidadBase();
            backlogService.resolverBacklog(compra.getProyectoId(), detalle.getRecursoExternalId(), unidad);
        }
    }

    /**
     * Registra una salida de material por consumo con imputación presupuestal.
     * 
     * @param proyectoId ID del proyecto
     * @param recursoId  ID del recurso (InventoryItem ID or Resource ID? Using
     *                   Recurso ID logical lookup)
     * @param partidaId  ID de la Partida Presupuestal
     * @param cantidad   Cantidad a egresar
     * @param referencia Descripción o referencia de la salida
     * @throws IllegalArgumentException      si el inventario no existe, partida no
     *                                       válida
     * @throws CantidadInsuficienteException si no hay suficiente stock
     */
    public void registrarSalidaPorConsumo(UUID proyectoId, UUID recursoId, UUID partidaId, BigDecimal cantidad,
            String referencia) {
        // Validar Partida
        if (!partidaValidator.existeYEstaActiva(partidaId)) {
            throw new IllegalArgumentException("La partida presupuestal no existe o no está activa: " + partidaId);
        }

        InventarioItem inventarioItem = inventarioRepository.findByProyectoIdAndRecursoId(proyectoId, recursoId)
                .orElseThrow(() -> new IllegalArgumentException(String
                        .format("Inventario no encontrado para proyecto %s y recurso %s", proyectoId, recursoId)));

        // Registrar la salida (NOTA: Usamos egresar estándar, pero añadimos contexto
        // presupuestal.
        // Task 11 pide AC imputation validation.
        // InventarioItem.egresarPorRequisicion toma partidaId. egresar() no.
        // Asumimos flujo directo sin requisición formal para este método de servicio
        // general?
        // O usamos egresar() y hacemos el binding en el evento?
        // El metodo egresarPorRequisicion requiere requisicionId. Aquí no tenemos
        // requisicionId.
        // Usaremos egresar() normal y publicaremos el evento AC manualmente.

        MovimientoInventario movimiento = inventarioItem.egresar(cantidad, referencia);

        // Persistir (optimistic locking)
        inventarioRepository.save(inventarioItem);

        // Publicar evento AC
        MaterialConsumed event = new MaterialConsumed(proyectoId, partidaId, inventarioItem.getRecursoExternalId(),
                cantidad, movimiento.getCostoTotal(), movimiento.getFechaHora(), referencia);
        acPublisher.publicar(event);

        // Verificar semáforo presupuestal
        double porcentajeEjecucion = partidaValidator.getPorcentajeEjecucion(partidaId);
        // Alertar si > 80%
        if (porcentajeEjecucion > 80.0) {
            BudgetAlertSeverity severity = porcentajeEjecucion > 100.0 ? BudgetAlertSeverity.CRITICAL
                    : BudgetAlertSeverity.WARNING;

            BudgetAlertEvent alertEvent = new BudgetAlertEvent(proyectoId, partidaId, porcentajeEjecucion, severity,
                    LocalDateTime.now());
            budgetAlertPublisher.publicar(alertEvent);
        }
    }

    /**
     * Registra un ajuste de inventario (positivo o negativo).
     * 
     * Requiere roles: ADMIN_BODEGA o PROJECT_MANAGER. Requiere justificación
     * detallada (min 20 chars).
     * 
     * @param inventarioId  ID del item a ajustar
     * @param cantidad      Cantidad a ajustar (positiva o negativa)
     * @param justificacion Motivo del ajuste
     * @param referencia    Referencia opcional
     * @throws SecurityException        si no tiene permisos
     * @throws IllegalArgumentException si la justificación es corta
     */
    public void registrarAjuste(InventarioId inventarioId, BigDecimal cantidad, String justificacion,
            String referencia) {
        // 1. RBAC Check
        if (!securityPort.hasAnyRole(Set.of("ADMIN_BODEGA", "PROJECT_MANAGER"))) {
            throw new SecurityException("No tiene permisos para realizar ajustes de inventario");
        }

        // 2. Justificación check (redundant with domain but fails fast in service layer
        // for UI feedback)
        if (justificacion == null || justificacion.trim().length() < 20) {
            throw new IllegalArgumentException("La justificación debe tener al menos 20 caracteres");
        }

        InventarioItem item = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado: " + inventarioId));

        // 3. Crear Ajuste (Domain Logic)
        // Adjust quantity directly? The existing methods are ingresar/egresar.
        // We need a specific adjustment method in domain or use Movement Factory
        // directly?
        // Let's use MovimientoInventario.crearAjuste but how to apply to item?
        // InventarioItem doesn't have "ajustar" method yet.
        // We should add 'ajustar' to InventarioItem or handle logic here if allowed.
        // Best practice: Add 'ajustar' to aggregate root.
        // For now, I will modify InventarioItem to support 'ajustar'.
        // Wait, I can't modify InventarioItem in this tool call.
        // I will assume InventarioItem has 'ajustar' method or I add it in next step.
        // I will implement this logic assuming I'll add the method to InventarioItem
        // immediately after.

        // Actually, let's defer the call to item.ajustar() and just modify
        // InventarioItem first?
        // No, I'm already editing the service.
        // I will implement the service call: item.ajustar(...)
        item.ajustar(cantidad, justificacion, referencia);

        inventarioRepository.save(item);
    }

}
