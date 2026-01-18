package com.budgetpro.infrastructure.rest.partida.controller;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.port.in.CrearPartidaUseCase;
import com.budgetpro.infrastructure.rest.partida.dto.CrearPartidaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controller REST para operaciones de Partida.
 */
@RestController
@RequestMapping("/api/v1/partidas")
public class PartidaController {

    private final CrearPartidaUseCase crearPartidaUseCase;

    public PartidaController(CrearPartidaUseCase crearPartidaUseCase) {
        this.crearPartidaUseCase = crearPartidaUseCase;
    }

    /**
     * Crea una nueva partida.
     * 
     * @param request Request con los datos de la partida
     * @return ResponseEntity con la partida creada y código HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<PartidaResponse> crear(@Valid @RequestBody CrearPartidaRequest request) {
        CrearPartidaCommand command = new CrearPartidaCommand(
                request.presupuestoId(),
                request.padreId(),
                request.item(),
                request.descripcion(),
                request.unidad(),
                request.metrado(),
                request.nivel()
        );

        PartidaResponse response = crearPartidaUseCase.crear(command);

        return ResponseEntity
                .created(URI.create("/api/v1/partidas/" + response.id()))
                .body(response);
    }
}
