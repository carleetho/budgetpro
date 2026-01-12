package com.budgetpro.infrastructure.rest.partida.controller;

import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.port.in.ConsultarPartidasUseCase;
import com.budgetpro.application.partida.port.in.CrearPartidaUseCase;
import com.budgetpro.infrastructure.rest.partida.dto.CrearPartidaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones relacionadas con Partidas.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone los casos de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega a los puertos de entrada (UseCases).
 */
@RestController
@RequestMapping("/api/v1/presupuestos/{presupuestoId}/partidas")
public class PartidaController {

    private final CrearPartidaUseCase crearPartidaUseCase;
    private final ConsultarPartidasUseCase consultarPartidasUseCase;

    public PartidaController(CrearPartidaUseCase crearPartidaUseCase,
                             ConsultarPartidasUseCase consultarPartidasUseCase) {
        this.crearPartidaUseCase = crearPartidaUseCase;
        this.consultarPartidasUseCase = consultarPartidasUseCase;
    }

    /**
     * Crea una nueva partida en un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto (obtenido de la URL path)
     * @param request El request con los datos de la partida a crear
     * @return ResponseEntity con la partida creada y código HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<PartidaResponse> crear(@PathVariable UUID presupuestoId,
                                                 @RequestBody @Valid CrearPartidaRequest request) {
        // Convertir Request DTO a Command (incluyendo presupuestoId de la URL)
        var command = request.toCommand(presupuestoId);
        
        // Delegar al caso de uso (puerto de entrada)
        PartidaResponse response = crearPartidaUseCase.ejecutar(command);
        
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

    /**
     * Lista todas las partidas de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto (obtenido de la URL path)
     * @return ResponseEntity con la lista de partidas y código HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<PartidaResponse>> listar(@PathVariable UUID presupuestoId) {
        // Delegar al caso de uso (puerto de entrada)
        List<PartidaResponse> partidas = consultarPartidasUseCase.consultarPorPresupuesto(presupuestoId);
        
        // Retornar respuesta con código 200 OK
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(partidas);
    }
}
