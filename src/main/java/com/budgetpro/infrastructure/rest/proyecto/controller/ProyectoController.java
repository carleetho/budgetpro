package com.budgetpro.infrastructure.rest.proyecto.controller;

import com.budgetpro.application.proyecto.dto.CrearProyectoCommand;
import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.port.in.CrearProyectoUseCase;
import com.budgetpro.infrastructure.rest.proyecto.dto.CrearProyectoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controller REST para operaciones de Proyecto.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
public class ProyectoController {

    private final CrearProyectoUseCase crearProyectoUseCase;

    public ProyectoController(CrearProyectoUseCase crearProyectoUseCase) {
        this.crearProyectoUseCase = crearProyectoUseCase;
    }

    /**
     * Crea un nuevo proyecto.
     * 
     * @param request Request con los datos del proyecto
     * @return ResponseEntity con el proyecto creado y código HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<ProyectoResponse> crear(@Valid @RequestBody CrearProyectoRequest request) {
        CrearProyectoCommand command = new CrearProyectoCommand(
                request.nombre(),
                request.ubicacion()
        );

        ProyectoResponse response = crearProyectoUseCase.crear(command);

        return ResponseEntity
                .created(URI.create("/api/v1/proyectos/" + response.id()))
                .body(response);
    }
}
