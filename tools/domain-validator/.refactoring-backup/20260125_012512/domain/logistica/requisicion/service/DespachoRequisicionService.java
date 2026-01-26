package com.budgetpro.domain.logistica.requisicion.service;

import com.budgetpro.domain.logistica.backlog.model.PrioridadCompra;
import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.service.ImputacionService;
import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.requisicion.exception.RequisicionNoAprobadaException;
import com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion;
import com.budgetpro.domain.logistica.requisicion.model.Requisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionItem;
import com.budgetpro.domain.logistica.requisicion.port.out.RequisicionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de dominio que orquesta el despacho de requisiciones.
 * 
 * Responsabilidades: - Validar que la requisición esté aprobada - Validar stock
 * disponible antes de despachar - Registrar despachos parciales o totales -
 * Actualizar estado de la requisición automáticamente - Transicionar a
 * PENDIENTE_COMPRA cuando stock insuficiente
 * 
 * No persiste directamente; orquesta y delega a repositorios.
 */
public final class DespachoRequisicionService {

        private final RequisicionRepository requisicionRepository;
        private final InventarioRepository inventarioRepository;
        private final DefaultBodegaPort defaultBodegaPort;
        private final BacklogService backlogService;
        private final ImputacionService imputacionService;

        public DespachoRequisicionService(RequisicionRepository requisicionRepository,
                        InventarioRepository inventarioRepository, DefaultBodegaPort defaultBodegaPort,
                        BacklogService backlogService, ImputacionService imputacionService) {
                this.requisicionRepository = Objects.requireNonNull(requisicionRepository, "requisicionRepository");
                this.inventarioRepository = Objects.requireNonNull(inventarioRepository, "inventarioRepository");
                this.defaultBodegaPort = Objects.requireNonNull(defaultBodegaPort, "defaultBodegaPort");
                this.backlogService = Objects.requireNonNull(backlogService, "backlogService");
                this.imputacionService = Objects.requireNonNull(imputacionService, "imputacionService");
        }

        /**
         * Despacha ítems de una requisición aprobada.
         * 
         * <p>
         * Flujo: 1. Valida que la requisición esté en estado válido (APROBADA,
         * DESPACHADA_PARCIAL, PENDIENTE_COMPRA) 2. Para cada DespachoItem: - Busca el
         * RequisicionItem en la requisición - Busca InventarioItem por (proyectoId,
         * recursoExternalId, unidadMedida, bodegaId) - Valida stock disponible - Si
         * insuficiente: transiciona requisición a PENDIENTE_COMPRA y lanza excepción -
         * Si suficiente: egresa del inventario y actualiza cantidadDespachada 3.
         * Actualiza estado de la requisición según progreso (DESPACHADA_PARCIAL o
         * DESPACHADA_TOTAL) 4. Persiste cambios
         * 
         * @param requisicionId ID de la requisición a despachar
         * @param items         Lista de ítems a despachar con sus cantidades
         * @throws RequisicionNoAprobadaException si la requisición no está en estado
         *                                        válido
         * @throws CantidadInsuficienteException  si no hay stock suficiente
         *                                        (transiciona a PENDIENTE_COMPRA)
         * @throws IllegalArgumentException       si algún ítem no existe o cantidad
         *                                        inválida
         */
        public void despacharRequisicion(RequisicionId requisicionId, List<DespachoItem> items) {
                if (items == null || items.isEmpty()) {
                        throw new IllegalArgumentException("La lista de ítems a despachar no puede estar vacía");
                }

                Requisicion requisicion = requisicionRepository.findById(requisicionId).orElseThrow(
                                () -> new IllegalArgumentException("Requisición no encontrada: " + requisicionId));

                // Validar estado de la requisición
                EstadoRequisicion estado = requisicion.getEstado();
                if (estado != EstadoRequisicion.APROBADA && estado != EstadoRequisicion.DESPACHADA_PARCIAL
                                && estado != EstadoRequisicion.PENDIENTE_COMPRA) {
                        throw new RequisicionNoAprobadaException(requisicionId, estado);
                }

                // Obtener bodega por defecto del proyecto
                BodegaId bodegaId = defaultBodegaPort.getDefaultForProject(requisicion.getProyectoId()).orElseThrow(
                                () -> new IllegalArgumentException("No hay bodega por defecto para el proyecto "
                                                + requisicion.getProyectoId()));

                // Procesar cada ítem a despachar
                for (DespachoItem despachoItem : items) {
                        RequisicionItem requisicionItem = requisicion.getItems().stream()
                                        .filter(item -> item.getId().equals(despachoItem.getRequisicionItemId()))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Ítem no encontrado en la requisición: "
                                                                        + despachoItem.getRequisicionItemId()));

