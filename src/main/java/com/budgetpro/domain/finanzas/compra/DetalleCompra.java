package com.budgetpro.domain.finanzas.compra;

import com.budgetpro.domain.recurso.model.RecursoId;

import java.util.Objects;

/**
 * Entidad interna del agregado COMPRA.
 * 
 * Representa un detalle de compra con referencia a un recurso, cantidad y precio unitario.
 * 
 * REGLA DE DDD: Esta entidad NO es un agregado raíz. Pertenece al agregado Compra y
 * se accede exclusivamente a través del agregado raíz (Compra).
 * 
 * Invariantes Críticas:
 * 1. El recursoId no puede ser nulo (cada detalle referencia a CatalogoRecurso)
 * 2. La cantidad debe ser mayor que cero (validado por el Value Object Cantidad)
 * 3. El precio unitario debe ser mayor que cero (validado por el Value Object PrecioUnitario)
 * 4. El subtotal es derivado (cantidad * precio unitario)
 * 
 * Contexto: Logística & Costos
 */
public final class DetalleCompra {

    private final RecursoId recursoId;
    private final Cantidad cantidad;
    private final PrecioUnitario precioUnitario;

    /**
     * Constructor privado. Usar factory methods.
     */
    private DetalleCompra(RecursoId recursoId, Cantidad cantidad, PrecioUnitario precioUnitario) {
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId del detalle no puede ser nulo");
        this.cantidad = Objects.requireNonNull(cantidad, "La cantidad del detalle no puede ser nula");
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "El precio unitario del detalle no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo DetalleCompra.
     */
    public static DetalleCompra crear(RecursoId recursoId, Cantidad cantidad, PrecioUnitario precioUnitario) {
        return new DetalleCompra(recursoId, cantidad, precioUnitario);
    }

    /**
     * Calcula el subtotal de este detalle (cantidad * precio unitario).
     * 
     * @return TotalCompra con el subtotal calculado
     */
    public TotalCompra calcularSubtotal() {
        return cantidad.multiplicar(precioUnitario);
    }

    // Getters

    public RecursoId getRecursoId() {
        return recursoId;
    }

    public Cantidad getCantidad() {
        return cantidad;
    }

    public PrecioUnitario getPrecioUnitario() {
        return precioUnitario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleCompra that = (DetalleCompra) o;
        return Objects.equals(recursoId, that.recursoId) &&
               Objects.equals(cantidad, that.cantidad) &&
               Objects.equals(precioUnitario, that.precioUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recursoId, cantidad, precioUnitario);
    }

    @Override
    public String toString() {
        return "DetalleCompra{" +
                "recursoId=" + recursoId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                '}';
    }
}
