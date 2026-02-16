package com.budgetpro.domain.logistica.compra.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Evento de dominio publicado cuando una orden de compra es recibida (mercancía confirmada).
 * 
 * Se publica cuando la orden transiciona de ENVIADA a RECIBIDA.
 */
public class OrdenCompraRecibidaEvent extends ApplicationEvent {

    private final UUID ordenId;
    private final String numero;
    private final UUID proyectoId;
    private final UUID proveedorId;
    private final BigDecimal montoTotal;
    private final List<DetalleEvento> detalles;
    private final UUID userId;
    private final String correlationId;

    public OrdenCompraRecibidaEvent(Object source, UUID ordenId, String numero, UUID proyectoId, UUID proveedorId,
                                    BigDecimal montoTotal, List<DetalleEvento> detalles, UUID userId, String correlationId) {
        super(source);
        this.ordenId = ordenId;
        this.numero = numero;
        this.proyectoId = proyectoId;
        this.proveedorId = proveedorId;
        this.montoTotal = montoTotal;
        this.detalles = detalles != null ? List.copyOf(detalles) : List.of();
        this.userId = userId;
        this.correlationId = correlationId;
    }

    public UUID getOrdenId() {
        return ordenId;
    }

    public String getNumero() {
        return numero;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public UUID getProveedorId() {
        return proveedorId;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public List<DetalleEvento> getDetalles() {
        return detalles;
    }

    /**
     * Obtiene el timestamp del evento usando el método heredado de ApplicationEvent.
     */
    public LocalDateTime getEventTimestamp() {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(getTimestamp()),
            java.time.ZoneId.systemDefault()
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Value object que representa un detalle en el evento.
     */
    public record DetalleEvento(
            UUID partidaId,
            String descripcion,
            BigDecimal cantidad,
            String unidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        /**
         * Valida que el detalle tenga datos válidos.
         */
        public DetalleEvento {
            if (partidaId == null) {
                throw new IllegalArgumentException("partidaId no puede ser null");
            }
            if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("cantidad debe ser mayor que cero");
            }
            if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("precioUnitario no puede ser negativo");
            }
        }
    }
}
