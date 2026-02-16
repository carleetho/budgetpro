package com.budgetpro.domain.logistica.compra.model;

import com.budgetpro.domain.logistica.compra.port.out.PartidaValidator;
import com.budgetpro.domain.logistica.compra.port.out.PresupuestoValidator;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado ORDEN_COMPRA.
 * 
 * Representa una orden de compra con máquina de estados completa.
 * 
 * Máquina de estados:
 * BORRADOR → SOLICITADA → APROBADA → ENVIADA → RECIBIDA
 * 
 * Invariantes:
 * - El proyectoId es obligatorio
 * - El proveedorId es obligatorio y debe estar ACTIVO (L-04)
 * - La fecha no puede ser nula
 * - El número de orden no puede estar vacío
 * - La lista de detalles no puede ser nula ni vacía
 * - El montoTotal = Σ subtotales de detalles
 * - Todas las partidas deben ser leaf nodes válidas (REGLA-153)
 * - El montoTotal no puede exceder el presupuesto disponible (L-01)
 * - Los campos de auditoría son obligatorios (REGLA-167)
 * 
 * Contexto: Logística & Compras
 */
public final class OrdenCompra {

    private final OrdenCompraId id;
    private final String numero;
    private final UUID proyectoId;
    private final ProveedorId proveedorId;
    private final LocalDate fecha;
    
    // JUSTIFICACIÓN ARQUITECTÓNICA: Aggregate Root con estado mutable.
    // Campos que representan el estado evolutivo de la orden:
    // - estado: workflow transitions (BORRADOR → SOLICITADA → APROBADA → ENVIADA → RECIBIDA)
    // - montoTotal: recalculado dinámicamente cuando se agregan/modifican detalles
    // - condicionesPago: información de pago actualizable
    // - observaciones: notas adicionales actualizables
    // - version: optimistic locking para concurrencia
    // - updatedBy, updatedAt: campos de auditoría actualizables
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private OrdenCompraEstado estado;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal montoTotal; // Calculado: Σ subtotales de detalles
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private String condicionesPago;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private String observaciones;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private Long version;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private UUID updatedBy;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private LocalDateTime updatedAt;

    // Lista de detalles (value objects internos del agregado)
    private final List<DetalleOrdenCompra> detalles;

    // Campos de auditoría inmutables (REGLA-167)
    private final UUID createdBy;
    private final LocalDateTime createdAt;

