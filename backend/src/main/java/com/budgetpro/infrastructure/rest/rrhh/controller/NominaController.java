package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.NominaResponse;
import com.budgetpro.application.rrhh.port.in.CalcularNominaUseCase;
import com.budgetpro.application.rrhh.port.in.ConsultarNominaUseCase;
import com.budgetpro.domain.rrhh.model.NominaId;
import com.budgetpro.infrastructure.rest.rrhh.dto.CalcularNominaRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "RRHH (Under Development)", description = """
        ⚠️ UNDER DEVELOPMENT — madurez radiografía ~50%; riesgo alto de alucinación FE/IA.
        NO generar UI de nómina completa con IA (cálculos laborales / FSR / legislativos incompletos).
        Ver docs/frontend/RRHH_UNDER_DEVELOPMENT.md y RRHH_GAP_STUDY.md.
        """)
@RestController
@RequestMapping("/api/v1/rrhh/nominas")
@SecurityRequirement(name = "bearer-jwt")
public class NominaController {

    private final CalcularNominaUseCase calcularNominaUseCase;
    private final ConsultarNominaUseCase consultarNominaUseCase;

    public NominaController(CalcularNominaUseCase calcularNominaUseCase,
            ConsultarNominaUseCase consultarNominaUseCase) {
        this.calcularNominaUseCase = calcularNominaUseCase;
        this.consultarNominaUseCase = consultarNominaUseCase;
    }

    @PostMapping("/calcular")
    public ResponseEntity<NominaResponse> calcular(@RequestBody @Valid CalcularNominaRequest request) {
        var command = request.toCommand();
        var response = calcularNominaUseCase.calcularNomina(command);

        // Assuming response has an ID, though Calculate might return a summary or the
        // created payroll ID.
        // If response.id() exists.

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NominaResponse> obtenerPorId(@PathVariable String id) {
        return consultarNominaUseCase.obtenerPorId(NominaId.of(id)).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
