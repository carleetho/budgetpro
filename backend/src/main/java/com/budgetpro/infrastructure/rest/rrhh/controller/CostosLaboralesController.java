package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.ConsultarCostosLaboralesQuery;
import com.budgetpro.application.rrhh.dto.CostosLaboralesResponse;
import com.budgetpro.application.rrhh.port.in.ConsultarCostosLaboralesUseCase;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rrhh/costos")
public class CostosLaboralesController {

    private final ConsultarCostosLaboralesUseCase consultarCostosLaboralesUseCase;

    public CostosLaboralesController(ConsultarCostosLaboralesUseCase consultarCostosLaboralesUseCase) {
        this.consultarCostosLaboralesUseCase = consultarCostosLaboralesUseCase;
    }

    @GetMapping
    public ResponseEntity<CostosLaboralesResponse> consultarCostos(@RequestParam String proyectoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "EMPLEADO") ConsultarCostosLaboralesQuery.Agrupacion agruparPor,
            @RequestParam(defaultValue = "false") boolean incluirVarianza) {
        var query = new ConsultarCostosLaboralesQuery(ProyectoId.from(proyectoId), fechaInicio, fechaFin, agruparPor,
                incluirVarianza);
        return ResponseEntity.ok(consultarCostosLaboralesUseCase.consultarCostos(query));
    }
}
