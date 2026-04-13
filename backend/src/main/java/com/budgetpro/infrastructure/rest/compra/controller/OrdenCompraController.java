package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.domain.logistica.compra.model.DetalleOrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.model.Proveedor;
import com.budgetpro.domain.logistica.compra.port.in.AprobarOrdenCompraUseCase;
import com.budgetpro.domain.logistica.compra.port.in.ConfirmarRecepcionUseCase;
import com.budgetpro.domain.logistica.compra.port.in.CrearOrdenCompraUseCase;
import com.budgetpro.domain.logistica.compra.port.in.EnviarOrdenCompraUseCase;
import com.budgetpro.domain.logistica.compra.port.in.SolicitarAprobacionUseCase;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import com.budgetpro.application.compra.exception.AuthenticationRequiredException;
import com.budgetpro.infrastructure.rest.compra.dto.DetalleOrdenCompraRequest;
import com.budgetpro.infrastructure.rest.compra.dto.DetalleOrdenCompraResponse;
import com.budgetpro.infrastructure.rest.compra.dto.OrdenCompraRequest;
import com.budgetpro.infrastructure.rest.compra.dto.OrdenCompraResponse;
import com.budgetpro.infrastructure.rest.compra.dto.ProveedorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para operaciones de Orden de Compra.
 * 
 * Gestiona el ciclo de vida completo de las órdenes de compra, desde su creación
 * en estado BORRADOR hasta la confirmación de recepción en estado RECIBIDA.
 * 
 * El flujo de estados es: BORRADOR → SOLICITADA → APROBADA → ENVIADA → RECIBIDA
 */
@Tag(name = "Órdenes de Compra", description = "API para gestión de órdenes de compra y su ciclo de vida")
@RestController
@RequestMapping("/api/v1/ordenes-compra")
@SecurityRequirement(name = "bearer-jwt")
public class OrdenCompraController {

    private final CrearOrdenCompraUseCase crearOrdenCompraUseCase;
    private final SolicitarAprobacionUseCase solicitarAprobacionUseCase;
    private final AprobarOrdenCompraUseCase aprobarOrdenCompraUseCase;
    private final EnviarOrdenCompraUseCase enviarOrdenCompraUseCase;
    private final ConfirmarRecepcionUseCase confirmarRecepcionUseCase;
    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;

    public OrdenCompraController(CrearOrdenCompraUseCase crearOrdenCompraUseCase,
                                SolicitarAprobacionUseCase solicitarAprobacionUseCase,
                                AprobarOrdenCompraUseCase aprobarOrdenCompraUseCase,
                                EnviarOrdenCompraUseCase enviarOrdenCompraUseCase,
                                ConfirmarRecepcionUseCase confirmarRecepcionUseCase,
                                OrdenCompraRepository ordenCompraRepository,
                                ProveedorRepository proveedorRepository) {
        this.crearOrdenCompraUseCase = crearOrdenCompraUseCase;
        this.solicitarAprobacionUseCase = solicitarAprobacionUseCase;
        this.aprobarOrdenCompraUseCase = aprobarOrdenCompraUseCase;
        this.enviarOrdenCompraUseCase = enviarOrdenCompraUseCase;
        this.confirmarRecepcionUseCase = confirmarRecepcionUseCase;
        this.ordenCompraRepository = ordenCompraRepository;
        this.proveedorRepository = proveedorRepository;
    }

