package com.budgetpro.domain.catalogo.model;

import com.budgetpro.domain.catalogo.service.CalculoApuDinamicoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.budgetpro.domain.shared.model.Immutable;

/**
 * Aggregate Root del agregado APUSnapshot.
 *
 * Representa un snapshot de APU con rendimiento editable (Opción C),
 * preservando el rendimiento original del catálogo para auditoría.
 *
 * Invariantes: - partidaId no puede ser nulo - externalApuId y catalogSource no
 * pueden estar vacíos - rendimientoOriginal y rendimientoVigente deben ser
 * positivos - unidadSnapshot no puede estar vacía - snapshotDate no puede ser
 * nula
 */
@Immutable
public final class APUSnapshot {

    private final APUSnapshotId id;
    private final UUID partidaId;
    private final String externalApuId;
    private final String catalogSource;
    private final BigDecimal rendimientoOriginal;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.catalogo
    private final BigDecimal rendimientoVigente; // Immutable
    private final boolean rendimientoModificado; // Audit trail field
    private final UUID rendimientoModificadoPor; // Audit trail field
    private final LocalDateTime rendimientoModificadoEn; // Audit trail field
    private final String unidadSnapshot;
    private final LocalDateTime snapshotDate;
    private final List<APUInsumoSnapshot> insumos;
    private final Long version;

    private APUSnapshot(APUSnapshotId id, UUID partidaId, String externalApuId, String catalogSource,
            BigDecimal rendimientoOriginal, BigDecimal rendimientoVigente, boolean rendimientoModificado,
            UUID rendimientoModificadoPor, LocalDateTime rendimientoModificadoEn, String unidadSnapshot,
            LocalDateTime snapshotDate, List<APUInsumoSnapshot> insumos, Long version) {
        validarInvariantes(partidaId, externalApuId, catalogSource, rendimientoOriginal, rendimientoVigente,
                unidadSnapshot, snapshotDate);

        this.id = Objects.requireNonNull(id, "El ID del APUSnapshot no puede ser nulo");
        this.partidaId = partidaId;
        this.externalApuId = externalApuId.trim();
        this.catalogSource = catalogSource.trim();
        this.rendimientoOriginal = rendimientoOriginal;
        this.rendimientoVigente = rendimientoVigente;
        this.rendimientoModificado = rendimientoModificado;
        this.rendimientoModificadoPor = rendimientoModificadoPor;
        this.rendimientoModificadoEn = rendimientoModificadoEn;
        this.unidadSnapshot = unidadSnapshot.trim();
        this.snapshotDate = snapshotDate;
        this.insumos = insumos != null ? new ArrayList<>(insumos) : new ArrayList<>();
        this.version = version != null ? version : 0L;
    }

    public static APUSnapshot crear(APUSnapshotId id, UUID partidaId, String externalApuId, String catalogSource,
            BigDecimal rendimiento, String unidadSnapshot, LocalDateTime snapshotDate) {
        return new APUSnapshot(id, partidaId, externalApuId, catalogSource, rendimiento, rendimiento, false, null, null,
                unidadSnapshot, snapshotDate, new ArrayList<>(), 0L);
    }

    public static APUSnapshot reconstruir(APUSnapshotId id, UUID partidaId, String externalApuId, String catalogSource,
            BigDecimal rendimientoOriginal, BigDecimal rendimientoVigente, boolean rendimientoModificado,
            UUID rendimientoModificadoPor, LocalDateTime rendimientoModificadoEn, String unidadSnapshot,
            LocalDateTime snapshotDate, List<APUInsumoSnapshot> insumos, Long version) {
        return new APUSnapshot(id, partidaId, externalApuId, catalogSource, rendimientoOriginal, rendimientoVigente,
                rendimientoModificado, rendimientoModificadoPor, rendimientoModificadoEn, unidadSnapshot, snapshotDate,
                insumos, version);
    }

    private void validarInvariantes(UUID partidaId, String externalApuId, String catalogSource,
            BigDecimal rendimientoOriginal, BigDecimal rendimientoVigente, String unidadSnapshot,
            LocalDateTime snapshotDate) {
        if (partidaId == null) {
            throw new IllegalArgumentException("El partidaId no puede ser nulo");
        }
        if (externalApuId == null || externalApuId.isBlank()) {
            throw new IllegalArgumentException("El externalApuId no puede estar vacío");
        }
        if (catalogSource == null || catalogSource.isBlank()) {
            throw new IllegalArgumentException("El catalogSource no puede estar vacío");
        }
        if (rendimientoOriginal == null || rendimientoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El rendimientoOriginal debe ser positivo");
        }
        if (rendimientoVigente == null || rendimientoVigente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El rendimientoVigente debe ser positivo");
        }
        if (unidadSnapshot == null || unidadSnapshot.isBlank()) {
            throw new IllegalArgumentException("La unidadSnapshot no puede estar vacía");
        }
        if (snapshotDate == null) {
            throw new IllegalArgumentException("La snapshotDate no puede ser nula");
        }
    }

