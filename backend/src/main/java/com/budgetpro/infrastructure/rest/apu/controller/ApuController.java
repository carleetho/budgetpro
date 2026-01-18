package com.budgetpro.infrastructure.rest.apu.controller;

import com.budgetpro.application.apu.dto.ApuInsumoCommand;
import com.budgetpro.application.apu.dto.CrearApuCommand;
import com.budgetpro.application.apu.dto.ApuResponse;
import com.budgetpro.application.apu.port.in.CrearApuUseCase;
import com.budgetpro.infrastructure.rest.apu.dto.ApuInsumoRequest;
import com.budgetpro.infrastructure.rest.apu.dto.CrearApuRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para operaciones de APU.
 */
@RestController
@RequestMapping("/api/v1/partidas")
public class ApuController {

    private final CrearApuUseCase crearApuUseCase;

    public ApuController(CrearApuUseCase crearApuUseCase) {
        this.crearApuUseCase = crearApuUseCase;
    }

    /**
     * Crea un nuevo APU para una partida.
     * 
     * @param partidaId El ID de la partida
     * @param request Request con los datos del APU
     * @return ResponseEntity con el APU creado y código HTTP 201 CREATED
     */
    @PostMapping("/{partidaId}/apu")
    public ResponseEntity<ApuResponse> crear(@PathVariable UUID partidaId,
                                             @Valid @RequestBody CrearApuRequest request) {
        // Mapear insumos del request
        List<ApuInsumoCommand> insumosCommand = request.insumos().stream()
                .map(insumo -> new ApuInsumoCommand(
                    insumo.recursoId(),
                    insumo.cantidad(),
                    insumo.precioUnitario()
                ))
                .collect(Collectors.toList());

        CrearApuCommand command = new CrearApuCommand(
                partidaId,
                request.rendimiento(),
                request.unidad(),
                insumosCommand
        );

        ApuResponse response = crearApuUseCase.crear(command);

        return ResponseEntity
                .created(URI.create("/api/v1/partidas/" + partidaId + "/apu/" + response.id()))
                .body(response);
    }
}
