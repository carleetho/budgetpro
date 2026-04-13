package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.ResumenAsistenciaResponse;
import com.budgetpro.application.rrhh.exception.FiltrosConsultaAsistenciaIncompletosException;
import com.budgetpro.application.rrhh.port.in.ConsultarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.rest.rrhh.dto.RegistrarAsistenciaRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.LocalDate;
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
        boolean porEmpleado = StringUtils.hasText(empleadoId);
        boolean porProyecto = StringUtils.hasText(proyectoId);
        if (!porEmpleado && !porProyecto) {
            throw new FiltrosConsultaAsistenciaIncompletosException(
                    "Debe indicar empleadoId o proyectoId para consultar asistencias en el rango de fechas.");
        }
        if (porEmpleado) {
            return ResponseEntity.ok(consultarAsistenciaUseCase.consultarPorEmpleado(EmpleadoId.fromString(empleadoId),
                    fechaInicio, fechaFin));
        }
        return ResponseEntity.ok(consultarAsistenciaUseCase.consultarPorProyecto(ProyectoId.from(proyectoId),
                fechaInicio, fechaFin));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenAsistenciaResponse> resumenMensual(@RequestParam String empleadoId,
            @RequestParam int mes, @RequestParam int ano) {
        return ResponseEntity
                .ok(consultarAsistenciaUseCase.generarResumenMensual(EmpleadoId.fromString(empleadoId), mes, ano));
    }
}
