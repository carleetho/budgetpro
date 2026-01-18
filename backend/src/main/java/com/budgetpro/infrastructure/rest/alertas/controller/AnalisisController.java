package com.budgetpro.infrastructure.rest.alertas.controller;

import com.budgetpro.application.alertas.dto.AnalisisPresupuestoResponse;
import com.budgetpro.application.alertas.port.in.AnalizarPresupuestoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de análisis paramétrico de presupuestos.
 */
@RestController
@RequestMapping("/api/v1/analisis")
public class AnalisisController {

    private final AnalizarPresupuestoUseCase analizarPresupuestoUseCase;

    public AnalisisController(AnalizarPresupuestoUseCase analizarPresupuestoUseCase) {
        this.analizarPresupuestoUseCase = analizarPresupuestoUseCase;
    }

    /**
     * Analiza un presupuesto y genera alertas paramétricas.
     * 
     * @param presupuestoId El ID del presupuesto a analizar
     * @return ResponseEntity con el análisis y las alertas generadas
     */
    @GetMapping("/alertas/{presupuestoId}")
    public ResponseEntity<AnalisisPresupuestoResponse> analizarPresupuesto(
            @PathVariable UUID presupuestoId) {
        
        AnalisisPresupuestoResponse response = analizarPresupuestoUseCase.analizar(presupuestoId);

        return ResponseEntity.ok(response);
    }
}
