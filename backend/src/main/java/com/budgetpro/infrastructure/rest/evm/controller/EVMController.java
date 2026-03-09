package com.budgetpro.infrastructure.rest.evm.controller;

import com.budgetpro.application.evm.service.EVMCalculationService;
import com.budgetpro.application.finanzas.evm.port.in.CerrarPeriodoUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ForecastResult;
import com.budgetpro.application.finanzas.evm.port.in.ObtenerForecastFechaUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ObtenerSCurveUseCase;
import com.budgetpro.application.finanzas.evm.port.in.SCurveResult;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import com.budgetpro.infrastructure.rest.evm.dto.CerrarPeriodoRequest;
import com.budgetpro.infrastructure.rest.evm.dto.CerrarPeriodoResponse;
import com.budgetpro.infrastructure.rest.evm.dto.EVMSnapshotResponse;
import com.budgetpro.infrastructure.rest.evm.dto.ForecastResponse;
import com.budgetpro.infrastructure.rest.evm.dto.SCurveResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para el módulo EVM.
 */
@RestController
@RequestMapping("/api/v1/evm")
public class EVMController {

    private static final String STATUS_CERRADO = "CERRADO";

    private final EVMCalculationService evmCalculationService;
    private final ObtenerSCurveUseCase obtenerSCurveUseCase;
    private final ObtenerForecastFechaUseCase obtenerForecastFechaUseCase;
    private final CerrarPeriodoUseCase cerrarPeriodoUseCase;

    public EVMController(EVMCalculationService evmCalculationService, ObtenerSCurveUseCase obtenerSCurveUseCase,
            ObtenerForecastFechaUseCase obtenerForecastFechaUseCase, CerrarPeriodoUseCase cerrarPeriodoUseCase) {
        this.evmCalculationService = evmCalculationService;
        this.obtenerSCurveUseCase = obtenerSCurveUseCase;
        this.obtenerForecastFechaUseCase = obtenerForecastFechaUseCase;
        this.cerrarPeriodoUseCase = cerrarPeriodoUseCase;
    }

    /**
     * Calcula y retorna el snapshot de EVM para un proyecto y fecha de corte.
     */
    @GetMapping("/{proyectoId}")
    public ResponseEntity<EVMSnapshotResponse> obtenerMetricas(@PathVariable UUID proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte) {

        LocalDateTime corte = fechaCorte != null ? fechaCorte : LocalDateTime.now();
        EVMSnapshot snapshot = evmCalculationService.calcularYPersistir(proyectoId, corte);

        return ResponseEntity.ok(toResponse(snapshot));
    }

    @GetMapping("/{proyectoId}/s-curve")
    public ResponseEntity<SCurveResponse> getSCurve(
            @PathVariable UUID proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SCurveResult result = obtenerSCurveUseCase.obtener(proyectoId, startDate, endDate);
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * Obtiene la fecha de finalización proyectada basada en SPI y cronograma (REQ-63, UC-E05).
     */
    @GetMapping("/{proyectoId}/forecast")
    public ResponseEntity<ForecastResponse> getForecast(@PathVariable UUID proyectoId) {
        ForecastResult result = obtenerForecastFechaUseCase.obtener(proyectoId);
        return ResponseEntity.ok(toForecastResponse(result));
    }

    /**
     * Cierra un período de valuación para el proyecto (REQ-64, Invariante E-04).
     */
    @PostMapping("/{proyectoId}/cerrar-periodo")
    public ResponseEntity<CerrarPeriodoResponse> cerrarPeriodo(
            @PathVariable UUID proyectoId,
            @RequestBody CerrarPeriodoRequest request) {
        String periodoId = cerrarPeriodoUseCase.cerrar(proyectoId, request.fechaCorte());
        return ResponseEntity.ok(new CerrarPeriodoResponse(
                proyectoId,
                periodoId,
                request.fechaCorte(),
                STATUS_CERRADO));
    }

    private EVMSnapshotResponse toResponse(EVMSnapshot snapshot) {
        return new EVMSnapshotResponse(snapshot.getId().getValue(), snapshot.getProyectoId(), snapshot.getFechaCorte(),
                snapshot.getFechaCalculo(), snapshot.getPv(), snapshot.getEv(), snapshot.getAc(), snapshot.getBac(),
                snapshot.getCv(), snapshot.getSv(), snapshot.getCpi(), snapshot.getSpi(), snapshot.getEac(),
                snapshot.getEtc(), snapshot.getVac(), snapshot.getInterpretacion());
    }

    private SCurveResponse toResponse(SCurveResult result) {
        List<SCurveResponse.SCurveDataPoint> dataPoints = result.dataPoints().stream()
                .map(dp -> new SCurveResponse.SCurveDataPoint(
                        dp.fechaCorte(),
                        dp.periodo(),
                        dp.pvAcumulado(),
                        dp.evAcumulado(),
                        dp.acAcumulado(),
                        dp.cpiPeriodo(),
                        dp.spiPeriodo()))
                .toList();

        return new SCurveResponse(
                result.proyectoId(),
                result.moneda(),
                result.bacTotal(),
                result.bacAjustado(),
                dataPoints);
    }

    private ForecastResponse toForecastResponse(ForecastResult result) {
        return new ForecastResponse(
                result.proyectoId(),
                result.fechaCorteBase(),
                result.forecastCompletionDate(),
                result.fechaFinPlanificada(),
                result.remainingDays(),
                result.spiUsed(),
                result.forecastFallback());
    }
}
