package com.budgetpro.infrastructure.rest.transferencia.controller;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.transferencia.service.TransferenciaService;
import com.budgetpro.infrastructure.rest.transferencia.dto.TransferenciaEntreBodegasRequest;
import com.budgetpro.infrastructure.rest.transferencia.dto.TransferenciaEntreProyectosRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping("/entre-bodegas")
    public ResponseEntity<Void> transferirEntreBodegas(@Valid @RequestBody TransferenciaEntreBodegasRequest request) {
        transferenciaService.transferirEntreBodegas(
                InventarioId.of(request.inventarioOrigenId()),
                BodegaId.of(request.bodegaDestinoId()),
                request.cantidad(),
                request.referencia()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/entre-proyectos")
    public ResponseEntity<Void> transferirEntreProyectos(@Valid @RequestBody TransferenciaEntreProyectosRequest request) {
        transferenciaService.transferirEntreProyectos(
                InventarioId.of(request.inventarioOrigenId()),
                BodegaId.of(request.bodegaDestinoId()),
                request.proyectoDestinoId(),
                request.cantidad(),
                request.excepcionId(),
                request.referencia()
        );
        return ResponseEntity.noContent().build();
    }
}

