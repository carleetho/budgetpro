package com.budgetpro.infrastructure.rest.finanzas.controller;

import com.budgetpro.application.finanzas.dto.MovimientoResponse;
import com.budgetpro.application.finanzas.dto.SaldoResponse;
import com.budgetpro.application.finanzas.port.in.ConsultarSaldoUseCase;
import com.budgetpro.application.finanzas.port.in.IngresarFondosUseCase;
import com.budgetpro.infrastructure.rest.finanzas.dto.IngresarFondosRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para operaciones relacionadas con Billeteras.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone los casos de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega a los puertos de entrada (UseCases).
 * 
 * Cumple con el requisito S1-09: "API GET /saldo + /partidas"
 */
@RestController
@RequestMapping("/api/v1/billeteras")
public class BilleteraController {

    private final IngresarFondosUseCase ingresarFondosUseCase;
    private final ConsultarSaldoUseCase consultarSaldoUseCase;

    public BilleteraController(IngresarFondosUseCase ingresarFondosUseCase,
                              ConsultarSaldoUseCase consultarSaldoUseCase) {
        this.ingresarFondosUseCase = ingresarFondosUseCase;
        this.consultarSaldoUseCase = consultarSaldoUseCase;
    }

    /**
     * Ingresa fondos a la billetera de un proyecto.
     * 
     * Si el proyecto no tiene billetera, se crea automáticamente.
     * 
     * @param proyectoId El ID del proyecto (path variable)
     * @param request El request con los datos del ingreso (monto, referencia, evidenciaUrl)
     * @return ResponseEntity con el movimiento creado y código HTTP 201 CREATED
     */
    @PostMapping("/{proyectoId}/ingresos")
    public ResponseEntity<MovimientoResponse> ingresar(
            @PathVariable UUID proyectoId,
            @RequestBody @Valid IngresarFondosRequest request) {
        
        // Convertir Request DTO a Command (el proyectoId viene del path variable)
        var command = request.toCommand(proyectoId);
        
        // Delegar al caso de uso (puerto de entrada)
        MovimientoResponse response = ingresarFondosUseCase.ejecutar(command);
        
        // Retornar respuesta con código 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Consulta el saldo actual de la billetera de un proyecto.
     * 
     * @param proyectoId El ID del proyecto (path variable)
     * @return ResponseEntity con el saldo si existe, o 404 NOT FOUND si no existe la billetera
     */
    @GetMapping("/{proyectoId}/saldo")
    public ResponseEntity<SaldoResponse> consultarSaldo(@PathVariable UUID proyectoId) {
        // Delegar al caso de uso (puerto de entrada)
        var saldoOpt = consultarSaldoUseCase.ejecutar(proyectoId);
        
        // Si no existe la billetera, retornar 404 NOT FOUND
        if (saldoOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        
        // Retornar respuesta con código 200 OK y el saldo
        return ResponseEntity
                .ok(saldoOpt.get());
    }
}
