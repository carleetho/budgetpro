package com.budgetpro.infrastructure.rest.billetera.controller;

import com.budgetpro.application.finanzas.billetera.port.in.RegistrarMovimientoCajaUseCase;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;
import com.budgetpro.infrastructure.rest.billetera.dto.RegistrarMovimientoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.UUID;

@Tag(name = "Billetera", description = "Movimientos de caja; consultas de saldo en BilleteraQueryController")
@RestController
@RequestMapping("/api/v1/billeteras")
@SecurityRequirement(name = "bearer-jwt")
public class BilleteraController {

    private final RegistrarMovimientoCajaUseCase registrarMovimientoUseCase;

    public BilleteraController(RegistrarMovimientoCajaUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
    }

    @Operation(
            summary = "Registrar movimiento de caja",
            description = """
                    Registra INGRESO o EGRESO en la billetera.
                    
                    Integración con Estimación: la aprobación de estimación (ES-02) genera ingreso vía dominio;
                    este endpoint permite movimientos explícitos con referencia/evidencia.
                    
                    **Notas:** moneda normalizada a mayúsculas; `tipo` debe mapear a `TipoMovimiento`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimiento creado",
                    content = @Content(schema = @Schema(implementation = MovimientoCaja.class))),
            @ApiResponse(responseCode = "400", description = "Bean Validation / tipo o moneda inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Billetera no encontrada"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio / saldo insuficiente (EGRESO)")
    })
    @PostMapping("/{billeteraId}/movimientos")
    public ResponseEntity<MovimientoCaja> registrarMovimiento(
            @Parameter(description = "ID de la billetera", required = true) @PathVariable UUID billeteraId,
            @Valid @RequestBody RegistrarMovimientoRequest request) {

        String monedaNormalized = request.moneda().toUpperCase();
        TipoMovimiento tipo = TipoMovimiento.valueOf(request.tipo().toUpperCase());

        MovimientoCaja movimiento = registrarMovimientoUseCase.registrar(BilleteraId.of(billeteraId), request.monto(),
                monedaNormalized, tipo, request.referencia(), request.evidenciaUrl());

        return ResponseEntity
                .created(URI.create("/api/v1/billeteras/" + billeteraId + "/movimientos/" + movimiento.getId()))
                .body(movimiento);
    }
}