                        // Validar que no se exceda la cantidad solicitada
                        BigDecimal cantidadPendiente = requisicionItem.getCantidadPendiente();
                        if (despachoItem.getCantidadADespachar().compareTo(cantidadPendiente) > 0) {
                                throw new IllegalArgumentException(String.format(
                                                "No se puede despachar %s. Cantidad pendiente: %s de %s solicitados",
                                                despachoItem.getCantidadADespachar(), cantidadPendiente,
                                                requisicionItem.getCantidadSolicitada()));
                        }

                        // Buscar InventarioItem por recursoExternalId, unidadMedida y bodegaId
                        InventarioItem inventarioItem = inventarioRepository
                                        .findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                                                        requisicion.getProyectoId(),
                                                        requisicionItem.getRecursoExternalId(),
                                                        requisicionItem.getUnidadMedida(), bodegaId)
                                        .orElseThrow(() -> new IllegalArgumentException(String.format(
                                                        "Inventario no encontrado para recurso %s, unidad %s, bodega %s",
                                                        requisicionItem.getRecursoExternalId(),
                                                        requisicionItem.getUnidadMedida(), bodegaId)));

                        // Validar stock disponible ANTES de intentar egresar
                        if (inventarioItem.getCantidadFisica().compareTo(despachoItem.getCantidadADespachar()) < 0) {
                                // Stock insuficiente: transicionar a PENDIENTE_COMPRA y crear
                                // RequerimientoCompra
                                if (requisicion.getEstado() == EstadoRequisicion.APROBADA
                                                || requisicion.getEstado() == EstadoRequisicion.DESPACHADA_PARCIAL) {
                                        requisicion.marcarPendienteCompra();
                                        requisicionRepository.save(requisicion);

                                        // Crear RequerimientoCompra automáticamente con prioridad URGENTE
                                        BigDecimal cantidadNecesaria = requisicionItem.getCantidadPendiente();
                                        backlogService.crearRequerimiento(requisicion.getProyectoId(),
                                                        requisicion.getId(), requisicionItem.getRecursoExternalId(),
                                                        cantidadNecesaria, requisicionItem.getUnidadMedida(),
                                                        PrioridadCompra.URGENTE);
                                }
                                throw new CantidadInsuficienteException(String.format(
                                                "Stock insuficiente para despachar %s de %s. "
                                                                + "Disponible: %s, Requerido: %s. Requisición marcada como PENDIENTE_COMPRA y RequerimientoCompra creado.",
                                                despachoItem.getCantidadADespachar(),
                                                requisicionItem.getRecursoExternalId(),
                                                inventarioItem.getCantidadFisica(),
                                                despachoItem.getCantidadADespachar()));
                        }

                        // Egresar del inventario con referencias a requisición
                        String referencia = String.format("Despacho requisición #%s - %s", requisicionId.getValue(),
                                        requisicion.getSolicitante());
                        inventarioItem.egresarPorRequisicion(despachoItem.getCantidadADespachar(),
                                        requisicionId.getValue(), requisicionItem.getId().getValue(),
                                        requisicionItem.getPartidaId(), referencia);

                        // Validar Imputación y Registrar Costo Real (AC)
                        // Si tiene Partida ID, asumimos GASTO_DIRECTO_PARTIDA
                        NaturalezaGasto naturaleza = requisicionItem.getPartidaId() != null
                                        ? NaturalezaGasto.DIRECTO_PARTIDA
                                        : NaturalezaGasto.GENERAL_OBRA;

                        imputacionService.validarYRegistrarAC(requisicionItem.getPartidaId(), naturaleza,
                                        despachoItem.getCantidadADespachar(), inventarioItem.getCostoPromedio(),
                                        referencia);

                        // Actualizar cantidadDespachada en el RequisicionItem
                        requisicion.registrarDespacho(requisicionItem.getId(), despachoItem.getCantidadADespachar());

                        // Persistir inventario (con movimiento nuevo)
                        inventarioRepository.save(inventarioItem);
                }

                // Persistir requisición (con estado y cantidadDespachada actualizados)
                requisicionRepository.save(requisicion);
        }
}
