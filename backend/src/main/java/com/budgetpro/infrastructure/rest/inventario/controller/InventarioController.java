package com.budgetpro.infrastructure.rest.inventario.controller;

import com.budgetpro.application.inventario.dto.InventarioItemResponse;
import com.budgetpro.application.inventario.port.in.ConsultarInventarioUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operaciones de Inventario.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
public class InventarioController {

    private final ConsultarInventarioUseCase consultarInventarioUseCase;

    public InventarioController(ConsultarInventarioUseCase consultarInventarioUseCase) {
        this.consultarInventarioUseCase = consultarInventarioUseCase;
    }

    /**
     * Consulta todos los items de inventario de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return ResponseEntity con la lista de items de inventario
     */
    @GetMapping("/{proyectoId}/inventario")
    public ResponseEntity<List<InventarioItemResponse>> consultarPorProyecto(@PathVariable UUID proyectoId) {
        List<InventarioItemResponse> items = consultarInventarioUseCase.consultarPorProyecto(proyectoId);
        return ResponseEntity.ok(items);
    }
}
