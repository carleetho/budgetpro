package com.budgetpro.domain.finanzas.estimacion.service;

import com.budgetpro.domain.finanzas.estimacion.event.*;
import com.budgetpro.domain.finanzas.estimacion.exception.EstimacionCongeladaException;
import com.budgetpro.domain.finanzas.estimacion.exception.EstimacionNoEncontradaException;
import com.budgetpro.domain.finanzas.estimacion.exception.PresupuestoNoCongeladoException;
import com.budgetpro.domain.finanzas.estimacion.model.*;
import com.budgetpro.domain.finanzas.estimacion.port.AvancePartidaRepository;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.domain.finanzas.estimacion.port.EstimacionSnapshotRepository;
import com.budgetpro.domain.finanzas.estimacion.port.EventPublisher;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de dominio principal para la gestión de Estimaciones. Orquesta el
 * ciclo de vida: Crear -> Aprobar -> Facturar -> (Anular).
 */
public class EstimacionService {

    private final EstimacionRepository estimacionRepository;
    private final EstimacionSnapshotRepository snapshotRepository;
    private final AvancePartidaRepository avancePartidaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final EventPublisher eventPublisher;

    private final PeriodoValidatorService periodoValidatorService;
    private final AvanceCalculatorService avanceCalculatorService;
    private final SnapshotGeneratorService snapshotGeneratorService;

    public EstimacionService(EstimacionRepository estimacionRepository, EstimacionSnapshotRepository snapshotRepository,
            AvancePartidaRepository avancePartidaRepository, PresupuestoRepository presupuestoRepository,
            EventPublisher eventPublisher, PeriodoValidatorService periodoValidatorService,
            AvanceCalculatorService avanceCalculatorService, SnapshotGeneratorService snapshotGeneratorService) {
        this.estimacionRepository = Objects.requireNonNull(estimacionRepository, "EstimacionRepo requerido");
        this.snapshotRepository = Objects.requireNonNull(snapshotRepository, "SnapshotRepo requerido");
        this.avancePartidaRepository = Objects.requireNonNull(avancePartidaRepository, "AvancePartidaRepo requerido");
        this.presupuestoRepository = Objects.requireNonNull(presupuestoRepository, "PresupuestoRepo requerido");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "EventPublisher requerido");
        this.periodoValidatorService = Objects.requireNonNull(periodoValidatorService, "PeriodoValidator requerido");
        this.avanceCalculatorService = Objects.requireNonNull(avanceCalculatorService, "AvanceCalculator requerido");
        this.snapshotGeneratorService = Objects.requireNonNull(snapshotGeneratorService, "SnapshotGenerator requerido");
    }

    public Estimacion crear(UUID proyectoId, PresupuestoId presupuestoId, PeriodoEstimacion periodo,
            RetencionPorcentaje retencion) {
        Objects.requireNonNull(proyectoId, "Proyecto ID requerido");
        Objects.requireNonNull(presupuestoId, "Presupuesto ID requerido");
        Objects.requireNonNull(periodo, "Periodo requerido");

        // 1. Validar que el presupuesto esté congelado/aprobado
        var presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado: " + presupuestoId));

        if (!presupuesto.isAprobado()) {
            throw new PresupuestoNoCongeladoException(presupuestoId);
        }

        // 2. Validar que no haya solapamiento de periodos
        periodoValidatorService.validateNoOverlap(proyectoId, periodo.getFechaInicio(), periodo.getFechaFin());

        // 3. Crear la estimación
        Estimacion estimacion = Estimacion.crear(presupuestoId, periodo, retencion);
        Estimacion saved = estimacionRepository.save(estimacion);

        eventPublisher.publish(new EstimacionCreadaEvent(saved.getId().getValue(), proyectoId, periodo));

        return saved;
    }

    public void agregarItem(EstimacionId estimacionId, UUID partidaId, String concepto, MontoEstimado montoContractual,
            PorcentajeAvance porcentajePeriodo) {

        Estimacion estimacion = estimacionRepository.findById(estimacionId)
                .orElseThrow(() -> new EstimacionNoEncontradaException(estimacionId));

        // Validar porcentaje acumulado
        avanceCalculatorService.validateAvanceNoExcede100(partidaId, porcentajePeriodo.getValue());

        // Get previous progress
        BigDecimal avancePrevioVal = avancePartidaRepository.calcularAvanceAcumulado(partidaId);
        PorcentajeAvance avancePrevio = PorcentajeAvance
                .of(avancePrevioVal != null ? avancePrevioVal : BigDecimal.ZERO);

        // Calculate previous amount
        MontoEstimado montoPrevio = avanceCalculatorService.calcularMontoEstimado(montoContractual, avancePrevio);

        EstimacionItem item = EstimacionItem.crear(partidaId, concepto, montoContractual, avancePrevio, montoPrevio);
        item.registrarAvance(porcentajePeriodo);

        estimacion.agregarItem(item);
        estimacionRepository.save(estimacion);
    }

    private UUID resolveProyectoId(PresupuestoId presupuestoId) {
        return presupuestoRepository.findById(presupuestoId).map(p -> p.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));
    }

    public EstimacionSnapshot aprobar(EstimacionId estimacionId, UUID aprobadoPor) {
        Estimacion estimacion = estimacionRepository.findById(estimacionId)
                .orElseThrow(() -> new EstimacionNoEncontradaException(estimacionId));

        if (estimacion.getEstado() != EstadoEstimacion.BORRADOR) {
            throw new EstimacionCongeladaException(estimacionId, estimacion.getEstado());
        }

        // Generar Snapshot
        EstimacionSnapshot snapshot = snapshotGeneratorService.generarSnapshot(estimacion);

        // Aprobar estimación
        estimacion.aprobar(aprobadoPor, snapshot.getItemsSnapshot(), snapshot.getTotalesSnapshot(),
                snapshot.getMetadataSnapshot());

        estimacionRepository.save(estimacion);
        snapshotRepository.save(snapshot);

        // Registrar Avances Históricos
        for (EstimacionItem item : estimacion.getItems()) {
            AvancePartida avance = AvancePartida.registrar(item.getPartidaId(), estimacionId,
                    item.getPorcentajeAcumulado(), item.getMontoAcumulado());
            avancePartidaRepository.save(avance);
        }

        UUID proyectoId = resolveProyectoId(estimacion.getPresupuestoId());

        eventPublisher.publish(new EstimacionAprobadaEvent(estimacionId.getValue(), proyectoId,
                estimacion.calcularTotalPagar(), estimacion.getItems(), LocalDateTime.now()));

        return snapshot;
    }

    public void facturar(EstimacionId estimacionId) {
        Estimacion estimacion = estimacionRepository.findById(estimacionId)
                .orElseThrow(() -> new EstimacionNoEncontradaException(estimacionId));

        estimacion.facturar();
        estimacionRepository.save(estimacion);

        UUID proyectoId = resolveProyectoId(estimacion.getPresupuestoId());
        eventPublisher.publish(new EstimacionFacturadaEvent(estimacionId.getValue(), proyectoId, LocalDateTime.now()));
    }

    public void anular(EstimacionId estimacionId, String motivo) {
        Estimacion estimacion = estimacionRepository.findById(estimacionId)
                .orElseThrow(() -> new EstimacionNoEncontradaException(estimacionId));

        estimacion.anular();
        estimacionRepository.save(estimacion);

        UUID proyectoId = resolveProyectoId(estimacion.getPresupuestoId());
        eventPublisher
                .publish(new EstimacionAnuladaEvent(estimacionId.getValue(), proyectoId, motivo, LocalDateTime.now()));
    }
}
