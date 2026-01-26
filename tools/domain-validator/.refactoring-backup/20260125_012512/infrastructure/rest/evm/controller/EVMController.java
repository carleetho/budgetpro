package com.budgetpro.infrastructure.rest.evm.controller;

import com.budgetpro.application.evm.service.EVMCalculationService;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import com.budgetpro.infrastructure.rest.evm.dto.EVMSnapshotResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller REST para el módulo EVM.
 */
@RestController
@RequestMapping("/api/v1/evm")
public class EVMController {

    private final EVMCalculationService evmCalculationService;

    public EVMController(EVMCalculationService evmCalculationService) {
        this.evmCalculationService = evmCalculationService;
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

    private EVMSnapshotResponse toResponse(EVMSnapshot snapshot) {
        return new EVMSnapshotResponse(snapshot.getId().getValue(), snapshot.getProyectoId(), snapshot.getFechaCorte(),
                snapshot.getFechaCalculo(), snapshot.getPv(), snapshot.getEv(), snapshot.getAc(), snapshot.getBac(),
                snapshot.getCv(), snapshot.getSv(), snapshot.getCpi(), snapshot.getSpi(), snapshot.getEac(),
                snapshot.getEtc(), snapshot.getVac(), snapshot.getInterpretacion());
    }
}
