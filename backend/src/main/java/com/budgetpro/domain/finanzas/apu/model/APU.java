package com.budgetpro.domain.finanzas.apu.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado APU (Análisis de Precios Unitarios).
 * 
 * Representa el análisis de costos de una partida, compuesto por insumos
 * (recursos). Relación 1:1 con Partida (Una partida tiene un APU).
 * 
 * Invariantes: - El partidaId es obligatorio - La lista de insumos no puede ser
 * nula (puede estar vacía) - El costo total = Σ subtotales de insumos
 */
public final class APU {

    private final ApuId id;
    private final UUID partidaId;
    private final BigDecimal rendimiento; // Opcional, cantidad de unidades que se pueden producir por día
    private final String unidad; // Copia de la unidad de la partida
    private final Long version;
    private final List<ApuInsumo> insumos;

    /**
     * Constructor privado. Usar factory methods.
     */
    private APU(ApuId id, UUID partidaId, BigDecimal rendimiento, String unidad, Long version,
            List<ApuInsumo> insumos) {
        validarInvariantes(partidaId, insumos);

        this.id = Objects.requireNonNull(id, "El ID del APU no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo");
        this.rendimiento = rendimiento;
        this.unidad = unidad != null ? unidad.trim() : null;
        this.version = version != null ? version : 0L;
        this.insumos = Collections.unmodifiableList(new ArrayList<>(insumos));
    }

    /**
     * Factory method para crear un nuevo APU vacío (sin insumos).
     */
    public static APU crear(ApuId id, UUID partidaId, String unidad) {
        return new APU(id, partidaId, null, unidad, 0L, new ArrayList<>());
    }

    /**
     * Factory method para crear un nuevo APU con rendimiento.
     */
    public static APU crear(ApuId id, UUID partidaId, BigDecimal rendimiento, String unidad) {
        return new APU(id, partidaId, rendimiento, unidad, 0L, new ArrayList<>());
    }

    /**
     * Factory method para reconstruir un APU desde persistencia.
     */
    public static APU reconstruir(ApuId id, UUID partidaId, BigDecimal rendimiento, String unidad, Long version,
            List<ApuInsumo> insumos) {
        return new APU(id, partidaId, rendimiento, unidad, version, insumos);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID partidaId, List<ApuInsumo> insumos) {
        if (partidaId == null) {
            throw new IllegalArgumentException("El partidaId no puede ser nulo");
        }
        if (insumos == null) {
            // REGLA-035
            throw new IllegalArgumentException("La lista de insumos no puede ser nula");
        }
    }

    /**
     * Agrega un insumo al APU.
     * 
     * @return Nuevo APU con el insumo agregado.
     */
    public APU agregarInsumo(UUID recursoId, BigDecimal cantidad, BigDecimal precioUnitario) {
        ApuInsumoId insumoId = ApuInsumoId.nuevo();
        ApuInsumo insumo = ApuInsumo.crear(insumoId, recursoId, cantidad, precioUnitario);

        List<ApuInsumo> nuevosInsumos = new ArrayList<>(this.insumos);
        nuevosInsumos.add(insumo);

        return new APU(this.id, this.partidaId, this.rendimiento, this.unidad, this.version, nuevosInsumos);
    }

    /**
     * Calcula el costo total del APU: Σ subtotales de insumos.
     */
    public BigDecimal calcularCostoTotal() {
        return insumos.stream().map(ApuInsumo::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Actualiza el rendimiento del APU.
     * 
     * @return Nuevo APU con el rendimiento actualizado.
     */
    public APU actualizarRendimiento(BigDecimal nuevoRendimiento) {
        return new APU(this.id, this.partidaId, nuevoRendimiento, this.unidad, this.version, this.insumos);
    }

    /**
     * Actualiza la unidad del APU.
     * 
     * @return Nuevo APU con la unidad actualizada.
     */
    public APU actualizarUnidad(String nuevaUnidad) {
        return new APU(this.id, this.partidaId, this.rendimiento, nuevaUnidad, this.version, this.insumos);
    }

    /**
     * Obtiene la lista de insumos (inmutable).
     */
    public List<ApuInsumo> getInsumos() {
        return insumos;
    }

    // Getters

    public ApuId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public BigDecimal getRendimiento() {
        return rendimiento;
    }

    public String getUnidad() {
        return unidad;
    }

    public Long getVersion() {
        return version;
    }

    public boolean tieneInsumos() {
        return !insumos.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        APU apu = (APU) o;
        return Objects.equals(id, apu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("APU{id=%s, partidaId=%s, rendimiento=%s, unidad='%s', costoTotal=%s, insumos=%d}", id,
                partidaId, rendimiento, unidad, calcularCostoTotal(), insumos.size());
    }
}
