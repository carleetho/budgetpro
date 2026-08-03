package com.budgetpro.infrastructure.rest.estimacion.controller;

import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.dto.GenerarEstimacionCommand;
import com.budgetpro.application.estimacion.port.in.AprobarEstimacionUseCase;
import com.budgetpro.application.estimacion.port.in.ConsultarEstimacionUseCase;
import com.budgetpro.application.estimacion.port.in.GenerarEstimacionUseCase;
import com.budgetpro.infrastructure.rest.estimacion.dto.GenerarEstimacionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operaciones de estimaciones.
 */
@Tag(name = "Estimaciones", description = "Valoraciones de avance, aprobación secuencial e integración con billetera")
@RestController
@RequestMapping("/api/v1/proyectos")
@SecurityRequirement(name = "bearer-jwt")
public class EstimacionController {

    private final GenerarEstimacionUseCase generarEstimacionUseCase;
    private final AprobarEstimacionUseCase aprobarEstimacionUseCase;
    private final ConsultarEstimacionUseCase consultarEstimacionUseCase;

    public EstimacionController(GenerarEstimacionUseCase generarEstimacionUseCase,
                                AprobarEstimacionUseCase aprobarEstimacionUseCase,
                                ConsultarEstimacionUseCase consultarEstimacionUseCase) {
        this.generarEstimacionUseCase = generarEstimacionUseCase;
        this.aprobarEstimacionUseCase = aprobarEstimacionUseCase;
        this.consultarEstimacionUseCase = consultarEstimacionUseCase;
    }

    @Operation(
            summary = "Generar estimación",
            description = """
                    Genera una estimación de avance para el proyecto.
                    
                    **Validaciones DTO:** fechas obligatorias; anticipo ≥ 0 (REGLA-087);
                    cantidades/PU ≥ 0 (REGLA-088).
                    
                    **Dominio:** límites de volumen (REGLA-016), coherencia de periodos.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estimación creada",
                    content = @Content(schema = @Schema(implementation = EstimacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bean Validation / INVALID_ARGUMENT"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio / periodo inválido")
    })
    @PostMapping("/{proyectoId}/estimaciones")
    public ResponseEntity<EstimacionResponse> generarEstimacion(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId,
            @Valid @RequestBody GenerarEstimacionRequest request) {

        GenerarEstimacionCommand command = new GenerarEstimacionCommand(
                proyectoId,
                request.fechaCorte(),
                request.periodoInicio(),
                request.periodoFin(),
                request.detalles().stream()
                        .map(item -> new GenerarEstimacionCommand.DetalleEstimacionItem(
                                item.partidaId(),
                                item.cantidadAvance(),
                                item.precioUnitario()
                        ))
                        .toList(),
                request.evidenciaUrl(),
                request.porcentajeAnticipo(),
                request.porcentajeRetencionFondoGarantia()
        );

        EstimacionResponse response = generarEstimacionUseCase.generar(command);

        return ResponseEntity
                .created(URI.create("/api/v1/proyectos/" + proyectoId + "/estimaciones/" + response.id()))
                .body(response);
    }

    @Operation(
            summary = "Aprobar estimación",
            description = """
                    Aprueba la estimación (ES-01 aprobación secuencial) y registra ingreso en billetera (ES-02).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aprobada; movimiento de caja registrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Estimación no encontrada"),
            @ApiResponse(responseCode = "409", description = "Transición de estado ilegal (ES-01)")
    })
    @PutMapping("/estimaciones/{estimacionId}/aprobar")
    public ResponseEntity<Void> aprobarEstimacion(
            @Parameter(description = "ID de la estimación", required = true) @PathVariable UUID estimacionId) {
        aprobarEstimacionUseCase.aprobar(estimacionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar estimaciones de un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EstimacionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{proyectoId}/estimaciones")
    public ResponseEntity<List<EstimacionResponse>> listarPorProyecto(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId) {
        return ResponseEntity.ok(consultarEstimacionUseCase.listarPorProyecto(proyectoId));
    }

    @Operation(summary = "Obtener estimación por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estimación",
                    content = @Content(schema = @Schema(implementation = EstimacionResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @GetMapping("/estimaciones/{estimacionId}")
    public ResponseEntity<EstimacionResponse> obtenerPorId(
            @Parameter(description = "ID de la estimación", required = true) @PathVariable UUID estimacionId) {
        return ResponseEntity.ok(consultarEstimacionUseCase.obtenerPorId(estimacionId));
    }
}
