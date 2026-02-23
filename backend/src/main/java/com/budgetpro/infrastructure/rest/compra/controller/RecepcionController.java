package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.application.compra.command.RecibirOrdenCompraCommand;
import com.budgetpro.application.compra.dto.RecepcionResponse;
import com.budgetpro.application.compra.exception.AuthenticationRequiredException;
import com.budgetpro.application.compra.port.in.RecibirOrdenCompraInputPort;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.Recepcion;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
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
@RequestMapping("/api/v1/compras")
public class RecepcionController {

    private final RecibirOrdenCompraInputPort recibirOrdenCompraInputPort;
    private final CompraRepository compraRepository;

    public RecepcionController(
            RecibirOrdenCompraInputPort recibirOrdenCompraInputPort,
            CompraRepository compraRepository) {
        this.recibirOrdenCompraInputPort = recibirOrdenCompraInputPort;
        this.compraRepository = compraRepository;
    }

    /**
     * Recibe una orden de compra (registra la recepción de productos).
     * 
     * @param compraId ID de la compra a recibir
     * @param request Request con los datos de la recepción
     * @param auth Objeto de autenticación de Spring Security
     * @return ResponseEntity con la recepción creada y código HTTP 201 CREATED
     */
    @PostMapping("/{compraId}/recepciones")
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<RecepcionResponse> recibirOrdenCompra(
            @PathVariable UUID compraId,
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
                compraId,
                request.fechaRecepcion(),
                request.guiaRemision(),
                detallesCommand,
                usuarioId
        );
        
        // Ejecutar caso de uso
        Recepcion recepcion = recibirOrdenCompraInputPort.ejecutar(command);
        
        // Cargar la compra para obtener el estado
        Compra compra = compraRepository.findById(CompraId.from(compraId))
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Compra no encontrada: %s", compraId)
                ));
        
        // Construir respuesta desde dominio
        RecepcionResponse response = toResponse(recepcion, compra);
        
        URI location = URI.create("/api/v1/compras/" + compraId + "/recepciones/" + recepcion.getId().getValue());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }
    
    /**
     * Convierte una Recepcion (dominio) a RecepcionResponse.
     * 
     * @param recepcion El agregado de dominio Recepcion
     * @param compra La compra asociada para obtener el estado
     * @return RecepcionResponse con todos los datos mapeados
     */
    private RecepcionResponse toResponse(Recepcion recepcion, Compra compra) {
        // Mapear detalles desde dominio
        List<RecepcionResponse.DetalleResponse> detallesResponse = recepcion.getDetalles().stream()
                .map(detalle -> {
                    // Buscar el detalle de compra correspondiente para calcular cantidadPendiente
                    CompraDetalle compraDetalle = compra.getDetalles().stream()
                            .filter(d -> d.getId().getValue().equals(detalle.getCompraDetalleId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                String.format("Detalle de compra no encontrado: %s", detalle.getCompraDetalleId())
                            ));
                    
                    // Calcular cantidad pendiente
                    java.math.BigDecimal cantidadPendiente = compraDetalle.getCantidadPendiente();
                    
                    // Obtener movimientoAlmacenId del dominio
                    UUID movimientoAlmacenId = detalle.getMovimientoAlmacenId().getValue();
                    
                    return new RecepcionResponse.DetalleResponse(
                            detalle.getRecursoId(),
                            detalle.getCantidadRecibida(),
                            cantidadPendiente,
                            detalle.getAlmacenId().getValue(),
                            movimientoAlmacenId
                    );
                })
                .collect(Collectors.toList());
        
        return new RecepcionResponse(
                recepcion.getId().getValue(),
                recepcion.getCompraId().getValue(),
                compra.getEstado(),
                recepcion.getFechaRecepcion(),
                recepcion.getGuiaRemision(),
                detallesResponse,
                recepcion.getCreadoPorUsuarioId(),
                recepcion.getFechaCreacion()
        );
    }
    
    /**
     * Obtiene el ID del usuario actual del contexto de seguridad.
     * 
     * @param auth Objeto de autenticación (puede ser null)
     * @return El UUID del usuario actual
     * @throws AuthenticationRequiredException si no se puede obtener el usuario del contexto
     */
    private UUID getCurrentUserId(Authentication auth) {
        Authentication authentication = auth != null ? auth : SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationRequiredException(
                "Se requiere autenticación para realizar esta operación"
            );
        }
        
        // Intentar obtener el userId del principal
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                throw new AuthenticationRequiredException(
                    "No se pudo extraer el ID de usuario del token de autenticación"
                );
            }
        }
        
        // Si el principal no es String, intentar extraer de UserDetails
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Por ahora, no soportamos este formato
            throw new AuthenticationRequiredException(
                "Formato de autenticación no soportado"
            );
        }
        
        throw new AuthenticationRequiredException(
            "No se pudo determinar el usuario autenticado"
        );
    }
}
