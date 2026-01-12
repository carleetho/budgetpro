package com.budgetpro.domain.finanzas.compra;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.recurso.model.RecursoId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado COMPRA.
 * 
 * Representa una compra con su colección de detalles.
 * 
 * Invariantes Clave:
 * 1. No permitir compra sin detalles
 * 2. No permitir cantidad <= 0 (validado por Cantidad)
 * 3. No permitir precio unitario <= 0 (validado por PrecioUnitario)
 * 4. Total derivado (no seteable desde fuera)
 * 5. Estado debe pertenecer al set permitido del ciclo de vida
 * 6. El destino (proyectoId/presupuestoId) es obligatorio
 * 
 * Contexto: Logística & Costos
 * 
 * REGLA DE NEGOCIO: La compra no genera costo por sí sola. El costo nace en ConsumoPartida.
 */
public final class Compra {

    private final CompraId id;
    private final UUID proyectoId;
    private final UUID presupuestoId;
    private EstadoCompra estado;
    private Long version;
    
    // Colección de DetalleCompra como entidad interna del agregado
    private final List<DetalleCompra> detalles;
    
    // Total derivado (calculado desde los detalles)
    private TotalCompra total;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Compra(CompraId id, UUID proyectoId, UUID presupuestoId, List<DetalleCompra> detalles,
                   EstadoCompra estado, Long version, boolean validar) {
        this.id = Objects.requireNonNull(id, "El ID de la compra no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.estado = estado != null ? estado : EstadoCompra.PENDIENTE;
        this.version = version != null ? version : 0L;

        // Validar invariantes solo si es creación nueva (no reconstrucción desde BD)
        if (validar) {
            validarInvariantes();
        }

        // Calcular total derivado
        calcularTotal();
    }

    /**
     * Factory method para crear una nueva Compra con estado PENDIENTE por defecto.
     */
    public static Compra crear(UUID proyectoId, UUID presupuestoId, List<DetalleCompra> detalles) {
        return new Compra(
            CompraId.generate(),
            proyectoId,
            presupuestoId,
            detalles,
            EstadoCompra.PENDIENTE,
            0L,
            true // Validar invariantes en creación
        );
    }

    /**
     * Factory method para crear una Compra con ID específico.
     */
    public static Compra crear(CompraId id, UUID proyectoId, UUID presupuestoId, List<DetalleCompra> detalles) {
        return new Compra(
            id,
            proyectoId,
            presupuestoId,
            detalles,
            EstadoCompra.REGISTRADA,
            0L,
            true // Validar invariantes en creación
        );
    }

    /**
     * Factory method para reconstruir una Compra desde persistencia.
     * NO valida invariantes de creación (permite estados que podrían ser inválidos al crear).
     */
    public static Compra reconstruir(CompraId id, UUID proyectoId, UUID presupuestoId,
                                     List<DetalleCompra> detalles, EstadoCompra estado, Long version,
                                     TotalCompra total) {
        Compra compra = new Compra(
            id,
            proyectoId,
            presupuestoId,
            detalles,
            estado,
            version,
            false // NO validar invariantes en reconstrucción
        );
        // Asignar total reconstruido (no recalcular)
        compra.total = Objects.requireNonNull(total, "El total no puede ser nulo en reconstrucción");
        return compra;
    }

    /**
     * Valida las invariantes del agregado antes de crear o modificar.
     */
    private void validarInvariantes() {
        // INVARIANTE: No permitir compra sin detalles
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalStateException("La compra debe tener al menos un detalle");
        }

        // INVARIANTE: Estado debe pertenecer al set permitido
        if (estado == null) {
            throw new IllegalStateException("El estado de la compra no puede ser nulo");
        }
    }

    /**
     * Calcula el total de la compra sumando los subtotales de todos los detalles.
     */
    private void calcularTotal() {
        if (detalles == null || detalles.isEmpty()) {
            this.total = TotalCompra.cero();
            return;
        }

        TotalCompra suma = TotalCompra.cero();
        for (DetalleCompra detalle : detalles) {
            suma = suma.sumar(detalle.calcularSubtotal());
        }
        this.total = suma;
    }

    // Getters

    public CompraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Obtiene todos los detalles de la compra (lista inmutable).
     */
    public List<DetalleCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    /**
     * Obtiene el total de la compra (derivado).
     */
    public TotalCompra getTotal() {
        return total;
    }

    /**
     * Obtiene el total como Monto.
     */
    public Monto getTotalAsMonto() {
        return total.getValue();
    }

    /**
     * Confirma la compra (transición PENDIENTE -> CONFIRMADA).
     * 
     * Solo puede confirmarse una compra que esté en estado PENDIENTE.
     * 
     * @throws IllegalStateException si la compra no está en estado PENDIENTE
     */
    public void confirmar() {
        if (this.estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                String.format("No se puede confirmar una compra que no está en estado PENDIENTE. Estado actual: %s", 
                    this.estado)
            );
        }
        this.estado = EstadoCompra.CONFIRMADA;
    }

    /**
     * Marca la compra como con error (transición PENDIENTE -> ERROR).
     * 
     * Solo puede marcarse como error una compra que esté en estado PENDIENTE.
     * 
     * @throws IllegalStateException si la compra no está en estado PENDIENTE
     */
    public void marcarError() {
        if (this.estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                String.format("No se puede marcar como error una compra que no está en estado PENDIENTE. Estado actual: %s", 
                    this.estado)
            );
        }
        this.estado = EstadoCompra.ERROR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compra compra = (Compra) o;
        return Objects.equals(id, compra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", proyectoId=" + proyectoId +
                ", presupuestoId=" + presupuestoId +
                ", estado=" + estado +
                ", total=" + total +
                ", detalles=" + detalles.size() + " item(s)" +
                '}';
    }
}
