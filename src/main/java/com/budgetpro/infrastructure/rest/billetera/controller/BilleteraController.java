package com.budgetpro.infrastructure.rest.billetera.controller;

import com.budgetpro.application.billetera.dto.SaldoResponse;
import com.budgetpro.application.billetera.port.in.ConsultarSaldoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para operaciones relacionadas con Billetera.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone los casos de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega a los puertos de entrada (UseCases).
 */
@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/saldo")
public class BilleteraController {

    private final ConsultarSaldoUseCase consultarSaldoUseCase;

    public BilleteraController(ConsultarSaldoUseCase consultarSaldoUseCase) {
        this.consultarSaldoUseCase = consultarSaldoUseCase;
    }

    /**
     * Consulta el saldo actual de la billetera de un proyecto.
     * 
     * @param proyectoId El ID del proyecto (obtenido de la URL path)
     * @return ResponseEntity con el saldo actual y código HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<SaldoResponse> consultarSaldo(@PathVariable UUID proyectoId) {
        // Delegar al caso de uso (puerto de entrada)
        SaldoResponse saldo = consultarSaldoUseCase.consultarPorProyecto(proyectoId);
        
        // Retornar respuesta con código 200 OK
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(saldo);
    }
}