    /**
     * Actualiza el rendimiento vigente (Opción C) y registra auditoría.
     * 
     * @return Una nueva instancia con el rendimiento actualizado
     */
    public APUSnapshot actualizarRendimiento(BigDecimal nuevoRendimiento, UUID usuarioId) {
        if (nuevoRendimiento == null || nuevoRendimiento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El rendimiento debe ser positivo");
        }
        if (nuevoRendimiento.compareTo(this.rendimientoVigente) != 0) {
            return new APUSnapshot(this.id, this.partidaId, this.externalApuId, this.catalogSource,
                    this.rendimientoOriginal, nuevoRendimiento, true, usuarioId, LocalDateTime.now(),
                    this.unidadSnapshot, this.snapshotDate, this.insumos, this.version);
        }
        return this;
    }

    public APUSnapshot agregarInsumo(APUInsumoSnapshot insumo) {
        if (insumo == null) {
            throw new IllegalArgumentException("El insumo no puede ser nulo");
        }
        List<APUInsumoSnapshot> nuevosInsumos = new ArrayList<>(this.insumos);
        nuevosInsumos.add(insumo);
        return new APUSnapshot(this.id, this.partidaId, this.externalApuId, this.catalogSource,
                this.rendimientoOriginal, this.rendimientoVigente, this.rendimientoModificado,
                this.rendimientoModificadoPor, this.rendimientoModificadoEn, this.unidadSnapshot, this.snapshotDate,
                nuevosInsumos, this.version);
    }

    /**
     * Calcula el costo total usando el rendimiento vigente (método legacy). Para
     * nuevos APUs con cálculo dinámico, usar
     * calcularCostoTotal(CalculoApuDinamicoService, String).
     */
    public BigDecimal calcularCostoTotal() {
        BigDecimal base = insumos.stream().map(APUInsumoSnapshot::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return base.multiply(rendimientoVigente);
    }

    /**
     * Calcula el costo total usando el servicio de cálculo dinámico con fórmulas de
     * ingeniería civil. Respeta el orden de dependencias: MATERIAL, MANO_OBRA,
     * EQUIPO_MAQUINA → EQUIPO_HERRAMIENTA
     * 
     * @param calculoService El servicio de cálculo dinámico
     * @param monedaProyecto La moneda del proyecto para normalización
     * @return El costo total calculado dinámicamente
     */
    public BigDecimal calcularCostoTotal(CalculoApuDinamicoService calculoService, String monedaProyecto) {
        if (calculoService == null) {
            throw new IllegalArgumentException("El servicio de cálculo no puede ser nulo");
        }
        if (monedaProyecto == null || monedaProyecto.isBlank()) {
            throw new IllegalArgumentException("La moneda del proyecto no puede estar vacía");
        }
        return calculoService.calcularCostoTotalAPU(this, monedaProyecto);
    }

    public BigDecimal getDesviacionRendimiento() {
        return rendimientoVigente.subtract(rendimientoOriginal);
    }

    public List<APUInsumoSnapshot> getInsumos() {
        return List.copyOf(insumos);
    }

    public APUSnapshotId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public String getExternalApuId() {
        return externalApuId;
    }

    public String getCatalogSource() {
        return catalogSource;
    }

    public BigDecimal getRendimientoOriginal() {
        return rendimientoOriginal;
    }

    public BigDecimal getRendimientoVigente() {
        return rendimientoVigente;
    }

    public boolean isRendimientoModificado() {
        return rendimientoModificado;
    }

    public UUID getRendimientoModificadoPor() {
        return rendimientoModificadoPor;
    }

    public LocalDateTime getRendimientoModificadoEn() {
        return rendimientoModificadoEn;
    }

    public String getUnidadSnapshot() {
        return unidadSnapshot;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        APUSnapshot that = (APUSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("APUSnapshot{id=%s, partidaId=%s, externalApuId='%s', rendimientoVigente=%s, insumos=%d}",
                id, partidaId, externalApuId, rendimientoVigente, insumos.size());
    }
}