    /**
     * Crea una nueva orden de compra en estado BORRADOR.
     * 
     * @param request Request con los datos de la orden de compra
     * @return ResponseEntity con la orden de compra creada y código HTTP 201 CREATED
     */
    @Operation(
            summary = "Crear orden de compra",
            description = """
                    Crea una nueva orden de compra en estado BORRADOR.
                    
                    La orden se crea con:
                    - Número secuencial automático (formato: PO-YYYY-NNN)
                    - Estado inicial: BORRADOR
                    - Monto total calculado automáticamente desde los detalles
                    - Metadata de auditoría (createdBy, createdAt)
                    
                    **Validaciones aplicadas:**
                    - El proveedor debe existir y estar ACTIVO (L-04)
                    - Todos los detalles deben tener partidas válidas (REGLA-153)
                    - El monto total no puede exceder el presupuesto disponible (L-01)
                    """,
            tags = {"Órdenes de Compra"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Orden de compra creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrdenCompraResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos (validación de Bean Validation)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT faltante o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Permisos insuficientes"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Violación de regla de negocio (L-01, L-04, REGLA-153)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    @PostMapping
    public ResponseEntity<OrdenCompraResponse> crear(@Valid @RequestBody OrdenCompraRequest request) {
        UUID userId = getCurrentUserId();
        
        // Mapear detalles del request
        List<CrearOrdenCompraUseCase.DetalleCommand> detallesCommand = request.detalles().stream()
                .map(detalle -> new CrearOrdenCompraUseCase.DetalleCommand(
                    detalle.partidaId(),
                    detalle.descripcion(),
                    detalle.cantidad(),
                    detalle.unidad(),
                    detalle.precioUnitario()
                ))
                .collect(Collectors.toList());

        CrearOrdenCompraUseCase.CrearOrdenCompraCommand command = new CrearOrdenCompraUseCase.CrearOrdenCompraCommand(
                request.proyectoId(),
                request.proveedorId(),
                request.fecha(),
                request.condicionesPago(),
                request.observaciones(),
                detallesCommand,
                userId
        );

        OrdenCompraId ordenId = crearOrdenCompraUseCase.crear(command);
        
        // Cargar la orden creada para construir la respuesta
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ordenId)
                .orElseThrow(() -> new IllegalStateException("Orden de compra no encontrada después de creación"));

        OrdenCompraResponse response = toResponse(ordenCompra);

        URI location = URI.create("/api/v1/ordenes-compra/" + response.id());
        return ResponseEntity
                .created(location)
                .body(response);
    }

    /**
     * Lista órdenes de compra con filtros opcionales.
     * 
     * @param proyectoId Filtro opcional por proyecto
     * @param estado Filtro opcional por estado
     * @return ResponseEntity con la lista de órdenes de compra
     */
    @Operation(
            summary = "Listar órdenes de compra",
            description = """
                    Lista órdenes de compra con filtros opcionales.
                    
                    **Filtros disponibles:**
                    - `proyectoId`: Filtrar por proyecto específico
                    - `estado`: Filtrar por estado (BORRADOR, SOLICITADA, APROBADA, ENVIADA, RECIBIDA)
                    
                    **Nota:** Si no se proporcionan filtros, retorna lista vacía.
                    En producción se recomienda implementar paginación.
                    """,
            tags = {"Órdenes de Compra"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de órdenes de compra obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = OrdenCompraResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<OrdenCompraResponse>> listar(
            @Parameter(description = "ID del proyecto para filtrar", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID proyectoId,
            @Parameter(description = "Estado de la orden para filtrar", example = "BORRADOR")
            @RequestParam(required = false) OrdenCompraEstado estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<OrdenCompra> ordenes;
        
        if (proyectoId != null && estado != null) {
            ordenes = ordenCompraRepository.findByProyectoIdAndEstado(proyectoId, estado);
        } else if (proyectoId != null) {
            ordenes = ordenCompraRepository.findByProyectoId(proyectoId);
        } else if (estado != null) {
            ordenes = ordenCompraRepository.findByEstado(estado);
        } else {
            ordenes = ordenCompraRepository.findAll();
        }

        if (page < 0 || size <= 0 || size > 200) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        int from = Math.min(page * size, ordenes.size());
        int to = Math.min(from + size, ordenes.size());

        List<OrdenCompraResponse> response = ordenes.subList(from, to).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazar(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        OrdenCompra ordenCompra = ordenCompraRepository.findById(OrdenCompraId.from(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        String.format("Orden de compra no encontrada: %s", id)
                ));
        ordenCompra.rechazar(userId, LocalDateTime.now());
        ordenCompraRepository.save(ordenCompra);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene una orden de compra por su ID.
     * 
     * @param id El ID de la orden de compra
     * @return ResponseEntity con la orden de compra y código HTTP 200 OK
     */
    @Operation(
            summary = "Obtener orden de compra por ID",
            description = "Obtiene los detalles completos de una orden de compra por su identificador único.",
            tags = {"Órdenes de Compra"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden de compra encontrada",
                    content = @Content(schema = @Schema(implementation = OrdenCompraResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> obtener(
            @Parameter(description = "ID único de la orden de compra", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        OrdenCompra ordenCompra = ordenCompraRepository.findById(OrdenCompraId.from(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                    String.format("Orden de compra no encontrada: %s", id)
                ));

        OrdenCompraResponse response = toResponse(ordenCompra);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza una orden de compra en estado BORRADOR.
     * 
     * Actualiza solo condicionesPago y observaciones.
     * Para modificar detalles, se debe eliminar y recrear la orden.
     * 
     * @param id El ID de la orden de compra
     * @param request Request con los datos actualizados
     * @return ResponseEntity con la orden de compra actualizada y código HTTP 200 OK
     */
    @Operation(
            summary = "Actualizar orden de compra",
            description = """
                    Actualiza una orden de compra en estado BORRADOR.
                    
                    **Campos actualizables:**
                    - `condicionesPago`: Condiciones de pago
                    - `observaciones`: Observaciones adicionales
                    - `detalles`: Se pueden agregar nuevos detalles (no modificar existentes)
                    
                    **Restricciones:**
                    - Solo se pueden actualizar órdenes en estado BORRADOR
                    - Para modificar detalles existentes, eliminar y recrear la orden
                    """,
            tags = {"Órdenes de Compra"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden de compra actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrdenCompraResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "409", description = "Orden no está en estado BORRADOR"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> actualizar(
            @Parameter(description = "ID único de la orden de compra", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody OrdenCompraRequest request) {
        UUID userId = getCurrentUserId();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        OrdenCompra ordenCompra = ordenCompraRepository.findById(OrdenCompraId.from(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                    String.format("Orden de compra no encontrada: %s", id)
                ));

        if (!ordenCompra.puedeModificar()) {
            throw new IllegalStateException(
                String.format("Solo se pueden modificar órdenes en estado BORRADOR. Estado actual: %s", ordenCompra.getEstado())
            );
        }

        // Actualizar campos mutables
        if (request.condicionesPago() != null) {
            ordenCompra.actualizarCondicionesPago(request.condicionesPago(), userId, now);
        }
        if (request.observaciones() != null) {
            ordenCompra.actualizarObservaciones(request.observaciones(), userId, now);
        }

        // Agregar nuevos detalles si se proporcionan
        if (request.detalles() != null && !request.detalles().isEmpty()) {
            for (DetalleOrdenCompraRequest detalleRequest : request.detalles()) {
                DetalleOrdenCompra detalle = DetalleOrdenCompra.crear(
                    detalleRequest.partidaId(),
                    detalleRequest.descripcion(),
                    detalleRequest.cantidad(),
                    detalleRequest.unidad(),
                    detalleRequest.precioUnitario()
                );
                ordenCompra.agregarDetalle(detalle);
            }
        }

        ordenCompraRepository.save(ordenCompra);

        OrdenCompraResponse response = toResponse(ordenCompra);
        return ResponseEntity.ok(response);
    }

    /**
     * Solicita la aprobación de una orden de compra (BORRADOR → SOLICITADA).
     * 
     * @param id El ID de la orden de compra
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @Operation(
            summary = "Solicitar aprobación de orden de compra",
            description = """
                    Transiciona una orden de compra de BORRADOR a SOLICITADA.
                    
                    **Validaciones aplicadas:**
                    - L-04: El proveedor debe estar ACTIVO
                    - REGLA-153: Todas las partidas deben ser válidas (leaf nodes)
                    - L-01: El monto total no puede exceder el presupuesto disponible
                    
                    **Transición de estado:** BORRADOR → SOLICITADA
                    """,
            tags = {"Órdenes de Compra", "Transiciones de Estado"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Aprobación solicitada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere permiso CREATE_PO"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "409", description = "Orden no está en estado BORRADOR"),
            @ApiResponse(responseCode = "422", description = "Violación de regla de negocio (L-01, L-04, REGLA-153)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/{id}/solicitar")
    public ResponseEntity<Void> solicitar(
            @Parameter(description = "ID único de la orden de compra", required = true)
            @PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        
        solicitarAprobacionUseCase.solicitar(OrdenCompraId.from(id), userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aprueba una orden de compra (SOLICITADA → APROBADA).
     * 
     * @param id El ID de la orden de compra
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @Operation(
            summary = "Aprobar orden de compra",
            description = """
                    Aprueba una orden de compra, transicionándola de SOLICITADA a APROBADA.
                    
                    **Transición de estado:** SOLICITADA → APROBADA
                    
                    **Permisos requeridos:** APPROVE_PO
                    """,
            tags = {"Órdenes de Compra", "Transiciones de Estado"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orden aprobada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere permiso APPROVE_PO"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "409", description = "Orden no está en estado SOLICITADA"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobar(
            @Parameter(description = "ID único de la orden de compra", required = true)
            @PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        
        aprobarOrdenCompraUseCase.aprobar(OrdenCompraId.from(id), userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Envía una orden de compra al proveedor (APROBADA → ENVIADA).
     * 
     * @param id El ID de la orden de compra
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @Operation(
            summary = "Enviar orden de compra al proveedor",
            description = """
                    Envía una orden de compra al proveedor, transicionándola de APROBADA a ENVIADA.
                    
                    **Transición de estado:** APROBADA → ENVIADA
                    
                    **Evento publicado:** `OrdenCompraEnviadaEvent`
                    
                    **Permisos requeridos:** SEND_PO
                    """,
            tags = {"Órdenes de Compra", "Transiciones de Estado"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orden enviada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere permiso SEND_PO"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "409", description = "Orden no está en estado APROBADA"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/{id}/enviar")
    public ResponseEntity<Void> enviar(
            @Parameter(description = "ID único de la orden de compra", required = true)
            @PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        
        enviarOrdenCompraUseCase.enviar(OrdenCompraId.from(id), userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirma la recepción de una orden de compra (ENVIADA → RECIBIDA).
     * 
     * @param id El ID de la orden de compra
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @Operation(
            summary = "Confirmar recepción de orden de compra",
            description = """
                    Confirma la recepción de una orden de compra, transicionándola de ENVIADA a RECIBIDA.
                    
                    **Transición de estado:** ENVIADA → RECIBIDA
                    
                    **Acciones automáticas:**
                    - Actualiza el inventario para partidas de tipo Material (L-03)
                    - Publica el evento `OrdenCompraRecibidaEvent`
                    
                    **Permisos requeridos:** RECEIVE_PO
                    """,
            tags = {"Órdenes de Compra", "Transiciones de Estado"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recepción confirmada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere permiso RECEIVE_PO"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "409", description = "Orden no está en estado ENVIADA"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor - Error al actualizar inventario")
    })
    @PostMapping("/{id}/confirmar-recepcion")
    public ResponseEntity<Void> confirmarRecepcion(
            @Parameter(description = "ID único de la orden de compra", required = true)
            @PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        
        confirmarRecepcionUseCase.confirmarRecepcion(OrdenCompraId.from(id), userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina una orden de compra en estado BORRADOR.
     * 
     * @param id El ID de la orden de compra
     * @return ResponseEntity con código HTTP 204 NO CONTENT
     */
    @Operation(
            summary = "Eliminar orden de compra",
            description = """
                    Elimina una orden de compra en estado BORRADOR.
                    
                    **Restricciones:**
                    - Solo se pueden eliminar órdenes en estado BORRADOR
                    - Las órdenes en otros estados no pueden ser eliminadas
                    """,
            tags = {"Órdenes de Compra"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Orden eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden de compra no encontrada"),
            @ApiResponse(responseCode = "409", description = "Orden no está en estado BORRADOR"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID único de la orden de compra", required = true)
            @PathVariable UUID id) {
        OrdenCompra ordenCompra = ordenCompraRepository.findById(OrdenCompraId.from(id))
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                    String.format("Orden de compra no encontrada: %s", id)
                ));

        if (!ordenCompra.puedeEliminar()) {
            throw new IllegalStateException(
                String.format("Solo se pueden eliminar órdenes en estado BORRADOR. Estado actual: %s", ordenCompra.getEstado())
            );
        }

        ordenCompraRepository.delete(OrdenCompraId.from(id));
        return ResponseEntity.noContent().build();
    }

    /**
     * Convierte un OrdenCompra (dominio) a OrdenCompraResponse (DTO).
     */
    private OrdenCompraResponse toResponse(OrdenCompra ordenCompra) {
        // Cargar proveedor para la respuesta
        Proveedor proveedor = proveedorRepository.findById(ordenCompra.getProveedorId())
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Proveedor no encontrado: %s", ordenCompra.getProveedorId())
                ));

        ProveedorResponse proveedorResponse = new ProveedorResponse(
            proveedor.getId().getValue(),
            proveedor.getRazonSocial(),
            proveedor.getRuc(),
            proveedor.getEstado()
        );

        List<DetalleOrdenCompraResponse> detallesResponse = ordenCompra.getDetalles().stream()
                .map(detalle -> new DetalleOrdenCompraResponse(
                    detalle.getPartidaId(),
                    detalle.getDescripcion(),
                    detalle.getCantidad(),
                    detalle.getUnidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrdenCompraResponse(
            ordenCompra.getId().getValue(),
            ordenCompra.getNumero(),
            ordenCompra.getProyectoId(),
            proveedorResponse,
            ordenCompra.getFecha(),
            ordenCompra.getEstado(),
            ordenCompra.getMontoTotal(),
            ordenCompra.getCondicionesPago(),
            ordenCompra.getObservaciones(),
            ordenCompra.getVersion(),
            detallesResponse,
            ordenCompra.getCreatedAt(),
            ordenCompra.getUpdatedAt(),
            ordenCompra.getCreatedBy(),
            ordenCompra.getUpdatedBy()
        );
    }

    /**
     * Obtiene el ID del usuario actual del contexto de seguridad.
     * 
     * @return El UUID del usuario actual
     * @throws AuthenticationRequiredException si no se puede obtener el usuario del contexto
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationRequiredException(
                "Se requiere autenticación para realizar esta operación"
            );
        }

        // Intentar obtener el userId del principal
        // Asumiendo que el principal contiene el userId como String o UUID
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
