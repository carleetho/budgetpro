package com.budgetpro.infrastructure.rest.proyecto.controller;

import com.budgetpro.application.proyecto.dto.CrearProyectoCommand;
import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.port.in.ConsultarProyectoUseCase;
import com.budgetpro.application.proyecto.port.in.ConsultarProyectosUseCase;
import com.budgetpro.application.proyecto.port.in.CrearProyectoUseCase;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.rest.proyecto.dto.CrearProyectoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operaciones de Proyecto.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
public class ProyectoController {

    private final CrearProyectoUseCase crearProyectoUseCase;
    private final ConsultarProyectosUseCase consultarProyectosUseCase;
    private final ConsultarProyectoUseCase consultarProyectoUseCase;

    public ProyectoController(CrearProyectoUseCase crearProyectoUseCase, 
                              ConsultarProyectosUseCase consultarProyectosUseCase,
                              ConsultarProyectoUseCase consultarProyectoUseCase) {
        this.crearProyectoUseCase = crearProyectoUseCase;
        this.consultarProyectosUseCase = consultarProyectosUseCase;
        this.consultarProyectoUseCase = consultarProyectoUseCase;
    }

    /**
     * Obtiene todos los proyectos.
     * 
     * @return Lista de proyectos
     */
    @GetMapping
    public ResponseEntity<List<ProyectoResponse>> listar() {
        List<ProyectoResponse> proyectos = consultarProyectosUseCase.listar();
        return ResponseEntity.ok(proyectos);
    }

    /**
     * Obtiene un proyecto por su ID.
     * 
     * @param id El ID del proyecto (UUID)
     * @return ResponseEntity con el proyecto si existe, 404 NOT FOUND si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponse> obtenerPorId(@PathVariable UUID id) {
        return consultarProyectoUseCase.obtenerPorId(ProyectoId.from(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
