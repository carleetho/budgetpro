package com.budgetpro.infrastructure.rest.apu.controller;

import com.budgetpro.application.apu.dto.ApuInsumoCommand;
import com.budgetpro.application.apu.dto.CrearApuCommand;
import com.budgetpro.application.apu.dto.ApuResponse;
import com.budgetpro.application.apu.port.in.ActualizarRendimientoUseCase;
import com.budgetpro.application.apu.port.in.CrearApuUseCase;
import com.budgetpro.infrastructure.rest.apu.dto.ActualizarRendimientoRequest;
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
@RequestMapping("/api/v1")
public class ApuController {

    private final CrearApuUseCase crearApuUseCase;
    private final ActualizarRendimientoUseCase actualizarRendimientoUseCase;

    public ApuController(CrearApuUseCase crearApuUseCase,
                         ActualizarRendimientoUseCase actualizarRendimientoUseCase) {
        this.crearApuUseCase = crearApuUseCase;
        this.actualizarRendimientoUseCase = actualizarRendimientoUseCase;
    }

    /**
     * Crea un nuevo APU para una partida.
     * 
     * @param partidaId El ID de la partida
     * @param request Request con los datos del APU
     * @return ResponseEntity con el APU creado y código HTTP 201 CREATED
     */
    @PostMapping("/partidas/{partidaId}/apu")
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

    /**
     * Actualiza el rendimiento vigente de un APU y recalcula automáticamente los costos afectados.
     * 
     * @param apuSnapshotId El ID del APUSnapshot a actualizar
     * @param request Request con el nuevo rendimiento y usuario
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @PutMapping("/apu/{apuSnapshotId}/rendimiento")
    public ResponseEntity<Void> actualizarRendimiento(
            @PathVariable UUID apuSnapshotId,
            @Valid @RequestBody ActualizarRendimientoRequest request) {
        actualizarRendimientoUseCase.actualizarRendimiento(
                apuSnapshotId,
                request.nuevoRendimiento(),
                request.usuarioId()
        );
        return ResponseEntity.noContent().build();
    }
}
