package com.budgetpro.domain.logistica.compra.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado Compra.
 * 
 * Representa un ítem comprado que está asociado a una partida específica.
 * 
 * Invariantes:
 * - El recursoId no puede ser nulo
 * - La clasificación es obligatoria
 * - La cantidad no puede ser negativa
 * - El precioUnitario no puede ser negativo
 * - El subtotal = cantidad * precioUnitario
 */
public final class CompraDetalle {

    private final CompraDetalleId id;
    private final UUID recursoId;
    private final UUID partidaId; // Puede ser null si compra es no imputable
    private final NaturalezaGasto naturalezaGasto;
    private final RelacionContractual relacionContractual;
    private final RubroInsumo rubroInsumo;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal; // Calculado: cantidad * precioUnitario

    /**
     * Constructor privado. Usar factory methods.
     */
    private CompraDetalle(CompraDetalleId id, UUID recursoId, UUID partidaId,
                          NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual,
                          RubroInsumo rubroInsumo, BigDecimal cantidad, BigDecimal precioUnitario) {
        validarInvariantes(recursoId, naturalezaGasto, relacionContractual, rubroInsumo, cantidad, precioUnitario);
        
        this.id = Objects.requireNonNull(id, "El ID del detalle no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId no puede ser nulo");
        this.partidaId = partidaId;
        this.naturalezaGasto = Objects.requireNonNull(naturalezaGasto, "La naturaleza del gasto es obligatoria");
        this.relacionContractual = Objects.requireNonNull(relacionContractual, "La relación contractual es obligatoria");
        this.rubroInsumo = Objects.requireNonNull(rubroInsumo, "El rubro es obligatorio");
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = calcularSubtotal();
    }

    /**
     * Factory method para crear un nuevo CompraDetalle.
     */
    public static CompraDetalle crear(CompraDetalleId id, UUID recursoId, UUID partidaId,
                                      NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual,
                                      RubroInsumo rubroInsumo, BigDecimal cantidad, BigDecimal precioUnitario) {
        return new CompraDetalle(id, recursoId, partidaId, naturalezaGasto, relacionContractual, rubroInsumo,
                cantidad, precioUnitario);
    }

    /**
     * Factory method para reconstruir un CompraDetalle desde persistencia.
     */
    public static CompraDetalle reconstruir(CompraDetalleId id, UUID recursoId, UUID partidaId,
                                            NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual,
                                            RubroInsumo rubroInsumo, BigDecimal cantidad, BigDecimal precioUnitario,
                                            BigDecimal subtotal) {
        CompraDetalle detalle = new CompraDetalle(id, recursoId, partidaId, naturalezaGasto, relacionContractual,
                rubroInsumo, cantidad, precioUnitario);
        detalle.subtotal = subtotal != null ? subtotal : detalle.calcularSubtotal();
        return detalle;
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID recursoId, NaturalezaGasto naturalezaGasto,
                                    RelacionContractual relacionContractual, RubroInsumo rubroInsumo,
                                    BigDecimal cantidad, BigDecimal precioUnitario) {
        if (recursoId == null) {
            throw new IllegalArgumentException("El recursoId no puede ser nulo");
        }
        Objects.requireNonNull(naturalezaGasto, "La naturaleza del gasto es obligatoria");
        Objects.requireNonNull(relacionContractual, "La relación contractual es obligatoria");
        Objects.requireNonNull(rubroInsumo, "El rubro es obligatorio");
        if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
    }

    /**
     * Calcula el subtotal: cantidad * precioUnitario.
     */
    private BigDecimal calcularSubtotal() {
        return this.cantidad.multiply(this.precioUnitario);
    }

    /**
     * Actualiza la cantidad y recalcula el subtotal.
     */
    public void actualizarCantidad(BigDecimal nuevaCantidad) {
        if (nuevaCantidad == null) {
            this.cantidad = BigDecimal.ZERO;
        } else if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        } else {
            this.cantidad = nuevaCantidad;
        }
        this.subtotal = calcularSubtotal();
    }

    /**
     * Actualiza el precio unitario y recalcula el subtotal.
     */
    public void actualizarPrecioUnitario(BigDecimal nuevoPrecioUnitario) {
        if (nuevoPrecioUnitario == null) {
            this.precioUnitario = BigDecimal.ZERO;
        } else if (nuevoPrecioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        } else {
            this.precioUnitario = nuevoPrecioUnitario;
        }
        this.subtotal = calcularSubtotal();
    }

    // Getters

    public CompraDetalleId getId() {
        return id;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public NaturalezaGasto getNaturalezaGasto() {
        return naturalezaGasto;
    }

    public RelacionContractual getRelacionContractual() {
        return relacionContractual;
    }

    public RubroInsumo getRubroInsumo() {
        return rubroInsumo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompraDetalle that = (CompraDetalle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("CompraDetalle{id=%s, recursoId=%s, partidaId=%s, naturaleza=%s, relacion=%s, rubro=%s, cantidad=%s, precioUnitario=%s, subtotal=%s}",
                id, recursoId, partidaId, naturalezaGasto, relacionContractual, rubroInsumo, cantidad, precioUnitario, subtotal);
    }
}
