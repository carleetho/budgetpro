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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "EVM", description = """
        Earned Value (CPI/SPI/EAC, S-Curve, forecast, cierre de periodo).
        Madurez radiografía ~95%. Agrupado en Phase 2 de REQ-47 por roadmap histórico.
        ⚠️ Agregaciones dashboard / escenarios de varianza raros: validar con notebook EVM.
        """)
@RestController
@RequestMapping("/api/v1/evm")
@SecurityRequirement(name = "bearer-jwt")
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

    @Operation(summary = "Snapshot EVM", description = "Calcula y persiste métricas PV/EV/AC/CPI/SPI para fecha de corte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Snapshot",
                    content = @Content(schema = @Schema(implementation = EVMSnapshotResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping("/{proyectoId}")
    public ResponseEntity<EVMSnapshotResponse> obtenerMetricas(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCorte) {

        LocalDateTime corte = fechaCorte != null ? fechaCorte : LocalDateTime.now();
        EVMSnapshot snapshot = evmCalculationService.calcularYPersistir(proyectoId, corte);

        return ResponseEntity.ok(toResponse(snapshot));
    }

    @Operation(summary = "S-Curve", description = "Serie temporal PV/EV/AC (UC-E04).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "S-Curve",
                    content = @Content(schema = @Schema(implementation = SCurveResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{proyectoId}/s-curve")
    public ResponseEntity<SCurveResponse> getSCurve(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SCurveResult result = obtenerSCurveUseCase.obtener(proyectoId, startDate, endDate);
        return ResponseEntity.ok(toResponse(result));
    }

    @Operation(summary = "Forecast fecha fin", description = "Proyección por SPI (UC-E05 / REQ-63).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Forecast",
                    content = @Content(schema = @Schema(implementation = ForecastResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{proyectoId}/forecast")
    public ResponseEntity<ForecastResponse> getForecast(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId) {
        ForecastResult result = obtenerForecastFechaUseCase.obtener(proyectoId);
        return ResponseEntity.ok(toForecastResponse(result));
    }

    @Operation(
            summary = "Cerrar periodo de valuación",
            description = "Cierre de periodo (invariante E-04 / REQ-64). Requiere revisión humana en escenarios borde."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Periodo cerrado",
                    content = @Content(schema = @Schema(implementation = CerrarPeriodoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Fecha inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Periodo ya cerrado / estado ilegal")
    })
    @PostMapping("/{proyectoId}/cerrar-periodo")
    public ResponseEntity<CerrarPeriodoResponse> cerrarPeriodo(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId,
            @Valid @RequestBody CerrarPeriodoRequest request) {
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
