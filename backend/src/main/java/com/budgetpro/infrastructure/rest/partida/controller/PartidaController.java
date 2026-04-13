package com.budgetpro.infrastructure.rest.partida.controller;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.dto.WbsNodeResponse;
import com.budgetpro.application.partida.port.in.CrearPartidaUseCase;
import com.budgetpro.application.partida.port.in.ObtenerPartidaUseCase;
import com.budgetpro.application.partida.port.in.ObtenerWbsUseCase;
import com.budgetpro.infrastructure.rest.partida.dto.CrearPartidaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operaciones de Partida.
 */
@RestController
@RequestMapping("/api/v1/partidas")
public class PartidaController {

    private final CrearPartidaUseCase crearPartidaUseCase;
    private final ObtenerPartidaUseCase obtenerPartidaUseCase;
    private final ObtenerWbsUseCase obtenerWbsUseCase;

    public PartidaController(CrearPartidaUseCase crearPartidaUseCase,
                             ObtenerPartidaUseCase obtenerPartidaUseCase,
                             ObtenerWbsUseCase obtenerWbsUseCase) {
        this.crearPartidaUseCase = crearPartidaUseCase;
        this.obtenerPartidaUseCase = obtenerPartidaUseCase;
        this.obtenerWbsUseCase = obtenerWbsUseCase;
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

    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponse> obtenerPorId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(obtenerPartidaUseCase.obtenerPorId(id));
    }

    @GetMapping("/wbs")
    public ResponseEntity<List<WbsNodeResponse>> obtenerWbs(@RequestParam("presupuestoId") UUID presupuestoId) {
        return ResponseEntity.ok(obtenerWbsUseCase.obtenerWbsPorPresupuesto(presupuestoId));
    }
}
