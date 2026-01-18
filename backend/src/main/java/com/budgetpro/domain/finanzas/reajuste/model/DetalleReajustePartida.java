package com.budgetpro.domain.finanzas.reajuste.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa el detalle de reajuste por partida.
 * 
 * Pertenece al agregado EstimacionReajuste.
 */
public final class DetalleReajustePartida {
    
    private final DetalleReajustePartidaId id;
    private final UUID partidaId;
    private final BigDecimal montoBase;
    private final BigDecimal montoReajustado;
    private final BigDecimal diferencial;
    
    /**
     * Constructor privado. Usar factory methods.
     */
    private DetalleReajustePartida(DetalleReajustePartidaId id, UUID partidaId,
                                   BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial) {
        this.id = Objects.requireNonNull(id, "El ID del detalle no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El ID de la partida no puede ser nulo");
        this.montoBase = Objects.requireNonNull(montoBase, "El monto base no puede ser nulo");
        this.montoReajustado = Objects.requireNonNull(montoReajustado, "El monto reajustado no puede ser nulo");
        this.diferencial = Objects.requireNonNull(diferencial, "El diferencial no puede ser nulo");
        
        if (montoBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto base debe ser mayor o igual a cero");
        }
        if (montoReajustado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto reajustado debe ser mayor o igual a cero");
        }
    }
    
    /**
     * Factory method para crear un nuevo detalle de reajuste.
     */
    public static DetalleReajustePartida crear(DetalleReajustePartidaId id, UUID partidaId,
                                              BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial) {
        return new DetalleReajustePartida(id, partidaId, montoBase, montoReajustado, diferencial);
    }
    
    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static DetalleReajustePartida reconstruir(DetalleReajustePartidaId id, UUID partidaId,
                                                     BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial) {
        return new DetalleReajustePartida(id, partidaId, montoBase, montoReajustado, diferencial);
    }
    
    // Getters
    
    public DetalleReajustePartidaId getId() { return id; }
    public UUID getPartidaId() { return partidaId; }
    public BigDecimal getMontoBase() { return montoBase; }
    public BigDecimal getMontoReajustado() { return montoReajustado; }
    public BigDecimal getDiferencial() { return diferencial; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleReajustePartida that = (DetalleReajustePartida) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
