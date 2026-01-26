package com.budgetpro.application.evm.service;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.port.out.ConsumoPartidaRepository;
import com.budgetpro.domain.finanzas.control.service.AgregacionControlCostosService;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshotId;
import com.budgetpro.domain.finanzas.evm.port.out.EVMSnapshotRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de cálculo de EVM.
 */
@Service
public class EVMCalculationServiceImpl implements EVMCalculationService {

    private final EVMSnapshotRepository evmSnapshotRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final EstimacionRepository estimacionRepository;
    private final ProgramaObraRepository programaObraRepository;
    private final PartidaRepository partidaRepository;
    private final ConsumoPartidaRepository consumoPartidaRepository;
    private final AgregacionControlCostosService controlCostosService;

    public EVMCalculationServiceImpl(EVMSnapshotRepository evmSnapshotRepository,
            PresupuestoRepository presupuestoRepository, EstimacionRepository estimacionRepository,
            ProgramaObraRepository programaObraRepository, PartidaRepository partidaRepository,
            ConsumoPartidaRepository consumoPartidaRepository, AgregacionControlCostosService controlCostosService) {
        this.evmSnapshotRepository = evmSnapshotRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.estimacionRepository = estimacionRepository;
        this.programaObraRepository = programaObraRepository;
        this.partidaRepository = partidaRepository;
        this.consumoPartidaRepository = consumoPartidaRepository;
        this.controlCostosService = controlCostosService;
    }

    @Override
    @Transactional
    public EVMSnapshot calcularYPersistir(UUID proyectoId, LocalDateTime fechaCorte) {
        // 1. Obtener Presupuesto y BAC
        Presupuesto presupuesto = presupuestoRepository.findByProyectoId(proyectoId).orElseThrow(
                () -> new IllegalStateException("Presupuesto no encontrado para el proyecto: " + proyectoId));

        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuesto.getId().getValue());

        // Usar AgregacionControlCostosService para obtener el BAC (parcialPlan total)
        // Necesitamos consumos aunque para BAC no influyan, el servicio los pide
        // En una implementación real, podríamos optimizar esto.
        Map<UUID, AgregacionControlCostosService.DatosControlPartida> datosControl = controlCostosService
                .agregarDatosControl(partidas, List.of());

        BigDecimal bac = partidas.stream().filter(p -> p.getPadreId() == null) // Raíces
                .map(p -> datosControl.get(p.getId().getValue()).parcialPlan())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Calcular AC (Actual Cost)
        // Obtenemos todos los consumos del proyecto
        List<ConsumoPartida> consumos = partidas.stream()
                .flatMap(p -> consumoPartidaRepository.findByPartidaId(p.getId().getValue()).stream())
                .filter(c -> !c.getFecha().isAfter(fechaCorte.toLocalDate())).collect(Collectors.toList());

        BigDecimal ac = consumos.stream().map(ConsumoPartida::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Calcular EV (Earned Value)
        // Buscamos la última estimación aprobada hasta la fecha de corte
        // TODO: Integrar con ReporteProduccion (RPC) para obtener avance físico en
        // tiempo real (AC14)
        List<Estimacion> estimacionesAprobadas = estimacionRepository.findAprobadasByProyectoId(proyectoId);
        Estimacion ultimaEstimacion = estimacionesAprobadas.stream()
                .filter(e -> !e.getFechaCorte().isAfter(fechaCorte.toLocalDate()))
                .max(Comparator.comparing(Estimacion::getNumeroEstimacion)).orElse(null);

        BigDecimal ev = BigDecimal.ZERO;
        if (ultimaEstimacion != null) {
            // EV = Sum (AcumuladoTotal_i * UnitPrice_i)
            // Según AC12: se calcula basándose en el acumulado total de las estimaciones
            ev = ultimaEstimacion.getDetalles().stream()
                    .map(d -> d.calcularAcumuladoTotal().multiply(d.getPrecioUnitario()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(4, RoundingMode.HALF_UP);
        }

        // 4. Calcular PV (Planned Value)
        // Cálculo pro-rata basado en Cronograma
        BigDecimal pv = calcularPVProRata(proyectoId, fechaCorte, bac);

        // 5. Verificar si ya existe un snapshot para esta fecha de corte (AC10)
        if (evmSnapshotRepository.existsByProyectoIdAndFechaCorte(proyectoId, fechaCorte)) {
            return evmSnapshotRepository.findByProyectoIdAndRango(proyectoId, fechaCorte, fechaCorte).get(0);
        }

        // 6. Crear y persistir el Snapshot
        EVMSnapshot snapshot = EVMSnapshot.calcular(EVMSnapshotId.nuevo(), proyectoId, fechaCorte, pv, ev, ac, bac);

        evmSnapshotRepository.save(snapshot);
        return snapshot;
    }

    private BigDecimal calcularPVProRata(UUID proyectoId, LocalDateTime fechaCorte, BigDecimal bac) {
        // TODO: AC9 - Implementar cálculo basado en calendario (días hábiles/feriados)
        // para mayor precisión
        return programaObraRepository.findByProyectoId(proyectoId).map(programa -> {
            if (programa.getFechaInicio() == null || programa.getFechaFinEstimada() == null) {
                return BigDecimal.ZERO;
            }

            long totalDays = ChronoUnit.DAYS.between(programa.getFechaInicio(), programa.getFechaFinEstimada()) + 1;
            long elapsedDays = ChronoUnit.DAYS.between(programa.getFechaInicio(), fechaCorte.toLocalDate()) + 1;

            if (elapsedDays <= 0)
                return BigDecimal.ZERO;
            if (elapsedDays >= totalDays)
                return bac;

            return bac.multiply(new BigDecimal(elapsedDays)).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP);
        }).orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public EVMSnapshot obtenerUltimo(UUID proyectoId) {
        return evmSnapshotRepository.findLatestByProyectoId(proyectoId).orElseThrow(
                () -> new IllegalStateException("No hay snapshots de EVM para el proyecto: " + proyectoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EVMSnapshot> obtenerHistorico(UUID proyectoId, LocalDateTime desde, LocalDateTime hasta) {
        return evmSnapshotRepository.findByProyectoIdAndRango(proyectoId, desde, hasta);
    }
}