    /**
     * Constructor privado. Usar factory methods.
     */
    private OrdenCompra(OrdenCompraId id, String numero, UUID proyectoId, ProveedorId proveedorId,
                       LocalDate fecha, OrdenCompraEstado estado, BigDecimal montoTotal,
                       String condicionesPago, String observaciones, Long version,
                       UUID createdBy, LocalDateTime createdAt, UUID updatedBy, LocalDateTime updatedAt,
                       List<DetalleOrdenCompra> detalles) {
        validarInvariantes(proyectoId, proveedorId, fecha, numero, detalles, createdBy, createdAt);

        this.id = Objects.requireNonNull(id, "El ID de la orden de compra no puede ser nulo");
        this.numero = normalizarNumero(numero);
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.proveedorId = Objects.requireNonNull(proveedorId, "El proveedorId no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.estado = estado != null ? estado : OrdenCompraEstado.BORRADOR;
        this.version = version != null ? version : 0L;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.montoTotal = calcularMontoTotal();
        this.condicionesPago = condicionesPago != null && !condicionesPago.isBlank() ? condicionesPago.trim() : null;
        this.observaciones = observaciones != null && !observaciones.isBlank() ? observaciones.trim() : null;
        this.createdBy = Objects.requireNonNull(createdBy, "El createdBy no puede ser nulo (REGLA-167)");
        this.createdAt = Objects.requireNonNull(createdAt, "El createdAt no puede ser nulo (REGLA-167)");
        this.updatedBy = updatedBy != null ? updatedBy : createdBy;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
    }

    /**
     * Factory method para crear una nueva OrdenCompra en estado BORRADOR.
     */
    public static OrdenCompra crear(OrdenCompraId id, String numero, UUID proyectoId, ProveedorId proveedorId,
                                   LocalDate fecha, String condicionesPago, String observaciones,
                                   List<DetalleOrdenCompra> detalles,
                                   UUID createdBy, LocalDateTime createdAt) {
        return new OrdenCompra(id, numero, proyectoId, proveedorId, fecha, OrdenCompraEstado.BORRADOR, null,
                              condicionesPago, observaciones, 0L, createdBy, createdAt, null, null, detalles);
    }

    /**
     * Factory method para reconstruir una OrdenCompra desde persistencia.
     */
    public static OrdenCompra reconstruir(OrdenCompraId id, String numero, UUID proyectoId, ProveedorId proveedorId,
                                         LocalDate fecha, OrdenCompraEstado estado, BigDecimal montoTotal,
                                         String condicionesPago, String observaciones, Long version,
                                         UUID createdBy, LocalDateTime createdAt,
                                         UUID updatedBy, LocalDateTime updatedAt,
                                         List<DetalleOrdenCompra> detalles) {
        return new OrdenCompra(id, numero, proyectoId, proveedorId, fecha, estado, montoTotal,
                              condicionesPago, observaciones, version, createdBy, createdAt, updatedBy, updatedAt, detalles);
    }


    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, ProveedorId proveedorId, LocalDate fecha, String numero,
                                   List<DetalleOrdenCompra> detalles, UUID createdBy, LocalDateTime createdAt) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (proveedorId == null) {
            throw new IllegalArgumentException("El proveedorId no puede ser nulo");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de orden no puede estar vacío");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La orden de compra debe tener al menos un detalle");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo (REGLA-167)");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("El createdAt no puede ser nulo (REGLA-167)");
        }
    }

    /**
     * Normaliza el número de orden (trim).
     */
    private String normalizarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de orden no puede estar vacío");
        }
        return numero.trim();
    }

    /**
     * Calcula el monto total de la orden: Σ subtotales de detalles.
     */
    private BigDecimal calcularMontoTotal() {
        return detalles.stream()
                .map(DetalleOrdenCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Solicita la orden de compra (BORRADOR → SOLICITADA).
     * 
     * Valida:
     * - L-01: Presupuesto disponible
     * - L-04: Proveedor activo
     * - REGLA-153: Partidas leaf válidas
     * 
     * @param presupuestoValidator Validador de presupuesto
     * @param partidaValidator Validador de partidas
     * @param proveedorValidator Validador de proveedores
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     * @throws IllegalStateException si la transición no es válida o las validaciones fallan
     */
    public void solicitar(PresupuestoValidator presupuestoValidator,
                         PartidaValidator partidaValidator,
                         ProveedorValidator proveedorValidator,
                         UUID updatedBy, LocalDateTime updatedAt) {
        if (estado != OrdenCompraEstado.BORRADOR) {
            throw new IllegalStateException(
                String.format("Solo se puede solicitar una orden en estado BORRADOR. Estado actual: %s", estado)
            );
        }

        // Validar proveedor activo (L-04)
        if (proveedorValidator != null && !proveedorValidator.esProveedorActivo(proveedorId)) {
            throw new IllegalStateException("El proveedor debe estar ACTIVO para solicitar la orden (L-04)");
        }

        // Validar partidas leaf (REGLA-153)
        if (partidaValidator != null) {
            for (DetalleOrdenCompra detalle : detalles) {
                if (!partidaValidator.esPartidaLeafValida(detalle.getPartidaId())) {
                    throw new IllegalStateException(
                        String.format("La partida %s no es una partida leaf válida (REGLA-153)", detalle.getPartidaId())
                    );
                }
            }
        }

        // Validar presupuesto disponible (L-01)
        if (presupuestoValidator != null) {
            presupuestoValidator.validarDisponibilidadPresupuesto(proyectoId, montoTotal);
        }

        this.estado = OrdenCompraEstado.SOLICITADA;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Aprueba la orden de compra (SOLICITADA → APROBADA).
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     * @throws IllegalStateException si la transición no es válida
     */
    public void aprobar(UUID updatedBy, LocalDateTime updatedAt) {
        if (estado != OrdenCompraEstado.SOLICITADA) {
            throw new IllegalStateException(
                String.format("Solo se puede aprobar una orden en estado SOLICITADA. Estado actual: %s", estado)
            );
        }

        this.estado = OrdenCompraEstado.APROBADA;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Rechaza la orden de compra (SOLICITADA → BORRADOR).
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     * @throws IllegalStateException si la transición no es válida
     */
    public void rechazar(UUID updatedBy, LocalDateTime updatedAt) {
        if (estado != OrdenCompraEstado.SOLICITADA) {
            throw new IllegalStateException(
                String.format("Solo se puede rechazar una orden en estado SOLICITADA. Estado actual: %s", estado)
            );
        }

        this.estado = OrdenCompraEstado.BORRADOR;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Envía la orden de compra al proveedor (APROBADA → ENVIADA).
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     * @throws IllegalStateException si la transición no es válida
     */
    public void enviar(UUID updatedBy, LocalDateTime updatedAt) {
        if (estado != OrdenCompraEstado.APROBADA) {
            throw new IllegalStateException(
                String.format("Solo se puede enviar una orden en estado APROBADA. Estado actual: %s", estado)
            );
        }

        this.estado = OrdenCompraEstado.ENVIADA;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Confirma la recepción de la orden (ENVIADA → RECIBIDA).
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     * @throws IllegalStateException si la transición no es válida
     */
    public void confirmarRecepcion(UUID updatedBy, LocalDateTime updatedAt) {
        if (estado != OrdenCompraEstado.ENVIADA) {
            throw new IllegalStateException(
                String.format("Solo se puede confirmar recepción de una orden en estado ENVIADA. Estado actual: %s", estado)
            );
        }

        this.estado = OrdenCompraEstado.RECIBIDA;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Agrega un detalle a la orden.
     * Solo permitido si la orden está en estado BORRADOR.
     * 
     * @param detalle El detalle a agregar
     * @throws IllegalStateException si la orden no está en estado BORRADOR
     */
    public void agregarDetalle(DetalleOrdenCompra detalle) {
        if (!puedeModificar()) {
            throw new IllegalStateException("Solo se pueden agregar detalles a órdenes en estado BORRADOR");
        }
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        this.detalles.add(detalle);
        this.montoTotal = calcularMontoTotal();
    }

    /**
     * Actualiza las condiciones de pago.
     * Solo permitido si la orden está en estado BORRADOR.
     * 
     * @param nuevasCondicionesPago Nuevas condiciones de pago
     * @param updatedBy ID del usuario que realiza la actualización
     * @param updatedAt Fecha y hora de la actualización
     */
    public void actualizarCondicionesPago(String nuevasCondicionesPago, UUID updatedBy, LocalDateTime updatedAt) {
        if (!puedeModificar()) {
            throw new IllegalStateException("Solo se pueden modificar las condiciones de pago en estado BORRADOR");
        }
        this.condicionesPago = nuevasCondicionesPago != null && !nuevasCondicionesPago.isBlank() 
            ? nuevasCondicionesPago.trim() : null;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Actualiza las observaciones.
     * Solo permitido si la orden está en estado BORRADOR.
     * 
     * @param nuevasObservaciones Nuevas observaciones
     * @param updatedBy ID del usuario que realiza la actualización
     * @param updatedAt Fecha y hora de la actualización
     */
    public void actualizarObservaciones(String nuevasObservaciones, UUID updatedBy, LocalDateTime updatedAt) {
        if (!puedeModificar()) {
            throw new IllegalStateException("Solo se pueden modificar las observaciones en estado BORRADOR");
        }
        this.observaciones = nuevasObservaciones != null && !nuevasObservaciones.isBlank() 
            ? nuevasObservaciones.trim() : null;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Verifica si la orden puede ser modificada.
     * Solo las órdenes en estado BORRADOR pueden ser modificadas.
     * 
     * @return true si puede ser modificada, false en caso contrario
     */
    public boolean puedeModificar() {
        return estado == OrdenCompraEstado.BORRADOR;
    }

    /**
     * Verifica si la orden puede ser eliminada.
     * Solo las órdenes en estado BORRADOR pueden ser eliminadas.
     * 
     * @return true si puede ser eliminada, false en caso contrario
     */
    public boolean puedeEliminar() {
        return estado == OrdenCompraEstado.BORRADOR;
    }

    /**
     * Obtiene la lista de detalles (inmutable).
     */
    public List<DetalleOrdenCompra> getDetalles() {
        return List.copyOf(detalles);
    }

    // Getters

    public OrdenCompraId getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public ProveedorId getProveedorId() {
        return proveedorId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public OrdenCompraEstado getEstado() {
        return estado;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public String getCondicionesPago() {
        return condicionesPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenCompra that = (OrdenCompra) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("OrdenCompra{id=%s, numero='%s', proyectoId=%s, proveedorId=%s, fecha=%s, estado=%s, montoTotal=%s, detalles=%d}",
                id, numero, proyectoId, proveedorId, fecha, estado, montoTotal, detalles.size());
    }
}
