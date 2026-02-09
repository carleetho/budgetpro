package com.budgetpro.domain.logistica.compra.service;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId;
import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityAuditLog;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import com.budgetpro.domain.shared.port.out.ObservabilityPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Dominio para procesar compras.
 */
public class ProcesarCompraService {

    private final PartidaRepository partidaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final IntegrityHashService integrityHashService;
    private final IntegrityAuditLog auditLog;
    private final GestionInventarioService gestionInventarioService;
    private final ObservabilityPort observability;

    public ProcesarCompraService(PartidaRepository partidaRepository, PresupuestoRepository presupuestoRepository,
            IntegrityHashService integrityHashService, IntegrityAuditLog auditLog,
            GestionInventarioService gestionInventarioService, ObservabilityPort observability) {
        this.partidaRepository = partidaRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.integrityHashService = integrityHashService;
        this.auditLog = auditLog;
        this.gestionInventarioService = gestionInventarioService;
        this.observability = observability;
    }

    public record CompraProcesada(Compra compra, List<ConsumoPartida> consumos) {
        public CompraProcesada {
            Objects.requireNonNull(compra, "La compra no puede ser nula");
            Objects.requireNonNull(consumos, "La lista de consumos no puede ser nula");
        }
    }

    /**
     * Procesa una compra y genera los consumos presupuestales correspondientes.
     */
    public CompraProcesada procesar(Compra compra, Billetera billetera) {
        // CRÍTICO: Validar integridad criptográfica del presupuesto ANTES de procesar
        Presupuesto presupuesto = presupuestoRepository.findByProyectoId(compra.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Presupuesto no encontrado para proyecto: %s", compra.getProyectoId())));

        // Validar integridad del presupuesto (solo si fue aprobado y tiene hash)
        if (presupuesto.isAprobado()) {
            String correlationId = observability.generateCorrelationId();
            long validationStartTime = System.currentTimeMillis();

            try {
                presupuesto.validarIntegridad(integrityHashService);

                // Registrar validación exitosa en audit log
                long validationDuration = System.currentTimeMillis() - validationStartTime;
                observability.recordMetrics("budget.integrity.validation", (double) validationDuration, "status",
                        "success");
                observability.logEvent("HASH_VALIDATION_SUCCESS",
                        String.format("Purchase approval validation for compra %s", compra.getId().getValue()));
                auditLog.logHashValidation(presupuesto, null, true,
                        String.format("Purchase approval validation for compra %s", compra.getId().getValue()));
            } catch (BudgetIntegrityViolationException e) {
                // Registrar violación en audit log
                long validationDuration = System.currentTimeMillis() - validationStartTime;
                observability.recordMetrics("budget.integrity.violation", 1.0, "type", e.getViolationType());
                observability.logError(correlationId, "logistica.compra", "purchase_approval",
                        compra.getProyectoId().toString(), e);
                auditLog.logIntegrityViolation(e, null);
                throw e; // Prevenir aprobación de compra sobre presupuesto comprometido
            }
        }

        List<ConsumoPartida> consumos = new ArrayList<>();

        // Agregar totales por partida usando Map para mejor type safety
        Map<PartidaId, PartidaData> partidasData = agregarTotalesPorPartida(compra);
        List<CompraDetalle> detallesConPartida = filtrarDetallesConPartida(compra);

        // Validar saldos disponibles sin mutar estado (atomicidad)
        for (Map.Entry<PartidaId, PartidaData> entry : partidasData.entrySet()) {
            Partida partida = entry.getValue().partida;
            BigDecimal totalPartida = entry.getValue().total;
            if (totalPartida.compareTo(partida.getSaldoDisponible()) > 0) {
                throw new SaldoInsuficienteException(compra.getProyectoId(), partida.getSaldoDisponible(), totalPartida,
                        String.format("Partida %s", partida.getId().getValue()));
            }
        }

        // Reservar saldo y persistir partidas
        for (Map.Entry<PartidaId, PartidaData> entry : partidasData.entrySet()) {
            Partida partida = entry.getValue().partida;
            BigDecimal totalPartida = entry.getValue().total;
            Partida partidaReservada = partida.reservarSaldo(totalPartida);
            partidaRepository.save(partidaReservada);
        }

        // Crear consumos por detalle
        for (CompraDetalle detalle : detallesConPartida) {
            ConsumoPartidaId consumoId = ConsumoPartidaId.nuevo();
            ConsumoPartida consumo = ConsumoPartida.crearPorCompra(consumoId, detalle.getPartidaId(),
                    detalle.getId().getValue(), detalle.getSubtotal(), compra.getFecha());
            consumos.add(consumo);
        }

        // Descontar de la billetera
        // Usar el método egresar que valida saldo, integridad y crea el movimiento
        billetera.egresar(compra.getTotal(),
                String.format("Compra #%s - %s", compra.getId().getValue(), compra.getProveedor()), null, // evidenciaUrl
                presupuesto.getId(), true); // true because we validated it above

        // Aprobar la compra
        compra.aprobar();
        Compra compraAprobada = compra;

        // CRÍTICO: Registrar entrada en Inventario (Kardex físico)
        Map<UUID, BigDecimal> cantidadesRecibidas = new HashMap<>();
        for (CompraDetalle detalle : compraAprobada.getDetalles()) {
            cantidadesRecibidas.put(detalle.getId().getValue(), detalle.getCantidad());
        }
        gestionInventarioService.registrarEntradaPorCompra(compraAprobada, cantidadesRecibidas);

        // Actualizar hash de ejecución del presupuesto después de cambios financieros
        if (presupuesto.isAprobado()) {
            presupuesto.actualizarHashEjecucion(integrityHashService);
            presupuestoRepository.save(presupuesto);
        }

        return new CompraProcesada(compraAprobada, consumos);
    }

    public List<PartidaId> validarSaldoPartidas(Compra compra) {
        List<PartidaId> partidasSinSaldo = new ArrayList<>();
        Map<PartidaId, PartidaData> partidasData = agregarTotalesPorPartida(compra);

        for (Map.Entry<PartidaId, PartidaData> entry : partidasData.entrySet()) {
            Partida partida = entry.getValue().partida;
            BigDecimal totalPartida = entry.getValue().total;
            if (totalPartida.compareTo(partida.getSaldoDisponible()) > 0) {
                partidasSinSaldo.add(partida.getId());
            }
        }
        return partidasSinSaldo;
    }

    private Map<PartidaId, PartidaData> agregarTotalesPorPartida(Compra compra) {
        Map<PartidaId, PartidaData> partidasData = new HashMap<>();

        for (CompraDetalle detalle : compra.getDetalles()) {
            if (detalle.getPartidaId() == null) {
                continue;
            }

            PartidaId partidaId = PartidaId.from(detalle.getPartidaId());
            Partida partida = partidaRepository.findById(partidaId).orElseThrow(() -> new IllegalArgumentException(
                    String.format("Partida no encontrada: %s", detalle.getPartidaId())));

            partidasData.compute(partidaId, (key, existing) -> {
                if (existing == null) {
                    return new PartidaData(partida, detalle.getSubtotal());
                } else {
                    return new PartidaData(partida, existing.total.add(detalle.getSubtotal()));
                }
            });
        }
        return partidasData;
    }

    private List<CompraDetalle> filtrarDetallesConPartida(Compra compra) {
        List<CompraDetalle> detallesConPartida = new ArrayList<>();
        for (CompraDetalle detalle : compra.getDetalles()) {
            if (detalle.getPartidaId() != null) {
                detallesConPartida.add(detalle);
            }
        }
        return detallesConPartida;
    }

    private static class PartidaData {
        final Partida partida;
        final BigDecimal total;

        PartidaData(Partida partida, BigDecimal total) {
            this.partida = partida;
            this.total = total;
        }
    }
}
