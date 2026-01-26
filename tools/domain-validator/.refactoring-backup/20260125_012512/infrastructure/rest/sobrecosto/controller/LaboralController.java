package com.budgetpro.infrastructure.rest.sobrecosto.controller;

import com.budgetpro.application.sobrecosto.dto.ConfiguracionLaboralResponse;
import com.budgetpro.application.sobrecosto.dto.ConfigurarLaboralCommand;
import com.budgetpro.application.sobrecosto.port.in.ConfigurarLaboralUseCase;
import com.budgetpro.infrastructure.rest.sobrecosto.dto.ConfigurarLaboralRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de configuración laboral (FSR).
 */
@RestController
@RequestMapping("/api/v1")
public class LaboralController {

    private final ConfigurarLaboralUseCase configurarLaboralUseCase;

    public LaboralController(ConfigurarLaboralUseCase configurarLaboralUseCase) {
        this.configurarLaboralUseCase = configurarLaboralUseCase;
    }

    /**
     * Configura o actualiza la configuración laboral global (singleton).
     * 
     * @param request Request con los parámetros laborales
     * @return ResponseEntity con la configuración y el FSR calculado
     */
    @PutMapping("/configuracion-laboral")
    public ResponseEntity<ConfiguracionLaboralResponse> configurarLaboralGlobal(
            @Valid @RequestBody ConfigurarLaboralRequest request) {
        
        ConfigurarLaboralCommand command = new ConfigurarLaboralCommand(
                null, // proyectoId null = configuración global
                request.diasAguinaldo(),
                request.diasVacaciones(),
                request.porcentajeSeguridadSocial(),
                request.diasNoTrabajados(),
                request.diasLaborablesAno()
        );

        ConfiguracionLaboralResponse response = configurarLaboralUseCase.configurar(command);

        return ResponseEntity
                .ok()
                .location(URI.create("/api/v1/configuracion-laboral"))
                .body(response);
    }

    /**
     * Configura o actualiza la configuración laboral de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @param request Request con los parámetros laborales
     * @return ResponseEntity con la configuración y el FSR calculado
     */
    @PutMapping("/proyectos/{proyectoId}/configuracion-laboral")
    public ResponseEntity<ConfiguracionLaboralResponse> configurarLaboralProyecto(
            @PathVariable UUID proyectoId,
            @Valid @RequestBody ConfigurarLaboralRequest request) {
        
        ConfigurarLaboralCommand command = new ConfigurarLaboralCommand(
                proyectoId,
                request.diasAguinaldo(),
                request.diasVacaciones(),
                request.porcentajeSeguridadSocial(),
                request.diasNoTrabajados(),
                request.diasLaborablesAno()
        );

        ConfiguracionLaboralResponse response = configurarLaboralUseCase.configurar(command);

        return ResponseEntity
                .ok()
                .location(URI.create("/api/v1/proyectos/" + proyectoId + "/configuracion-laboral"))
                .body(response);
    }
}
