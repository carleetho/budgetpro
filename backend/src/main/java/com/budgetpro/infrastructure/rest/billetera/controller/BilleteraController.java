package com.budgetpro.infrastructure.rest.billetera.controller;

import com.budgetpro.application.finanzas.billetera.port.in.RegistrarMovimientoCajaUseCase;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;
import com.budgetpro.infrastructure.rest.billetera.dto.RegistrarMovimientoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billeteras")
public class BilleteraController {

    private final RegistrarMovimientoCajaUseCase registrarMovimientoUseCase;

    public BilleteraController(RegistrarMovimientoCajaUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
    }

    @PostMapping("/{billeteraId}/movimientos")
    public ResponseEntity<MovimientoCaja> registrarMovimiento(@PathVariable UUID billeteraId,
            @Valid @RequestBody RegistrarMovimientoRequest request) {

        String monedaNormalized = request.moneda().toUpperCase();
        TipoMovimiento tipo = TipoMovimiento.valueOf(request.tipo());

        MovimientoCaja movimiento = registrarMovimientoUseCase.registrar(BilleteraId.of(billeteraId), request.monto(),
                monedaNormalized, tipo, request.referencia(), request.evidenciaUrl());

        return ResponseEntity
                .created(URI.create("/api/v1/billeteras/" + billeteraId + "/movimientos/" + movimiento.getId()))
                .body(movimiento);
    }
}
