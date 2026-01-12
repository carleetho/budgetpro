package com.budgetpro.domain.logistica.inventario;

import com.budgetpro.domain.finanzas.compra.Cantidad;
import com.budgetpro.domain.recurso.model.RecursoId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Aggregate Root del agregado INVENTARIO.
 * 
 * Representa un ítem de inventario con stock de un recurso específico.
 * 
 * Invariantes Clave:
 * 1. Stock nunca negativo (validado en registrarEgreso)
 * 2. El stock solo se modifica mediante comportamiento del agregado (registrarIngreso, registrarEgreso)
 * 3. El inventario no conoce proyectos ni partidas (separación estricta)
 * 
 * Contexto: Logística & Costos
 * 
 * REGLA DE NEGOCIO: El inventario es un "Proyecto Pasivo del Sistema" que no tiene
 * conocimiento financiero. Solo gestiona stock físico.
 */
public final class InventarioItem {

    private final InventarioId id;
    private final RecursoId recursoId;
    private BigDecimal stock;
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private InventarioItem(InventarioId id, RecursoId recursoId, BigDecimal stock, Long version, boolean validar) {
        this.id = Objects.requireNonNull(id, "El ID del inventario no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId no puede ser nulo");
        this.stock = stock != null ? stock : BigDecimal.ZERO;
        this.version = version != null ? version : 0L;

        if (validar) {
            validarInvariantes();
        }
    }

    /**
     * Factory method para crear un nuevo InventarioItem con stock inicial en ZERO.
     */
    public static InventarioItem crear(InventarioId id, RecursoId recursoId) {
        return new InventarioItem(id, recursoId, BigDecimal.ZERO, 0L, true);
    }

    /**
     * Factory method para crear un InventarioItem con stock inicial específico.
     */
    public static InventarioItem crear(InventarioId id, RecursoId recursoId, BigDecimal stockInicial) {
        InventarioItem item = new InventarioItem(id, recursoId, stockInicial, 0L, true);
        // Validar que el stock inicial no sea negativo
        if (stockInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        return item;
    }

    /**
     * Factory method para reconstruir un InventarioItem desde persistencia.
     * NO valida invariantes de creación (permite estados que podrían ser inválidos al crear).
     */
    public static InventarioItem reconstruir(InventarioId id, RecursoId recursoId, BigDecimal stock, Long version) {
        return new InventarioItem(id, recursoId, stock, version, false);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes() {
        // INVARIANTE: Stock nunca negativo (validado en registrarEgreso, pero también aquí para reconstrucción)
        if (stock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El stock no puede ser negativo");
        }
    }

    /**
     * Registra un ingreso de stock al inventario.
     * 
     * @param cantidad La cantidad a ingresar (debe ser positiva)
     * @throws IllegalArgumentException si la cantidad no es positiva
     */
    public void registrarIngreso(Cantidad cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a ingresar no puede ser nula");
        
        BigDecimal cantidadValue = cantidad.getValue();
        if (cantidadValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a ingresar debe ser positiva");
        }

        this.stock = this.stock.add(cantidadValue);
        this.version = this.version + 1;
    }

    /**
     * Registra un ingreso de stock al inventario usando BigDecimal.
     * 
     * @param cantidad La cantidad a ingresar (debe ser positiva)
     * @throws IllegalArgumentException si la cantidad no es positiva
     */
    public void registrarIngreso(BigDecimal cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a ingresar no puede ser nula");
        
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a ingresar debe ser positiva");
        }

        this.stock = this.stock.add(cantidad);
        this.version = this.version + 1;
    }

    /**
     * Registra un egreso de stock del inventario.
     * 
     * INVARIANTE: Si el stock resultante sería negativo, lanza IllegalStateException.
     * 
     * @param cantidad La cantidad a egresar (debe ser positiva)
     * @throws IllegalArgumentException si la cantidad no es positiva
     * @throws IllegalStateException si el stock resultante sería negativo
     */
    public void registrarEgreso(Cantidad cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a egresar no puede ser nula");
        
        BigDecimal cantidadValue = cantidad.getValue();
        if (cantidadValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a egresar debe ser positiva");
        }

        // INVARIANTE: Validar que el stock no quede negativo
        BigDecimal stockResultante = this.stock.subtract(cantidadValue);
        if (stockResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                String.format("Stock insuficiente. Stock actual: %s, cantidad solicitada: %s", stock, cantidadValue)
            );
        }

        this.stock = stockResultante;
        this.version = this.version + 1;
    }

    /**
     * Registra un egreso de stock del inventario usando BigDecimal.
     * 
     * INVARIANTE: Si el stock resultante sería negativo, lanza IllegalStateException.
     * 
     * @param cantidad La cantidad a egresar (debe ser positiva)
     * @throws IllegalArgumentException si la cantidad no es positiva
     * @throws IllegalStateException si el stock resultante sería negativo
     */
    public void registrarEgreso(BigDecimal cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a egresar no puede ser nula");
        
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a egresar debe ser positiva");
        }

        // INVARIANTE: Validar que el stock no quede negativo
        BigDecimal stockResultante = this.stock.subtract(cantidad);
        if (stockResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                String.format("Stock insuficiente. Stock actual: %s, cantidad solicitada: %s", stock, cantidad)
            );
        }

        this.stock = stockResultante;
        this.version = this.version + 1;
    }

    /**
     * Valida que haya disponibilidad suficiente de stock.
     * 
     * @param cantidad La cantidad a validar
     * @return true si hay stock suficiente, false en caso contrario
     */
    public boolean validarDisponibilidad(Cantidad cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a validar no puede ser nula");
        return this.stock.compareTo(cantidad.getValue()) >= 0;
    }

    /**
     * Valida que haya disponibilidad suficiente de stock usando BigDecimal.
     * 
     * @param cantidad La cantidad a validar
     * @return true si hay stock suficiente, false en caso contrario
     */
    public boolean validarDisponibilidad(BigDecimal cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a validar no puede ser nula");
        return this.stock.compareTo(cantidad) >= 0;
    }

    // Getters

    public InventarioId getId() {
        return id;
    }

    public RecursoId getRecursoId() {
        return recursoId;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioItem that = (InventarioItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InventarioItem{" +
                "id=" + id +
                ", recursoId=" + recursoId +
                ", stock=" + stock +
                '}';
    }
}
