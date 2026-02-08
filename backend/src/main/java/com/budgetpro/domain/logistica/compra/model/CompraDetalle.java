package com.budgetpro.domain.logistica.compra.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado Compra.
 * 
 * Representa un ítem comprado que está asociado a una partida específica.
 * 
 * Invariantes: - El recursoExternalId no puede ser nulo o vacío - El
 * recursoNombre no puede ser nulo o vacío - La clasificación es obligatoria -
 * La cantidad no puede ser negativa - El precioUnitario no puede ser negativo -
 * El subtotal = cantidad * precioUnitario
 */
public final class CompraDetalle {

    private final CompraDetalleId id;
    private final String recursoExternalId; // Referencia externa al recurso (ej. "MAT-001" de catálogo)
    private final String recursoNombre; // Snapshot del nombre del recurso para display/reporting
    private final String unidad; // Unidad en que llega la compra (Authority by PO). Para detección de cambio de
                                 // unidad vs catálogo.
    private final UUID partidaId; // Puede ser null si compra es no imputable
    private final NaturalezaGasto naturalezaGasto;
    private final RelacionContractual relacionContractual;
    private final RubroInsumo rubroInsumo;
    // JUSTIFICACIÓN ARQUITECTÓNICA: Entidad interna con campos editables.
    // Estos campos pueden modificarse durante la edición de la orden de compra:
    // - cantidad: puede ajustarse (actualizarCantidad)
    // - precioUnitario: puede negociarse (actualizarPrecioUnitario)
    // - subtotal: calculado dinámicamente = cantidad * precioUnitario
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal cantidad;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal precioUnitario;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal subtotal; // Calculado: cantidad * precioUnitario

    /**
     * Constructor privado. Usar factory methods.
     */
    private CompraDetalle(CompraDetalleId id, String recursoExternalId, String recursoNombre, String unidad,
            UUID partidaId, NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual,
            RubroInsumo rubroInsumo, BigDecimal cantidad, BigDecimal precioUnitario) {
        validarInvariantes(recursoExternalId, recursoNombre, unidad, naturalezaGasto, relacionContractual, rubroInsumo,
                cantidad, precioUnitario);

        this.id = Objects.requireNonNull(id, "El ID del detalle no puede ser nulo");
        this.recursoExternalId = Objects.requireNonNull(recursoExternalId, "El recursoExternalId no puede ser nulo")
                .trim();
        this.recursoNombre = Objects.requireNonNull(recursoNombre, "El recursoNombre no puede ser nulo").trim();
        this.unidad = unidad != null && !unidad.isBlank() ? unidad.trim() : null; // Opcional; si null, se usa unidad
                                                                                  // del catálogo
        this.partidaId = partidaId;
        this.naturalezaGasto = Objects.requireNonNull(naturalezaGasto, "La naturaleza del gasto es obligatoria");
        this.relacionContractual = Objects.requireNonNull(relacionContractual,
                "La relación contractual es obligatoria");
        this.rubroInsumo = Objects.requireNonNull(rubroInsumo, "El rubro es obligatorio");
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = calcularSubtotal();
    }

    /**
     * Factory method para crear un nuevo CompraDetalle.
     *
     * @param unidad Unidad en que llega la compra (Authority by PO). Si null, se
     *               usará la unidad del catálogo.
     */
    public static CompraDetalle crear(CompraDetalleId id, String recursoExternalId, String recursoNombre, String unidad,
            UUID partidaId, NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual,
            RubroInsumo rubroInsumo, BigDecimal cantidad, BigDecimal precioUnitario) {
        return new CompraDetalle(id, recursoExternalId, recursoNombre, unidad, partidaId, naturalezaGasto,
                relacionContractual, rubroInsumo, cantidad, precioUnitario);
    }

    /**
     * Factory method para reconstruir un CompraDetalle desde persistencia.
     */
    public static CompraDetalle reconstruir(CompraDetalleId id, String recursoExternalId, String recursoNombre,
            String unidad, UUID partidaId, NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual,
            RubroInsumo rubroInsumo, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        CompraDetalle detalle = new CompraDetalle(id, recursoExternalId, recursoNombre, unidad, partidaId,
                naturalezaGasto, relacionContractual, rubroInsumo, cantidad, precioUnitario);
        detalle.subtotal = subtotal != null ? subtotal : detalle.calcularSubtotal();
        return detalle;
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(String recursoExternalId, String recursoNombre, String unidad,
            NaturalezaGasto naturalezaGasto, RelacionContractual relacionContractual, RubroInsumo rubroInsumo,
            BigDecimal cantidad, BigDecimal precioUnitario) {
        if (recursoExternalId == null || recursoExternalId.isBlank()) {
            throw new IllegalArgumentException("El recursoExternalId no puede ser nulo o vacío");
        }
        if (recursoNombre == null || recursoNombre.isBlank()) {
            throw new IllegalArgumentException("El recursoNombre no puede ser nulo o vacío");
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

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public String getRecursoNombre() {
        return recursoNombre;
    }

    /**
     * Unidad en que llega la compra (Authority by PO). Si null, se usa la unidad
     * base del catálogo al resolver inventario.
     */
    public String getUnidad() {
        return unidad;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CompraDetalle that = (CompraDetalle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "CompraDetalle{id=%s, recursoExternalId='%s', recursoNombre='%s', unidad=%s, partidaId=%s, naturaleza=%s, relacion=%s, rubro=%s, cantidad=%s, precioUnitario=%s, subtotal=%s}",
                id, recursoExternalId, recursoNombre, unidad, partidaId, naturalezaGasto, relacionContractual,
                rubroInsumo, cantidad, precioUnitario, subtotal);
    }
}
