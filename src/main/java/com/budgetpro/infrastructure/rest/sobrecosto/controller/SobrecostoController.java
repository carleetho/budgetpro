package com.budgetpro.infrastructure.rest.sobrecosto.controller;

import com.budgetpro.application.sobrecosto.dto.AnalisisSobrecostoResponse;
import com.budgetpro.application.sobrecosto.dto.ConfigurarSobrecostoCommand;
import com.budgetpro.application.sobrecosto.port.in.ConfigurarSobrecostoUseCase;
import com.budgetpro.infrastructure.rest.sobrecosto.dto.ConfigurarSobrecostoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de sobrecosto.
 */
@RestController
@RequestMapping("/api/v1/presupuestos")
public class SobrecostoController {

    private final ConfigurarSobrecostoUseCase configurarSobrecostoUseCase;

    public SobrecostoController(ConfigurarSobrecostoUseCase configurarSobrecostoUseCase) {
        this.configurarSobrecostoUseCase = configurarSobrecostoUseCase;
    }

    /**
     * Configura o actualiza el análisis de sobrecosto de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @param request Request con los porcentajes de sobrecosto
     * @return ResponseEntity con el análisis configurado y código HTTP 200 OK o 201 CREATED
     */
    @PutMapping("/{presupuestoId}/sobrecosto")
    public ResponseEntity<AnalisisSobrecostoResponse> configurarSobrecosto(
            @PathVariable UUID presupuestoId,
            @Valid @RequestBody ConfigurarSobrecostoRequest request) {
        
        ConfigurarSobrecostoCommand command = new ConfigurarSobrecostoCommand(
                presupuestoId,
                request.porcentajeIndirectosOficinaCentral(),
                request.porcentajeIndirectosOficinaCampo(),
                request.porcentajeFinanciamiento(),
                request.financiamientoCalculado(),
                request.porcentajeUtilidad(),
                request.porcentajeFianzas(),
                request.porcentajeImpuestosReflejables()
        );

        AnalisisSobrecostoResponse response = configurarSobrecostoUseCase.configurar(command);

        return ResponseEntity
                .ok()
                .location(URI.create("/api/v1/presupuestos/" + presupuestoId + "/sobrecosto"))
                .body(response);
    }
}
