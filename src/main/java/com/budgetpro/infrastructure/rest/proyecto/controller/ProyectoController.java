package com.budgetpro.infrastructure.rest.proyecto.controller;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.port.in.ConsultarProyectosUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones relacionadas con Proyectos.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone los casos de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega a los puertos de entrada (UseCases).
 */
@RestController
@RequestMapping("/api/v1/proyectos")
public class ProyectoController {

    private final ConsultarProyectosUseCase consultarProyectosUseCase;

    public ProyectoController(ConsultarProyectosUseCase consultarProyectosUseCase) {
        this.consultarProyectosUseCase = consultarProyectosUseCase;
    }

    /**
     * Lista todos los proyectos.
     * 
     * @param estado Filtro opcional por estado del proyecto
     * @return ResponseEntity con la lista de proyectos y código HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ProyectoResponse>> listar(
            @RequestParam(required = false) String estado) {
        // Delegar al caso de uso (puerto de entrada)
        List<ProyectoResponse> proyectos = estado != null
                ? consultarProyectosUseCase.consultarPorEstado(estado)
                : consultarProyectosUseCase.consultarTodos();
        
        // Retornar respuesta con código 200 OK
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(proyectos);
    }
}
