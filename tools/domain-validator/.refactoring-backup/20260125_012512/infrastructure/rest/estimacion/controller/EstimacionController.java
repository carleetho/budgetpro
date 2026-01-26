package com.budgetpro.infrastructure.rest.estimacion.controller;

import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.dto.GenerarEstimacionCommand;
import com.budgetpro.application.estimacion.port.in.AprobarEstimacionUseCase;
import com.budgetpro.application.estimacion.port.in.GenerarEstimacionUseCase;
import com.budgetpro.infrastructure.rest.estimacion.dto.GenerarEstimacionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de estimaciones.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
public class EstimacionController {

    private final GenerarEstimacionUseCase generarEstimacionUseCase;
    private final AprobarEstimacionUseCase aprobarEstimacionUseCase;

    public EstimacionController(GenerarEstimacionUseCase generarEstimacionUseCase,
                                AprobarEstimacionUseCase aprobarEstimacionUseCase) {
        this.generarEstimacionUseCase = generarEstimacionUseCase;
        this.aprobarEstimacionUseCase = aprobarEstimacionUseCase;
    }

    /**
     * Genera una nueva estimación de avance.
     * 
     * @param proyectoId El ID del proyecto
     * @param request Request con los datos de la estimación
     * @return ResponseEntity con la estimación generada
     */
    @PostMapping("/{proyectoId}/estimaciones")
    public ResponseEntity<EstimacionResponse> generarEstimacion(
            @PathVariable UUID proyectoId,
            @Valid @RequestBody GenerarEstimacionRequest request) {
        
        GenerarEstimacionCommand command = new GenerarEstimacionCommand(
                proyectoId,
                request.fechaCorte(),
                request.periodoInicio(),
                request.periodoFin(),
                request.detalles().stream()
                        .map(item -> new GenerarEstimacionCommand.DetalleEstimacionItem(
                                item.partidaId(),
                                item.cantidadAvance(),
                                item.precioUnitario()
                        ))
                        .toList(),
                request.evidenciaUrl(),
                request.porcentajeAnticipo(),
                request.porcentajeRetencionFondoGarantia()
        );

        EstimacionResponse response = generarEstimacionUseCase.generar(command);

        return ResponseEntity
                .created(URI.create("/api/v1/proyectos/" + proyectoId + "/estimaciones/" + response.id()))
                .body(response);
    }

    /**
     * Aprueba una estimación y registra el ingreso en la billetera.
     * 
     * @param estimacionId El ID de la estimación
     * @return ResponseEntity con código HTTP 204 No Content
     */
    @PutMapping("/estimaciones/{estimacionId}/aprobar")
    public ResponseEntity<Void> aprobarEstimacion(@PathVariable UUID estimacionId) {
        aprobarEstimacionUseCase.aprobar(estimacionId);
        return ResponseEntity.noContent().build();
    }
}
