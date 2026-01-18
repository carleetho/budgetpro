package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.application.compra.dto.CompraDetalleCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraResponse;
import com.budgetpro.application.compra.port.in.RegistrarCompraUseCase;
import com.budgetpro.infrastructure.rest.compra.dto.CompraDetalleRequest;
import com.budgetpro.infrastructure.rest.compra.dto.RegistrarCompraRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Controller REST para operaciones de Compra.
 */
@RestController
@RequestMapping("/api/v1/compras")
public class CompraController {

    private final RegistrarCompraUseCase registrarCompraUseCase;

    public CompraController(RegistrarCompraUseCase registrarCompraUseCase) {
        this.registrarCompraUseCase = registrarCompraUseCase;
    }

    /**
     * Registra una nueva compra.
     * 
     * @param request Request con los datos de la compra
     * @return ResponseEntity con la compra registrada y código HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<RegistrarCompraResponse> registrar(@Valid @RequestBody RegistrarCompraRequest request) {
        // Mapear detalles del request
        java.util.List<CompraDetalleCommand> detallesCommand = request.detalles().stream()
                .map(detalle -> new CompraDetalleCommand(
                    detalle.recursoId(),
                    detalle.partidaId(),
                    detalle.naturalezaGasto(),
                    detalle.relacionContractual(),
                    detalle.rubroInsumo(),
                    detalle.cantidad(),
                    detalle.precioUnitario()
                ))
                .collect(Collectors.toList());

        RegistrarCompraCommand command = new RegistrarCompraCommand(
                request.proyectoId(),
                request.fecha(),
                request.proveedor(),
                detallesCommand
        );

        RegistrarCompraResponse response = registrarCompraUseCase.registrar(command);

        return ResponseEntity
                .created(URI.create("/api/v1/compras/" + response.id()))
                .body(response);
    }
}
