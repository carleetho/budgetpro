package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.application.compra.exception.AuthenticationRequiredException;
import com.budgetpro.domain.logistica.compra.model.Proveedor;
import com.budgetpro.domain.logistica.compra.model.ProveedorEstado;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import com.budgetpro.infrastructure.rest.compra.dto.ProveedorRequest;
import com.budgetpro.infrastructure.rest.compra.dto.ProveedorResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    public ProveedorController(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        UUID userId = getCurrentUserId();

        if (proveedorRepository.existsByRuc(request.ruc())) {
            throw new IllegalArgumentException("Ya existe un proveedor con RUC: " + request.ruc());
        }

        Proveedor proveedor = Proveedor.crear(
                ProveedorId.nuevo(),
                request.razonSocial(),
                request.ruc(),
                request.contacto(),
                request.direccion(),
                userId,
                LocalDateTime.now()
        );

        proveedorRepository.save(proveedor);

        ProveedorResponse response = toResponse(proveedor);
        return ResponseEntity.created(URI.create("/api/v1/proveedores/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtener(@PathVariable UUID id) {
        Proveedor proveedor = proveedorRepository.findById(ProveedorId.from(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Proveedor no encontrado: " + id));
        return ResponseEntity.ok(toResponse(proveedor));
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listar(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "50") int size) {
        if (page < 0 || size <= 0 || size > 200) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        List<Proveedor> all = proveedorRepository.findAll();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return ResponseEntity.ok(all.subList(from, to).stream().map(ProveedorController::toResponse).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody ProveedorRequest request) {
        UUID userId = getCurrentUserId();
        Proveedor proveedor = proveedorRepository.findById(ProveedorId.from(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Proveedor no encontrado: " + id));

        // Campos actualizables
        if (request.contacto() != null) {
            proveedor.actualizarContacto(request.contacto(), userId, LocalDateTime.now());
        }
        if (request.direccion() != null) {
            proveedor.actualizarDireccion(request.direccion(), userId, LocalDateTime.now());
        }
        if (request.estado() != null) {
            ProveedorEstado estado = ProveedorEstado.valueOf(request.estado().toUpperCase());
            switch (estado) {
                case ACTIVO -> proveedor.activar(userId, LocalDateTime.now());
                case INACTIVO -> proveedor.inactivar(userId, LocalDateTime.now());
                case BLOQUEADO -> proveedor.bloquear(userId, LocalDateTime.now());
            }
        }

        proveedorRepository.save(proveedor);
        return ResponseEntity.ok(toResponse(proveedor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        ProveedorId proveedorId = ProveedorId.from(id);
        if (proveedorRepository.isReferencedByOrdenCompra(proveedorId)) {
            throw new IllegalStateException("No se puede eliminar proveedor referenciado por órdenes de compra");
        }
        proveedorRepository.delete(proveedorId);
        return ResponseEntity.noContent().build();
    }

    private static ProveedorResponse toResponse(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId().getValue(),
                proveedor.getRazonSocial(),
                proveedor.getRuc(),
                proveedor.getEstado()
        );
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationRequiredException("Se requiere autenticación para realizar esta operación");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException e) {
                throw new AuthenticationRequiredException("No se pudo extraer el ID de usuario del token de autenticación");
            }
        }

        throw new AuthenticationRequiredException("No se pudo determinar el usuario autenticado");
    }
}

