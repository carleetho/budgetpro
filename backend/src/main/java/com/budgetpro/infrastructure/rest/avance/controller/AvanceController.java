package com.budgetpro.infrastructure.rest.avance.controller;

import com.budgetpro.application.avance.dto.AvanceFisicoResponse;
import com.budgetpro.application.avance.dto.RegistrarAvanceCommand;
import com.budgetpro.application.avance.port.in.RegistrarAvanceUseCase;
import com.budgetpro.infrastructure.rest.avance.dto.RegistrarAvanceRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de Avance Físico.
 */
@RestController
@RequestMapping("/api/v1/partidas")
public class AvanceController {

    private final RegistrarAvanceUseCase registrarAvanceUseCase;

    public AvanceController(RegistrarAvanceUseCase registrarAvanceUseCase) {
        this.registrarAvanceUseCase = registrarAvanceUseCase;
    }

    /**
     * Registra un avance físico para una partida.
     * 
     * @param partidaId El ID de la partida
     * @param request Request con los datos del avance
     * @return ResponseEntity con el avance registrado y código HTTP 201 CREATED
     */
    @PostMapping("/{partidaId}/avances")
    public ResponseEntity<AvanceFisicoResponse> registrar(@PathVariable UUID partidaId,
                                                           @Valid @RequestBody RegistrarAvanceRequest request) {
        RegistrarAvanceCommand command = new RegistrarAvanceCommand(
                partidaId,
                request.fecha(),
                request.metradoEjecutado(),
                request.observacion()
        );

        AvanceFisicoResponse response = registrarAvanceUseCase.registrar(command);

        return ResponseEntity
                .created(URI.create("/api/v1/partidas/" + partidaId + "/avances/" + response.id()))
                .body(response);
    }
}
