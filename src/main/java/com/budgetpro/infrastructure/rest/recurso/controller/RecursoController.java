package com.budgetpro.infrastructure.rest.recurso.controller;

import com.budgetpro.application.recurso.dto.RecursoResponse;
import com.budgetpro.application.recurso.dto.RecursoSearchResponse;
import com.budgetpro.application.recurso.port.in.BuscarRecursosUseCase;
import com.budgetpro.application.recurso.port.in.CrearRecursoUseCase;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.rest.recurso.dto.CrearRecursoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para operaciones relacionadas con Recursos.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone el caso de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega al puerto de entrada (UseCase).
 */
@RestController
@RequestMapping("/api/v1/recursos")
public class RecursoController {

    private final CrearRecursoUseCase crearRecursoUseCase;
    private final BuscarRecursosUseCase buscarRecursosUseCase;

    public RecursoController(CrearRecursoUseCase crearRecursoUseCase,
                             BuscarRecursosUseCase buscarRecursosUseCase) {
        this.crearRecursoUseCase = crearRecursoUseCase;
        this.buscarRecursosUseCase = buscarRecursosUseCase;
    }

    /**
     * Busca recursos por nombre (autocomplete).
     * 
     * @param search Término de búsqueda (opcional)
     * @param tipo Filtro opcional por tipo de recurso
     * @param limit Límite de resultados (opcional, por defecto sin límite)
     * @return ResponseEntity con la lista de recursos y código HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<RecursoSearchResponse>> buscar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TipoRecurso tipo,
            @RequestParam(required = false) Integer limit) {
        // Delegar al caso de uso (puerto de entrada)
        List<RecursoSearchResponse> recursos = buscarRecursosUseCase.buscar(search, tipo, limit);
        
        // Retornar respuesta con código 200 OK
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(recursos);
    }

    /**
     * Crea un nuevo recurso.
     * 
     * @param request El request con los datos del recurso a crear
     * @return ResponseEntity con el recurso creado y código HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<RecursoResponse> crear(@RequestBody @Valid CrearRecursoRequest request) {
        // Convertir Request DTO a Command
        var command = request.toCommand();
        
        // Delegar al caso de uso (puerto de entrada)
        RecursoResponse response = crearRecursoUseCase.ejecutar(command);
        
        // Construir URI del recurso creado para el header Location
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        
        // Retornar respuesta con código 201 CREATED y header Location
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }
}
