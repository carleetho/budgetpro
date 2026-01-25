package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.CuadrillaResponse;
import com.budgetpro.application.rrhh.port.in.ActualizarCuadrillaUseCase;
import com.budgetpro.application.rrhh.port.in.AsignarCuadrillaActividadUseCase;
import com.budgetpro.application.rrhh.port.in.ConsultarCuadrillaUseCase;
import com.budgetpro.application.rrhh.port.in.CrearCuadrillaUseCase;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.infrastructure.rest.rrhh.dto.ActualizarMiembrosRequest;
import com.budgetpro.infrastructure.rest.rrhh.dto.AsignarActividadRequest;
import com.budgetpro.infrastructure.rest.rrhh.dto.CrearCuadrillaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rrhh/cuadrillas")
public class CuadrillaController {

    private final CrearCuadrillaUseCase crearCuadrillaUseCase;
    private final ActualizarCuadrillaUseCase actualizarCuadrillaUseCase;
    private final AsignarCuadrillaActividadUseCase asignarCuadrillaActividadUseCase;
    private final ConsultarCuadrillaUseCase consultarCuadrillaUseCase;

    public CuadrillaController(CrearCuadrillaUseCase crearCuadrillaUseCase,
            ActualizarCuadrillaUseCase actualizarCuadrillaUseCase,
            AsignarCuadrillaActividadUseCase asignarCuadrillaActividadUseCase,
            ConsultarCuadrillaUseCase consultarCuadrillaUseCase) {
        this.crearCuadrillaUseCase = crearCuadrillaUseCase;
        this.actualizarCuadrillaUseCase = actualizarCuadrillaUseCase;
        this.asignarCuadrillaActividadUseCase = asignarCuadrillaActividadUseCase;
        this.consultarCuadrillaUseCase = consultarCuadrillaUseCase;
    }

    @PostMapping
    public ResponseEntity<CuadrillaResponse> crear(@RequestBody @Valid CrearCuadrillaRequest request) {
        var command = request.toCommand();
        var response = crearCuadrillaUseCase.crearCuadrilla(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuadrillaResponse> obtenerPorId(@PathVariable String id) {
        return consultarCuadrillaUseCase.findById(CuadrillaId.of(UUID.fromString(id))).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CuadrillaResponse>> listar() {
        return ResponseEntity.ok(consultarCuadrillaUseCase.findAll());
    }

    @PutMapping("/{id}/miembros")
    public ResponseEntity<CuadrillaResponse> actualizarMiembros(@PathVariable String id,
            @RequestBody @Valid ActualizarMiembrosRequest request) {
        var command = request.toCommand(id);
        var response = actualizarCuadrillaUseCase.actualizarCuadrilla(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/actividades")
    public ResponseEntity<Void> asignarActividad(@PathVariable String id,
            @RequestBody @Valid AsignarActividadRequest request) {
        var command = request.toCommand(id);
        asignarCuadrillaActividadUseCase.asignarCuadrilla(command);
        return ResponseEntity.ok().build();
    }
}
