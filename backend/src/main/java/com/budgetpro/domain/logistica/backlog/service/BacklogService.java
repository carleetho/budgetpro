package com.budgetpro.domain.logistica.backlog.service;

import com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento;
import com.budgetpro.domain.logistica.backlog.model.PrioridadCompra;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompra;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompraId;
import com.budgetpro.domain.logistica.backlog.port.out.RequerimientoCompraRepository;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion;
import com.budgetpro.domain.logistica.requisicion.model.Requisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;
import com.budgetpro.domain.logistica.requisicion.port.out.RequisicionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de dominio que gestiona el backlog de inventario mediante RequerimientoCompra.
 * 
 * Responsabilidades:
 * - Crear RequerimientoCompra automáticamente cuando hay stock insuficiente
 * - Resolver backlog cuando llega stock: transicionar requisiciones PENDIENTE_COMPRA → APROBADA
 * - Marcar RequerimientoCompra como RECIBIDA cuando se resuelve
 * 
 * No persiste directamente; orquesta y delega a repositorios.
 */
public final class BacklogService {

    private final RequerimientoCompraRepository requerimientoCompraRepository;
    private final RequisicionRepository requisicionRepository;
    private final InventarioRepository inventarioRepository;

    public BacklogService(RequerimientoCompraRepository requerimientoCompraRepository,
                         RequisicionRepository requisicionRepository,
                         InventarioRepository inventarioRepository) {
        this.requerimientoCompraRepository = Objects.requireNonNull(requerimientoCompraRepository, "requerimientoCompraRepository");
        this.requisicionRepository = Objects.requireNonNull(requisicionRepository, "requisicionRepository");
        this.inventarioRepository = Objects.requireNonNull(inventarioRepository, "inventarioRepository");
    }

    /**
     * Crea un RequerimientoCompra automáticamente cuando hay stock insuficiente.
     * 
     * @param proyectoId ID del proyecto
     * @param requisicionId ID de la requisición que originó el requerimiento
     * @param recursoExternalId ID externo del recurso
     * @param cantidadNecesaria Cantidad que se necesita comprar
     * @param unidadMedida Unidad de medida
     * @param prioridad Prioridad del requerimiento (típicamente URGENTE cuando stock = 0)
     * @return El RequerimientoCompra creado
     */
    public RequerimientoCompra crearRequerimiento(UUID proyectoId, RequisicionId requisicionId,
                                                  String recursoExternalId, BigDecimal cantidadNecesaria,
                                                  String unidadMedida, PrioridadCompra prioridad) {
        RequerimientoCompraId id = RequerimientoCompraId.generate();
        RequerimientoCompra requerimiento = RequerimientoCompra.crear(
                id, proyectoId, requisicionId, recursoExternalId, cantidadNecesaria, unidadMedida, prioridad
        );
        requerimientoCompraRepository.save(requerimiento);
        return requerimiento;
    }

    /**
     * Resuelve el backlog cuando llega stock para un recurso.
     * 
     * <p>Flujo:
     * 1. Busca RequerimientoCompra pendientes para el recurso
     * 2. Verifica si hay stock suficiente en inventario
     * 3. Si hay stock: transiciona Requisicion PENDIENTE_COMPRA → APROBADA
     * 4. Marca RequerimientoCompra como RECIBIDA
     * 
     * @param proyectoId ID del proyecto
     * @param recursoExternalId ID externo del recurso
     * @param unidadMedida Unidad de medida
     */
    public void resolverBacklog(UUID proyectoId, String recursoExternalId, String unidadMedida) {
        // Buscar requerimientos pendientes para este recurso
        List<RequerimientoCompra> requerimientosPendientes = requerimientoCompraRepository
                .findPendientesPorRecurso(proyectoId, recursoExternalId, unidadMedida);

        if (requerimientosPendientes.isEmpty()) {
            return; // No hay backlog para este recurso
        }

        // Buscar inventario actual para verificar stock
        // Necesitamos bodegaId, pero no lo tenemos aquí. Por ahora, asumimos que hay stock
        // si existe algún InventarioItem con cantidad > 0 para este recurso.
        // En una implementación completa, deberíamos pasar bodegaId o buscar por proyecto.
        var inventarioOpt = inventarioRepository.findByProyectoId(proyectoId).stream()
                .filter(item -> item.getRecursoExternalId().equals(recursoExternalId) &&
                               item.getUnidadBase().equals(unidadMedida) &&
                               item.getCantidadFisica().compareTo(BigDecimal.ZERO) > 0)
                .findFirst();

        if (inventarioOpt.isEmpty()) {
            return; // Aún no hay stock
        }

        InventarioItem inventario = inventarioOpt.get();
        BigDecimal stockDisponible = inventario.getCantidadFisica();

        // Procesar cada requerimiento pendiente
        for (RequerimientoCompra requerimiento : requerimientosPendientes) {
            // Verificar si hay stock suficiente para este requerimiento
            if (stockDisponible.compareTo(requerimiento.getCantidadNecesaria()) >= 0) {
                // Resolver: transicionar requisición y marcar requerimiento como recibido
                Requisicion requisicion = requisicionRepository.findById(requerimiento.getRequisicionId())
                        .orElse(null);

                if (requisicion != null && requisicion.getEstado() == EstadoRequisicion.PENDIENTE_COMPRA) {
                    // Transicionar a APROBADA (el stock ya está disponible)
                    requisicion.reactivar();
                    requisicionRepository.save(requisicion);
                }

                // Marcar requerimiento como recibido
                requerimiento.marcarRecibido();
                requerimientoCompraRepository.save(requerimiento);
            }
        }
    }

    /**
     * Resuelve backlog para una requisición específica cuando llega stock.
     * Versión más específica que resuelve una requisición individual.
     * 
     * @param requisicionId ID de la requisición a reactivar
     */
    public void resolverBacklogParaRequisicion(RequisicionId requisicionId) {
        Requisicion requisicion = requisicionRepository.findById(requisicionId)
                .orElse(null);

        if (requisicion == null || requisicion.getEstado() != EstadoRequisicion.PENDIENTE_COMPRA) {
            return; // No hay nada que resolver
        }

        // Buscar requerimientos pendientes para esta requisición
        List<RequerimientoCompra> requerimientos = requerimientoCompraRepository
                .findByRequisicionId(requisicionId.getValue());

        if (requerimientos.isEmpty()) {
            return;
        }

        // Verificar stock para cada ítem de la requisición
        boolean todosResueltos = true;
        for (var item : requisicion.getItems()) {
            var inventarioOpt = inventarioRepository.findByProyectoId(requisicion.getProyectoId()).stream()
                    .filter(inv -> inv.getRecursoExternalId().equals(item.getRecursoExternalId()) &&
                                  inv.getUnidadBase().equals(item.getUnidadMedida()) &&
                                  inv.getCantidadFisica().compareTo(item.getCantidadPendiente()) >= 0)
                    .findFirst();

            if (inventarioOpt.isEmpty()) {
                todosResueltos = false;
                break;
            }
        }

        // Si hay stock para todos los ítems, reactivar requisición y marcar requerimientos como recibidos
        if (todosResueltos) {
            requisicion.reactivar();
            requisicionRepository.save(requisicion);

            for (RequerimientoCompra requerimiento : requerimientos) {
                if (requerimiento.getEstado() != EstadoRequerimiento.RECIBIDA) {
                    requerimiento.marcarRecibido();
                    requerimientoCompraRepository.save(requerimiento);
                }
            }
        }
    }
}
