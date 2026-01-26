package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.ResumenAsistenciaResponse;
import com.budgetpro.application.rrhh.port.in.ConsultarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.rest.rrhh.dto.RegistrarAsistenciaRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rrhh/asistencias")
public class AsistenciaController {

    private final RegistrarAsistenciaUseCase registrarAsistenciaUseCase;
    private final ConsultarAsistenciaUseCase consultarAsistenciaUseCase;

    public AsistenciaController(RegistrarAsistenciaUseCase registrarAsistenciaUseCase,
            ConsultarAsistenciaUseCase consultarAsistenciaUseCase) {
        this.registrarAsistenciaUseCase = registrarAsistenciaUseCase;
        this.consultarAsistenciaUseCase = consultarAsistenciaUseCase;
    }

    @PostMapping
    public ResponseEntity<AsistenciaResponse> registrar(@RequestBody @Valid RegistrarAsistenciaRequest request) {
        var command = request.toCommand();
        var response = registrarAsistenciaUseCase.registrarAsistencia(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AsistenciaResponse>> listar(@RequestParam(required = false) String empleadoId,
            @RequestParam(required = false) String proyectoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        if (empleadoId != null) {
            return ResponseEntity.ok(consultarAsistenciaUseCase.consultarPorEmpleado(EmpleadoId.fromString(empleadoId),
                    fechaInicio, fechaFin));
        } else if (proyectoId != null) {
            return ResponseEntity.ok(consultarAsistenciaUseCase.consultarPorProyecto(ProyectoId.from(proyectoId),
                    fechaInicio, fechaFin));
        }
        // If neither is provided, return empty list or bad request.
        // For now, returning empty list as per typical behavior if filters are missing
        // but required to narrow down scope,
        // though requirements didn't specify strict validation here.
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenAsistenciaResponse> resumenMensual(@RequestParam String empleadoId,
            @RequestParam int mes, @RequestParam int ano) {
        return ResponseEntity
                .ok(consultarAsistenciaUseCase.generarResumenMensual(EmpleadoId.fromString(empleadoId), mes, ano));
    }
}
