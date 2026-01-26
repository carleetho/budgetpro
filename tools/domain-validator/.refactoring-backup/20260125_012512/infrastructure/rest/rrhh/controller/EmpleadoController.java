package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.application.rrhh.port.in.ActualizarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.in.ConsultarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.in.CrearEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.in.InactivarEmpleadoUseCase;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.infrastructure.rest.rrhh.dto.ActualizarEmpleadoRequest;
import com.budgetpro.infrastructure.rest.rrhh.dto.CrearEmpleadoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rrhh/empleados")
public class EmpleadoController {

    private final CrearEmpleadoUseCase crearEmpleadoUseCase;
    private final ActualizarEmpleadoUseCase actualizarEmpleadoUseCase;
    private final ConsultarEmpleadoUseCase consultarEmpleadoUseCase;
    private final InactivarEmpleadoUseCase inactivarEmpleadoUseCase;

    public EmpleadoController(CrearEmpleadoUseCase crearEmpleadoUseCase,
            ActualizarEmpleadoUseCase actualizarEmpleadoUseCase, ConsultarEmpleadoUseCase consultarEmpleadoUseCase,
            InactivarEmpleadoUseCase inactivarEmpleadoUseCase) {
        this.crearEmpleadoUseCase = crearEmpleadoUseCase;
        this.actualizarEmpleadoUseCase = actualizarEmpleadoUseCase;
        this.consultarEmpleadoUseCase = consultarEmpleadoUseCase;
        this.inactivarEmpleadoUseCase = inactivarEmpleadoUseCase;
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponse> crear(@RequestBody @Valid CrearEmpleadoRequest request) {
        var command = request.toCommand();
        var response = crearEmpleadoUseCase.ejecutar(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> obtenerPorId(@PathVariable String id) {
        return consultarEmpleadoUseCase.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponse>> listar(@RequestParam(required = false) EstadoEmpleado estado) {
        if (estado != null) {
            return ResponseEntity.ok(consultarEmpleadoUseCase.findByEstado(estado));
        }
        return ResponseEntity.ok(consultarEmpleadoUseCase.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> actualizar(@PathVariable String id,
            @RequestBody @Valid ActualizarEmpleadoRequest request) {
        var command = request.toCommand(id);
        var response = actualizarEmpleadoUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        inactivarEmpleadoUseCase.ejecutar(id);
        return ResponseEntity.noContent().build();
    }
}
