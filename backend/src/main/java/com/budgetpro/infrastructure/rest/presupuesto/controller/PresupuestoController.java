package com.budgetpro.infrastructure.rest.presupuesto.controller;

import com.budgetpro.application.control.dto.ReporteControlCostosResponse;
import com.budgetpro.application.control.port.in.ConsultarControlCostosUseCase;
import com.budgetpro.application.explosion.dto.ExplosionInsumosResponse;
import com.budgetpro.application.explosion.port.in.ExplotarInsumosPresupuestoUseCase;
import com.budgetpro.application.presupuesto.dto.CrearPresupuestoCommand;
import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.dto.ListarPresupuestosPaginadosResponse;
import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
import com.budgetpro.application.presupuesto.port.in.ConsultarPresupuestoUseCase;
import com.budgetpro.application.presupuesto.port.in.CrearPresupuestoUseCase;
import com.budgetpro.application.presupuesto.port.in.ListarPresupuestosPaginadosUseCase;
import com.budgetpro.infrastructure.rest.presupuesto.dto.CrearPresupuestoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de Presupuesto.
 */
@Tag(name = "Presupuestos", description = "Gestión de presupuestos, control de costos y explosión de insumos")
@RestController
@RequestMapping("/api/v1/presupuestos")
@Validated
@SecurityRequirement(name = "bearer-jwt")
public class PresupuestoController {

    private final CrearPresupuestoUseCase crearPresupuestoUseCase;
    private final AprobarPresupuestoUseCase aprobarPresupuestoUseCase;
    private final ConsultarPresupuestoUseCase consultarPresupuestoUseCase;
    private final ListarPresupuestosPaginadosUseCase listarPresupuestosPaginadosUseCase;
    private final ConsultarControlCostosUseCase consultarControlCostosUseCase;
    private final ExplotarInsumosPresupuestoUseCase explotarInsumosPresupuestoUseCase;

    public PresupuestoController(CrearPresupuestoUseCase crearPresupuestoUseCase,
                                 AprobarPresupuestoUseCase aprobarPresupuestoUseCase,
                                 ConsultarPresupuestoUseCase consultarPresupuestoUseCase,
                                 ListarPresupuestosPaginadosUseCase listarPresupuestosPaginadosUseCase,
                                 ConsultarControlCostosUseCase consultarControlCostosUseCase,
                                 ExplotarInsumosPresupuestoUseCase explotarInsumosPresupuestoUseCase) {
        this.crearPresupuestoUseCase = crearPresupuestoUseCase;
        this.aprobarPresupuestoUseCase = aprobarPresupuestoUseCase;
        this.consultarPresupuestoUseCase = consultarPresupuestoUseCase;
        this.listarPresupuestosPaginadosUseCase = listarPresupuestosPaginadosUseCase;
        this.consultarControlCostosUseCase = consultarControlCostosUseCase;
        this.explotarInsumosPresupuestoUseCase = explotarInsumosPresupuestoUseCase;
    }

    @Operation(
            summary = "Listar presupuestos (paginado)",
            description = "Lista presupuestos filtrados por tenant y proyecto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de presupuestos",
                    content = @Content(schema = @Schema(implementation = ListarPresupuestosPaginadosResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping(params = {"tenantId", "proyectoId"})
    public ResponseEntity<ListarPresupuestosPaginadosResponse> listarPaginado(
            @Parameter(description = "ID del tenant", required = true) @RequestParam UUID tenantId,
            @Parameter(description = "ID del proyecto", required = true) @RequestParam UUID proyectoId,
            @Parameter(description = "Página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página (1-100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        ListarPresupuestosPaginadosResponse response =
                listarPresupuestosPaginadosUseCase.listar(tenantId, proyectoId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Crear presupuesto",
            description = """
                    Crea un presupuesto asociado a un proyecto.
                    
                    **Validaciones / reglas:**
                    - `proyectoId` obligatorio (REGLA-098)
                    - `nombre` no blank
                    - El proyecto debe existir (dominio)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Presupuesto creado",
                    content = @Content(schema = @Schema(implementation = PresupuestoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bean Validation / INVALID_ARGUMENT"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Tenant/proyecto no autorizado")
    })
    @PostMapping
    public ResponseEntity<PresupuestoResponse> crear(@RequestBody CrearPresupuestoRequest request) {
        CrearPresupuestoCommand command = new CrearPresupuestoCommand(
                request.proyectoId(),
                request.nombre()
        );

        PresupuestoResponse response = crearPresupuestoUseCase.crear(command);

        return ResponseEntity
                .created(URI.create("/api/v1/presupuestos/" + response.id()))
                .body(response);
    }

    @Operation(
            summary = "Aprobar presupuesto",
            description = """
                    Aprueba el presupuesto y lo congela (hash de integridad).
                    
                    **Reglas:** no modificar presupuesto congelado (invariante P-01 / estado CONGELADO).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aprobado / congelado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Estado ilegal / ya congelado")
    })
    @PostMapping("/{presupuestoId}/aprobar")
    public ResponseEntity<Void> aprobar(
            @Parameter(description = "ID del presupuesto", required = true) @PathVariable UUID presupuestoId) {
        aprobarPresupuestoUseCase.aprobar(presupuestoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar presupuesto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto",
                    content = @Content(schema = @Schema(implementation = PresupuestoResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{presupuestoId}")
    public ResponseEntity<PresupuestoResponse> consultar(
            @Parameter(description = "ID del presupuesto", required = true) @PathVariable UUID presupuestoId) {
        PresupuestoResponse response = consultarPresupuestoUseCase.consultar(presupuestoId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Control de costos (Plan vs Real)",
            description = "Reporte de control de costos del presupuesto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte",
                    content = @Content(schema = @Schema(implementation = ReporteControlCostosResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @GetMapping("/{presupuestoId}/control-costos")
    public ResponseEntity<ReporteControlCostosResponse> consultarControlCostos(
            @Parameter(description = "ID del presupuesto", required = true) @PathVariable UUID presupuestoId) {
        ReporteControlCostosResponse response = consultarControlCostosUseCase.consultar(presupuestoId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Explosión de insumos",
            description = "Agrega cantidades por unidad base sobre partidas hoja del WBS, agrupadas por tipo de recurso."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Explosión",
                    content = @Content(schema = @Schema(implementation = ExplosionInsumosResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado")
    })
    @GetMapping("/{presupuestoId}/explosion-insumos")
    public ResponseEntity<ExplosionInsumosResponse> explotarInsumos(
            @Parameter(description = "ID del presupuesto", required = true) @PathVariable UUID presupuestoId) {
        ExplosionInsumosResponse response = explotarInsumosPresupuestoUseCase.ejecutar(presupuestoId);
        return ResponseEntity.ok(response);
    }
}
