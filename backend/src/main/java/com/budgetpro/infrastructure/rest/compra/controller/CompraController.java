package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.application.compra.dto.CompraDetalleCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraResponse;
import com.budgetpro.application.compra.port.in.RegistrarCompraUseCase;
import com.budgetpro.infrastructure.rest.compra.dto.RegistrarCompraRequest;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.stream.Collectors;

/**
 * Controller REST para operaciones de Compra (registro directo).
 * Ciclo de vida OC completo: ver tag "Órdenes de Compra".
 */
@Tag(name = "Compras", description = """
        Registro de compras / impacto en partidas. Madurez radiografía ~75%.
        ⚠️ Flujo de aprobación OC y edge cases: preferir OrdenCompraController + revisión humana.
        """)
@RestController
@RequestMapping("/api/v1/compras")
@SecurityRequirement(name = "bearer-jwt")
public class CompraController {

    private final RegistrarCompraUseCase registrarCompraUseCase;

    public CompraController(RegistrarCompraUseCase registrarCompraUseCase) {
        this.registrarCompraUseCase = registrarCompraUseCase;
    }

    @Operation(
            summary = "Registrar compra",
            description = "Registra compra con detalles de insumos vinculados a partidas. Integración inventario vía dominio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compra registrada",
                    content = @Content(schema = @Schema(implementation = RegistrarCompraResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bean Validation"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio")
    })
    @PostMapping
    public ResponseEntity<RegistrarCompraResponse> registrar(@Valid @RequestBody RegistrarCompraRequest request) {
        java.util.List<CompraDetalleCommand> detallesCommand = request.detalles().stream()
                .map(detalle -> new CompraDetalleCommand(
                    detalle.recursoExternalId(),
                    detalle.recursoNombre(),
                    detalle.unidad(),
                    detalle.partidaId(),
                    detalle.naturalezaGasto(),
                    detalle.relacionContractual(),
                    detalle.rubroInsumo(),
                    detalle.cantidad(),
                    detalle.precioUnitario()
                ))
                .collect(Collectors.toList());

        RegistrarCompraCommand command = new RegistrarCompraCommand(
                request.proyectoId(),
                request.fecha(),
                request.proveedor(),
                detallesCommand
        );

        RegistrarCompraResponse response = registrarCompraUseCase.registrar(command);

        return ResponseEntity
                .created(URI.create("/api/v1/compras/" + response.id()))
                .body(response);
    }
}
