package com.budgetpro.infrastructure.rest.reajuste.controller;

import com.budgetpro.application.reajuste.dto.EstimacionReajusteResponse;
import com.budgetpro.application.reajuste.port.in.CalcularReajusteUseCase;
import com.budgetpro.infrastructure.rest.reajuste.dto.CalcularReajusteRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de reajuste de costos.
 */
@RestController
@RequestMapping("/api/v1/reajustes")
public class ReajusteController {

    private final CalcularReajusteUseCase calcularReajusteUseCase;

    public ReajusteController(CalcularReajusteUseCase calcularReajusteUseCase) {
        this.calcularReajusteUseCase = calcularReajusteUseCase;
    }

    /**
     * Calcula el reajuste de costos para un presupuesto.
     * 
     * @param request Request con los datos del reajuste
     * @return ResponseEntity con la estimación de reajuste calculada
     */
    @PostMapping("/calcular")
    public ResponseEntity<EstimacionReajusteResponse> calcularReajuste(
            @Valid @RequestBody CalcularReajusteRequest request) {
        
        EstimacionReajusteResponse response = calcularReajusteUseCase.calcular(
                request.proyectoId(),
                request.presupuestoId(),
                request.fechaCorte(),
                request.indiceBaseCodigo(),
                request.indiceBaseFecha(),
                request.indiceActualCodigo(),
                request.indiceActualFecha()
        );

        return ResponseEntity
                .created(URI.create("/api/v1/reajustes/" + response.id()))
                .body(response);
    }
}
