package com.budgetpro.infrastructure.rest.inventario.controller;

import com.budgetpro.application.inventario.dto.InventarioItemResponse;
import com.budgetpro.application.inventario.port.in.ConsultarInventarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operaciones de Inventario.
 */
@Tag(name = "Inventario", description = """
        Consulta de stock por proyecto. Madurez radiografía ~70%.
        ⚠️ Multi-bodega / transferencias: ver endpoints de almacén y transferencias; escenarios parcialmente definidos.
        """)
@RestController
@RequestMapping("/api/v1/proyectos")
@SecurityRequirement(name = "bearer-jwt")
public class InventarioController {

    private final ConsultarInventarioUseCase consultarInventarioUseCase;

    public InventarioController(ConsultarInventarioUseCase consultarInventarioUseCase) {
        this.consultarInventarioUseCase = consultarInventarioUseCase;
    }

    @Operation(summary = "Inventario por proyecto", description = "Lista items de inventario del proyecto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = InventarioItemResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping("/{proyectoId}/inventario")
    public ResponseEntity<List<InventarioItemResponse>> consultarPorProyecto(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId) {
        List<InventarioItemResponse> items = consultarInventarioUseCase.consultarPorProyecto(proyectoId);
        return ResponseEntity.ok(items);
    }
}
