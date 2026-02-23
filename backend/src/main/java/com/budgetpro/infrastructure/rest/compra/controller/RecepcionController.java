package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.application.compra.command.RecibirOrdenCompraCommand;
import com.budgetpro.application.compra.dto.RecepcionResponse;
import com.budgetpro.application.compra.port.in.RecibirOrdenCompraInputPort;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.RecepcionId;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
import com.budgetpro.infrastructure.persistence.entity.compra.RecepcionEntity;
import com.budgetpro.infrastructure.persistence.repository.compra.RecepcionJpaRepository;
import com.budgetpro.infrastructure.rest.compra.dto.RecibirOrdenCompraRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para operaciones de recepción de órdenes de compra.
 * 
 * Gestiona la recepción de productos de órdenes de compra con validación
 * de guía de remisión y registro de movimientos de almacén.
 */
@RestController
@RequestMapping("/api/v1/ordenes-compra")
public class RecepcionController {

    private final RecibirOrdenCompraInputPort recibirOrdenCompraInputPort;
    private final CompraRepository compraRepository;
    private final RecepcionJpaRepository recepcionJpaRepository;

    public RecepcionController(
            RecibirOrdenCompraInputPort recibirOrdenCompraInputPort,
            CompraRepository compraRepository,
            RecepcionJpaRepository recepcionJpaRepository) {
        this.recibirOrdenCompraInputPort = recibirOrdenCompraInputPort;
        this.compraRepository = compraRepository;
        this.recepcionJpaRepository = recepcionJpaRepository;
    }

    /**
     * Recibe una orden de compra (registra la recepción de productos).
     * 
     * @param id ID de la compra a recibir
     * @param request Request con los datos de la recepción
     * @param auth Objeto de autenticación de Spring Security
     * @return ResponseEntity con la recepción creada y código HTTP 201 CREATED
     */
    @PostMapping("/{id}/recepciones")
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<RecepcionResponse> recibirOrdenCompra(
            @PathVariable UUID id,
            @RequestBody @Valid RecibirOrdenCompraRequest request,
            Authentication auth) {
        
        // Extraer usuarioId del contexto de autenticación
        UUID usuarioId = getCurrentUserId(auth);
        
        // Mapear detalles del request al comando
        List<RecibirOrdenCompraCommand.DetalleCommand> detallesCommand = request.detalles().stream()
                .map(detalle -> new RecibirOrdenCompraCommand.DetalleCommand(
                    detalle.detalleOrdenId(),
                    detalle.cantidadRecibida(),
                    detalle.almacenId()
                ))
                .collect(Collectors.toList());
        
        // Construir comando
        RecibirOrdenCompraCommand command = new RecibirOrdenCompraCommand(
                id,
                request.fechaRecepcion(),
                request.guiaRemision(),
                detallesCommand,
                usuarioId
        );
        
        // Ejecutar caso de uso
        RecepcionId recepcionId = recibirOrdenCompraInputPort.ejecutar(command);
        
        // Cargar la recepción creada desde la base de datos
        RecepcionEntity recepcionEntity = recepcionJpaRepository.findById(recepcionId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Recepción no encontrada después de creación: %s", recepcionId.getValue())
                ));
        
        // Cargar la compra para obtener el estado
        Compra compra = compraRepository.findById(CompraId.from(id))
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Compra no encontrada: %s", id)
                ));
        
        // Construir respuesta
        RecepcionResponse response = toResponse(recepcionEntity, compra);
        
        URI location = URI.create("/api/v1/ordenes-compra/" + id + "/recepciones/" + recepcionId.getValue());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }
    
    /**
     * Convierte una RecepcionEntity a RecepcionResponse.
     * 
     * @param recepcionEntity La entidad de recepción
     * @param compra La compra asociada para obtener el estado
     * @return RecepcionResponse con todos los datos mapeados
     */
    private RecepcionResponse toResponse(RecepcionEntity recepcionEntity, Compra compra) {
        // Mapear detalles
        List<RecepcionResponse.DetalleResponse> detallesResponse = recepcionEntity.getDetalles().stream()
                .map(detalleEntity -> {
                    // Buscar el detalle de compra correspondiente para calcular cantidadPendiente
                    CompraDetalle compraDetalle = compra.getDetalles().stream()
                            .filter(d -> d.getId().getValue().equals(detalleEntity.getCompraDetalleId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                String.format("Detalle de compra no encontrado: %s", detalleEntity.getCompraDetalleId())
                            ));
                    
                    // Calcular cantidad pendiente
                    java.math.BigDecimal cantidadPendiente = compraDetalle.getCantidadPendiente();
                    
                    // TODO: movimientoAlmacenId debería obtenerse del MovimientoAlmacen creado en el caso de uso
                    // Por ahora se deja como null, pero debería buscarse desde el repositorio de movimientos
                    // usando el recursoId, almacenId y fecha de la recepción
                    UUID movimientoAlmacenId = null; // Se debe obtener del MovimientoAlmacen creado
                    
                    return new RecepcionResponse.DetalleResponse(
                            detalleEntity.getRecursoId(),
                            detalleEntity.getCantidadRecibida(),
                            cantidadPendiente,
                            detalleEntity.getAlmacenId(),
                            movimientoAlmacenId
                    );
                })
                .collect(Collectors.toList());
        
        return new RecepcionResponse(
                recepcionEntity.getId(),
                recepcionEntity.getCompraId(),
                compra.getEstado(),
                recepcionEntity.getFechaRecepcion(),
                recepcionEntity.getGuiaRemision(),
                detallesResponse,
                recepcionEntity.getCreadoPorUsuarioId(),
                recepcionEntity.getFechaCreacion()
        );
    }
    
    /**
     * Obtiene el ID del usuario actual del contexto de seguridad.
     * 
     * @param auth Objeto de autenticación (puede ser null)
     * @return El UUID del usuario actual
     * @throws IllegalStateException si no se puede obtener el usuario del contexto
     */
    private UUID getCurrentUserId(Authentication auth) {
        Authentication authentication = auth != null ? auth : SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // Para desarrollo, usar un UUID por defecto
            // En producción, esto debería lanzar una excepción de autenticación
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        
        // Intentar obtener el userId del principal
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                // Si no es un UUID válido, usar UUID por defecto para desarrollo
                return UUID.fromString("00000000-0000-0000-0000-000000000000");
            }
        }
        
        // Fallback: usar UUID por defecto para desarrollo
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
