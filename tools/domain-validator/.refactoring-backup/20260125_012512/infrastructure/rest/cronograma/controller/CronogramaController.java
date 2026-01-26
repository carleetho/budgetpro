package com.budgetpro.infrastructure.rest.cronograma.controller;

import com.budgetpro.application.cronograma.dto.ActividadProgramadaResponse;
import com.budgetpro.application.cronograma.dto.CronogramaResponse;
import com.budgetpro.application.cronograma.dto.ProgramarActividadCommand;
import com.budgetpro.application.cronograma.port.in.ConsultarCronogramaUseCase;
import com.budgetpro.application.cronograma.port.in.ProgramarActividadUseCase;
import com.budgetpro.infrastructure.rest.cronograma.dto.ProgramarActividadRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de cronograma.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
public class CronogramaController {

    private final ProgramarActividadUseCase programarActividadUseCase;
    private final ConsultarCronogramaUseCase consultarCronogramaUseCase;

    public CronogramaController(ProgramarActividadUseCase programarActividadUseCase,
                                ConsultarCronogramaUseCase consultarCronogramaUseCase) {
        this.programarActividadUseCase = programarActividadUseCase;
        this.consultarCronogramaUseCase = consultarCronogramaUseCase;
    }

    /**
     * Programa o actualiza una actividad en el cronograma.
     * 
     * @param proyectoId El ID del proyecto
     * @param request Request con los datos de la actividad
     * @return ResponseEntity con la actividad programada
     */
    @PostMapping("/{proyectoId}/cronograma/actividades")
    public ResponseEntity<ActividadProgramadaResponse> programarActividad(
            @PathVariable UUID proyectoId,
            @Valid @RequestBody ProgramarActividadRequest request) {
        
        ProgramarActividadCommand command = new ProgramarActividadCommand(
                proyectoId,
                request.partidaId(),
                request.fechaInicio(),
                request.fechaFin(),
                request.predecesoras()
        );

        ActividadProgramadaResponse response = programarActividadUseCase.programar(command);

        return ResponseEntity
                .ok()
                .location(URI.create("/api/v1/proyectos/" + proyectoId + "/cronograma/actividades/" + response.id()))
                .body(response);
    }

    /**
     * Consulta el cronograma completo de un proyecto (Gantt de datos).
     * 
     * @param proyectoId El ID del proyecto
     * @return ResponseEntity con el cronograma completo
     */
    @GetMapping("/{proyectoId}/cronograma")
    public ResponseEntity<CronogramaResponse> consultarCronograma(@PathVariable UUID proyectoId) {
        CronogramaResponse response = consultarCronogramaUseCase.consultar(proyectoId);
        return ResponseEntity.ok(response);
    }
}
