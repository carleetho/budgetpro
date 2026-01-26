package com.budgetpro.infrastructure.rest.almacen.controller;

import com.budgetpro.application.almacen.dto.MovimientoAlmacenResponse;
import com.budgetpro.application.almacen.port.in.RegistrarMovimientoAlmacenUseCase;
import com.budgetpro.infrastructure.rest.almacen.dto.RegistrarMovimientoAlmacenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de almacén e inventarios.
 */
@RestController
@RequestMapping("/api/v1/almacen")
public class AlmacenController {

    private final RegistrarMovimientoAlmacenUseCase registrarMovimientoUseCase;

    public AlmacenController(RegistrarMovimientoAlmacenUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
    }

    /**
     * Registra un movimiento de entrada o salida de almacén.
     * 
     * @param request Request con los datos del movimiento
     * @return ResponseEntity con el movimiento registrado y stock actualizado
     */
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoAlmacenResponse> registrarMovimiento(
            @Valid @RequestBody RegistrarMovimientoAlmacenRequest request) {
        
        MovimientoAlmacenResponse response = registrarMovimientoUseCase.registrar(
                request.almacenId(),
                request.recursoId(),
                request.tipoMovimiento(),
                request.fechaMovimiento(),
                request.cantidad(),
                request.precioUnitario(),
                request.numeroDocumento(),
                request.partidaId(),
                request.centroCostoId(),
                request.observaciones()
        );

        return ResponseEntity
                .created(URI.create("/api/v1/almacen/movimientos/" + response.id()))
                .body(response);
    }
}
