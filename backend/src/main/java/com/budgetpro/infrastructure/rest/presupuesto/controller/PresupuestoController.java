package com.budgetpro.infrastructure.rest.presupuesto.controller;

import com.budgetpro.application.control.dto.ReporteControlCostosResponse;
import com.budgetpro.application.control.port.in.ConsultarControlCostosUseCase;
import com.budgetpro.application.presupuesto.dto.CrearPresupuestoCommand;
import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
import com.budgetpro.application.presupuesto.port.in.ConsultarPresupuestoUseCase;
import com.budgetpro.application.presupuesto.port.in.CrearPresupuestoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controller REST para operaciones de Presupuesto.
 */
@RestController
@RequestMapping("/api/v1/presupuestos")
public class PresupuestoController {

    private final CrearPresupuestoUseCase crearPresupuestoUseCase;
    private final AprobarPresupuestoUseCase aprobarPresupuestoUseCase;
    private final ConsultarPresupuestoUseCase consultarPresupuestoUseCase;
    private final ConsultarControlCostosUseCase consultarControlCostosUseCase;

    public PresupuestoController(CrearPresupuestoUseCase crearPresupuestoUseCase,
                                 AprobarPresupuestoUseCase aprobarPresupuestoUseCase,
                                 ConsultarPresupuestoUseCase consultarPresupuestoUseCase,
                                 ConsultarControlCostosUseCase consultarControlCostosUseCase) {
        this.crearPresupuestoUseCase = crearPresupuestoUseCase;
        this.aprobarPresupuestoUseCase = aprobarPresupuestoUseCase;
        this.consultarPresupuestoUseCase = consultarPresupuestoUseCase;
        this.consultarControlCostosUseCase = consultarControlCostosUseCase;
    }

    /**
     * Crea un nuevo presupuesto.
     * 
     * @param request Request con los datos del presupuesto
     * @return ResponseEntity con el presupuesto creado y código HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<PresupuestoResponse> crear(
            @RequestBody com.budgetpro.infrastructure.rest.presupuesto.dto.CrearPresupuestoRequest request) {
        CrearPresupuestoCommand command = new CrearPresupuestoCommand(
                request.proyectoId(),
                request.nombre()
        );

        PresupuestoResponse response = crearPresupuestoUseCase.crear(command);

        return ResponseEntity
                .created(URI.create("/api/v1/presupuestos/" + response.id()))
                .body(response);
    }

    /**
     * Aprueba un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto a aprobar
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @PostMapping("/{presupuestoId}/aprobar")
    public ResponseEntity<Void> aprobar(@PathVariable UUID presupuestoId) {
        aprobarPresupuestoUseCase.aprobar(presupuestoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Consulta un presupuesto por su ID.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return ResponseEntity con el presupuesto y código HTTP 200 OK
     */
    @GetMapping("/{presupuestoId}")
    public ResponseEntity<PresupuestoResponse> consultar(@PathVariable UUID presupuestoId) {
        PresupuestoResponse response = consultarPresupuestoUseCase.consultar(presupuestoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Consulta el reporte de control de costos (Plan vs Real) de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return ResponseEntity con el reporte de control de costos y código HTTP 200 OK
     */
    @GetMapping("/{presupuestoId}/control-costos")
    public ResponseEntity<ReporteControlCostosResponse> consultarControlCostos(@PathVariable UUID presupuestoId) {
        ReporteControlCostosResponse response = consultarControlCostosUseCase.consultar(presupuestoId);
        return ResponseEntity.ok(response);
    }
}
