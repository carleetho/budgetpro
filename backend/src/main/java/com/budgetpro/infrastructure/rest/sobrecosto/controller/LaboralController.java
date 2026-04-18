package com.budgetpro.infrastructure.rest.sobrecosto.controller;

import com.budgetpro.application.rrhh.dto.ConfiguracionLaboralExtendidaResponse;
import com.budgetpro.application.rrhh.port.in.ConfigurarLaboralExtendidaUseCase;
import com.budgetpro.infrastructure.rest.rrhh.dto.ConfigurarLaboralExtendidaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Configuración laboral (FSR) para flujos Presupuesto / sobrecosto.
 * <p>
 * Contrato alineado a {@code PUT /api/v1/rrhh/configuracion/**} (modelo extendido con vigencia y factores).
 */
@RestController
@RequestMapping("/api/v1")
public class LaboralController {

    private final ConfigurarLaboralExtendidaUseCase configurarLaboralExtendidaUseCase;

    public LaboralController(ConfigurarLaboralExtendidaUseCase configurarLaboralExtendidaUseCase) {
        this.configurarLaboralExtendidaUseCase = configurarLaboralExtendidaUseCase;
    }

    @PutMapping("/configuracion-laboral")
    public ResponseEntity<ConfiguracionLaboralExtendidaResponse> configurarLaboralGlobal(
            @Valid @RequestBody ConfigurarLaboralExtendidaRequest request) {
        var response = configurarLaboralExtendidaUseCase.configurar(request.toCommand(null));
        return ResponseEntity.ok()
                .location(URI.create("/api/v1/configuracion-laboral"))
                .body(response);
    }

    @PutMapping("/proyectos/{proyectoId}/configuracion-laboral")
    public ResponseEntity<ConfiguracionLaboralExtendidaResponse> configurarLaboralProyecto(
            @PathVariable UUID proyectoId,
            @Valid @RequestBody ConfigurarLaboralExtendidaRequest request) {
        var response = configurarLaboralExtendidaUseCase.configurar(request.toCommand(proyectoId.toString()));
        return ResponseEntity.ok()
                .location(URI.create("/api/v1/proyectos/" + proyectoId + "/configuracion-laboral"))
                .body(response);
    }
}
