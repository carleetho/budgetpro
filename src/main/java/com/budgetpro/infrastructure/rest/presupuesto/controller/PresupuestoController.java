package com.budgetpro.infrastructure.rest.presupuesto.controller;

import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.port.in.ConsultarPresupuestosUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones relacionadas con Presupuestos.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone los casos de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega a los puertos de entrada (UseCases).
 */
@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/presupuestos")
public class PresupuestoController {

    private final ConsultarPresupuestosUseCase consultarPresupuestosUseCase;

    public PresupuestoController(ConsultarPresupuestosUseCase consultarPresupuestosUseCase) {
        this.consultarPresupuestosUseCase = consultarPresupuestosUseCase;
    }

    /**
     * Lista todos los presupuestos de un proyecto.
     * 
     * @param proyectoId El ID del proyecto (obtenido de la URL path)
     * @return ResponseEntity con la lista de presupuestos y código HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<PresupuestoResponse>> listar(@PathVariable UUID proyectoId) {
        // Delegar al caso de uso (puerto de entrada)
        List<PresupuestoResponse> presupuestos = consultarPresupuestosUseCase.consultarPorProyecto(proyectoId);
        
        // Retornar respuesta con código 200 OK
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(presupuestos);
    }
}
