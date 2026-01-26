package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.ConfiguracionLaboralExtendidaResponse;
import com.budgetpro.application.rrhh.dto.HistorialFSRResponse;
import com.budgetpro.application.rrhh.port.in.ConfigurarLaboralExtendidaUseCase;
import com.budgetpro.application.rrhh.port.in.ConsultarHistorialFSRUseCase;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.rest.rrhh.dto.ConfigurarLaboralExtendidaRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rrhh/configuracion")
public class ConfiguracionLaboralExtendidaController {

    private final ConfigurarLaboralExtendidaUseCase configurarLaboralExtendidaUseCase;
    private final ConsultarHistorialFSRUseCase consultarHistorialFSRUseCase;

    public ConfiguracionLaboralExtendidaController(ConfigurarLaboralExtendidaUseCase configurarLaboralExtendidaUseCase,
            ConsultarHistorialFSRUseCase consultarHistorialFSRUseCase) {
        this.configurarLaboralExtendidaUseCase = configurarLaboralExtendidaUseCase;
        this.consultarHistorialFSRUseCase = consultarHistorialFSRUseCase;
    }

    @PutMapping("/global")
    public ResponseEntity<ConfiguracionLaboralExtendidaResponse> configurarGlobal(
            @RequestBody @Valid ConfigurarLaboralExtendidaRequest request) {
        var command = request.toCommand(null);
        var response = configurarLaboralExtendidaUseCase.configurar(command);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/proyectos/{proyectoId}")
    public ResponseEntity<ConfiguracionLaboralExtendidaResponse> configurarProyecto(@PathVariable String proyectoId,
            @RequestBody @Valid ConfigurarLaboralExtendidaRequest request) {
        var command = request.toCommand(proyectoId);
        var response = configurarLaboralExtendidaUseCase.configurar(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/proyectos/{proyectoId}/historial")
    public ResponseEntity<HistorialFSRResponse> consultarHistorial(@PathVariable String proyectoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        var response = consultarHistorialFSRUseCase.consultarHistorial(ProyectoId.from(UUID.fromString(proyectoId)),
                fechaInicio, fechaFin);
        return ResponseEntity.ok(response);
    }
}
