package com.budgetpro.infrastructure.rest.cronograma.controller;

import com.budgetpro.application.cronograma.dto.ActividadProgramadaResponse;
import com.budgetpro.application.cronograma.dto.CronogramaResponse;
import com.budgetpro.application.cronograma.dto.ProgramarActividadCommand;
import com.budgetpro.application.cronograma.port.in.ConsultarCronogramaUseCase;
import com.budgetpro.application.cronograma.port.in.ProgramarActividadUseCase;
import com.budgetpro.infrastructure.rest.cronograma.dto.ProgramarActividadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de cronograma.
 */
@Tag(name = "Cronograma", description = """
        Planificación temporal / Gantt. Madurez radiografía ~60% (medio).
        ⚠️ Edge cases de CPM / días hábiles: revisar con humano (C-04).
        """)
@RestController
@RequestMapping("/api/v1/proyectos")
@SecurityRequirement(name = "bearer-jwt")
public class CronogramaController {

    private final ProgramarActividadUseCase programarActividadUseCase;
    private final ConsultarCronogramaUseCase consultarCronogramaUseCase;

    public CronogramaController(ProgramarActividadUseCase programarActividadUseCase,
                                ConsultarCronogramaUseCase consultarCronogramaUseCase) {
        this.programarActividadUseCase = programarActividadUseCase;
        this.consultarCronogramaUseCase = consultarCronogramaUseCase;
    }

    @Operation(
            summary = "Programar actividad",
            description = """
                    Programa o actualiza una actividad (partida) con fechas y predecesoras.
                    ⚠️ Dependencias cíclicas / CPM: revisar cuidadosamente código generado por IA.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad programada",
                    content = @Content(schema = @Schema(implementation = ActividadProgramadaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Proyecto/partida no encontrado")
    })
    @PostMapping("/{proyectoId}/cronograma/actividades")
    public ResponseEntity<ActividadProgramadaResponse> programarActividad(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId,
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

    @Operation(summary = "Consultar cronograma", description = "Datos de Gantt del proyecto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma",
                    content = @Content(schema = @Schema(implementation = CronogramaResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping("/{proyectoId}/cronograma")
    public ResponseEntity<CronogramaResponse> consultarCronograma(
            @Parameter(description = "ID del proyecto", required = true) @PathVariable UUID proyectoId) {
        CronogramaResponse response = consultarCronogramaUseCase.consultar(proyectoId);
        return ResponseEntity.ok(response);
    }
}
