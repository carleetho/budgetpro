package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.application.compra.dto.RegistrarCompraDirectaResponse;
import com.budgetpro.application.compra.port.in.RegistrarCompraDirectaUseCase;
import com.budgetpro.infrastructure.rest.compra.dto.RegistrarCompraDirectaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones relacionadas con Compras.
 * 
 * Adaptador de entrada (Inbound Adapter) que expone los casos de uso a través de HTTP.
 * Sigue los principios de Arquitectura Hexagonal: NO contiene lógica de negocio,
 * solo mapea requests/responses y delega a los puertos de entrada (UseCases).
 */
@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {

    private final RegistrarCompraDirectaUseCase registrarCompraDirectaUseCase;

    public CompraController(RegistrarCompraDirectaUseCase registrarCompraDirectaUseCase) {
        this.registrarCompraDirectaUseCase = registrarCompraDirectaUseCase;
    }

    /**
     * Registra una compra directa.
     * 
     * @param request El request con los datos de la compra a registrar
     * @return ResponseEntity con la respuesta conteniendo el ID de la compra registrada y código HTTP 201 CREATED
     */
    @PostMapping("/directa")
    public ResponseEntity<RegistrarCompraDirectaResponse> registrarCompraDirecta(
            @RequestBody @Valid RegistrarCompraDirectaRequest request) {
        // Convertir Request DTO a Command
        var command = request.toCommand();
        
        // Delegar al caso de uso (puerto de entrada)
        RegistrarCompraDirectaResponse response = registrarCompraDirectaUseCase.ejecutar(command);
        
        // Retornar respuesta con código 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
