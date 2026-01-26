package com.budgetpro.domain.finanzas.reajuste.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Agregado que representa un índice de precios.
 * 
 * Catálogo mensual de índices de precios para reajustes de costos.
 */
public final class IndicePrecios {
    
    private final IndicePreciosId id;
    private final String codigo;
    private final String nombre;
    private final TipoIndicePrecios tipo;
    private final LocalDate fechaBase;
    private final BigDecimal valor;
    private boolean activo;
    
    /**
     * Constructor privado. Usar factory methods.
     */
    private IndicePrecios(IndicePreciosId id, String codigo, String nombre, TipoIndicePrecios tipo,
                         LocalDate fechaBase, BigDecimal valor, boolean activo) {
        this.id = Objects.requireNonNull(id, "El ID del índice no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código del índice no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre del índice no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "El tipo del índice no puede ser nulo");
        this.fechaBase = Objects.requireNonNull(fechaBase, "La fecha base no puede ser nula");
        this.valor = Objects.requireNonNull(valor, "El valor del índice no puede ser nulo");
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del índice debe ser mayor a cero");
        }
        this.activo = activo;
    }
    
    /**
     * Factory method para crear un nuevo índice de precios.
     */
    public static IndicePrecios crear(IndicePreciosId id, String codigo, String nombre, TipoIndicePrecios tipo,
                                    LocalDate fechaBase, BigDecimal valor) {
        return new IndicePrecios(id, codigo, nombre, tipo, fechaBase, valor, true);
    }
    
    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static IndicePrecios reconstruir(IndicePreciosId id, String codigo, String nombre, TipoIndicePrecios tipo,
                                           LocalDate fechaBase, BigDecimal valor, boolean activo) {
        return new IndicePrecios(id, codigo, nombre, tipo, fechaBase, valor, activo);
    }
    
    /**
     * Desactiva el índice.
     */
    public void desactivar() {
        this.activo = false;
    }
    
    /**
     * Activa el índice.
     */
    public void activar() {
        this.activo = true;
    }
    
    // Getters
    
    public IndicePreciosId getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public TipoIndicePrecios getTipo() { return tipo; }
    public LocalDate getFechaBase() { return fechaBase; }
    public BigDecimal getValor() { return valor; }
    public boolean isActivo() { return activo; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndicePrecios that = (IndicePrecios) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
