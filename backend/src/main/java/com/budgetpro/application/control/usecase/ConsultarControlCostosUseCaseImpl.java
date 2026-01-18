package com.budgetpro.application.control.usecase;

import com.budgetpro.application.control.dto.ReporteControlCostosResponse;
import com.budgetpro.application.control.dto.ReportePartidaDTO;
import com.budgetpro.application.control.port.in.ConsultarControlCostosUseCase;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.domain.finanzas.control.service.AgregacionControlCostosService;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.port.out.ConsumoPartidaRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para consultar el reporte de control de costos.
 * 
 * OPTIMIZACIÓN ANTI-N+1:
 * - Carga todas las Partida del presupuesto en una query
 * - Carga todos los ConsumoPartida del proyecto en una query
 * - Hace el cruce y agregación en memoria (Java Streams)
 */
@Service
public class ConsultarControlCostosUseCaseImpl implements ConsultarControlCostosUseCase {

    private final PresupuestoRepository presupuestoRepository;
    private final PartidaRepository partidaRepository;
    private final ConsumoPartidaRepository consumoPartidaRepository;
    private final AgregacionControlCostosService agregacionService;

    public ConsultarControlCostosUseCaseImpl(
            PresupuestoRepository presupuestoRepository,
            PartidaRepository partidaRepository,
            ConsumoPartidaRepository consumoPartidaRepository,
            AgregacionControlCostosService agregacionService) {
        this.presupuestoRepository = presupuestoRepository;
        this.partidaRepository = partidaRepository;
        this.consumoPartidaRepository = consumoPartidaRepository;
        this.agregacionService = agregacionService;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteControlCostosResponse consultar(UUID presupuestoId) {
        // 1. Validar que el presupuesto existe
        Presupuesto presupuesto = presupuestoRepository.findById(PresupuestoId.from(presupuestoId))
                .orElseThrow(() -> new PresupuestoNoEncontradoException(presupuestoId));

        // 2. OPTIMIZACIÓN: Cargar todas las Partida del presupuesto en una query
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);
        
        if (partidas.isEmpty()) {
            return new ReporteControlCostosResponse(
                presupuestoId,
                presupuesto.getNombre(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Collections.emptyList()
            );
        }

        // 3. OPTIMIZACIÓN: Cargar todos los ConsumoPartida del proyecto en una query
        // Extraer IDs de partidas
        Set<UUID> partidaIds = partidas.stream()
                .map(p -> p.getId().getValue())
                .collect(Collectors.toSet());
        
        // Cargar consumos de todas las partidas (evita N+1)
        List<ConsumoPartida> consumos = new ArrayList<>();
        for (UUID partidaId : partidaIds) {
            consumos.addAll(consumoPartidaRepository.findByPartidaId(partidaId));
        }

        // 4. Agregar datos de control (Plan vs Real) usando el servicio de dominio
        Map<UUID, AgregacionControlCostosService.DatosControlPartida> datosPorPartida =
                agregacionService.agregarDatosControl(partidas, consumos);

        // 5. Construir estructura jerárquica de DTOs
        List<ReportePartidaDTO> partidasDTO = construirJerarquia(partidas, datosPorPartida);

        // 6. Calcular totales (suma de partidas raíz)
        List<Partida> partidasRaiz = partidas.stream()
                .filter(p -> p.getPadreId() == null)
                .collect(Collectors.toList());
        
        BigDecimal totalPlan = partidasRaiz.stream()
                .map(p -> datosPorPartida.get(p.getId().getValue()))
                .filter(Objects::nonNull)
                .map(AgregacionControlCostosService.DatosControlPartida::parcialPlan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalReal = partidasRaiz.stream()
                .map(p -> datosPorPartida.get(p.getId().getValue()))
                .filter(Objects::nonNull)
                .map(AgregacionControlCostosService.DatosControlPartida::gastoAcumulado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSaldo = totalPlan.subtract(totalReal);
        BigDecimal porcentajeEjecucionTotal = BigDecimal.ZERO;
        if (totalPlan.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeEjecucionTotal = totalReal
                    .divide(totalPlan, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        return new ReporteControlCostosResponse(
            presupuestoId,
            presupuesto.getNombre(),
            totalPlan,
            totalReal,
            totalSaldo,
            porcentajeEjecucionTotal,
            partidasDTO
        );
    }

    /**
     * Construye la estructura jerárquica de partidas con sus datos de control.
     */
    private List<ReportePartidaDTO> construirJerarquia(
            List<Partida> partidas,
            Map<UUID, AgregacionControlCostosService.DatosControlPartida> datosPorPartida) {
        
        // Crear mapa de partidas por ID
        Map<UUID, Partida> partidasPorId = partidas.stream()
                .collect(Collectors.toMap(p -> p.getId().getValue(), p -> p));
        
        // Crear mapa de hijos por padreId
        Map<UUID, List<Partida>> hijosPorPadre = partidas.stream()
                .filter(p -> p.getPadreId() != null)
                .collect(Collectors.groupingBy(Partida::getPadreId));
        
        // Construir DTOs recursivamente (desde raíz hacia abajo)
        List<Partida> partidasRaiz = partidas.stream()
                .filter(p -> p.getPadreId() == null)
                .sorted(Comparator.comparing(Partida::getItem))
                .collect(Collectors.toList());
        
        return partidasRaiz.stream()
                .map(partida -> construirDTO(partida, datosPorPartida, hijosPorPadre))
                .collect(Collectors.toList());
    }

    /**
     * Construye un DTO de partida recursivamente (incluye hijos).
     */
    private ReportePartidaDTO construirDTO(
            Partida partida,
            Map<UUID, AgregacionControlCostosService.DatosControlPartida> datosPorPartida,
            Map<UUID, List<Partida>> hijosPorPadre) {
        
        AgregacionControlCostosService.DatosControlPartida datos =
                datosPorPartida.get(partida.getId().getValue());
        
        if (datos == null) {
            datos = new AgregacionControlCostosService.DatosControlPartida(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
        }
        
        // Construir hijos recursivamente
        List<Partida> hijos = hijosPorPadre.getOrDefault(
                partida.getId().getValue(), Collections.emptyList());
        
        List<ReportePartidaDTO> hijosDTO = hijos.stream()
                .sorted(Comparator.comparing(Partida::getItem))
                .map(hijo -> construirDTO(hijo, datosPorPartida, hijosPorPadre))
                .collect(Collectors.toList());
        
        return new ReportePartidaDTO(
            partida.getId().getValue(),
            partida.getItem(),
            partida.getDescripcion(),
            partida.getUnidad(),
            partida.getNivel(),
            datos.metrado(),
            datos.precioUnitario(),
            datos.parcialPlan(),
            datos.gastoAcumulado(),
            datos.saldo(),
            datos.porcentajeEjecucion(),
            partida.getPadreId(),
            hijosDTO
        );
    }
}
