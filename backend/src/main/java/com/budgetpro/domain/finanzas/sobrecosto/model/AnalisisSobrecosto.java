package com.budgetpro.domain.finanzas.sobrecosto.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class AnalisisSobrecosto {
    private final AnalisisSobrecostoId id;
    private final UUID presupuestoId;
    // nosemgrep
    private BigDecimal porcentajeIndirectosOficinaCentral;
    // nosemgrep
    private BigDecimal porcentajeIndirectosOficinaCampo;
    // nosemgrep
    private BigDecimal porcentajeFinanciamiento;
    // nosemgrep
    private Boolean financiamientoCalculado;
    // nosemgrep
    private BigDecimal porcentajeUtilidad;
    // nosemgrep
    private BigDecimal porcentajeFianzas;
    // nosemgrep
    private BigDecimal porcentajeImpuestosReflejables;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    private AnalisisSobrecosto(AnalisisSobrecostoId id, UUID presupuestoId,
                              BigDecimal porcentajeIndirectosOficinaCentral,
                              BigDecimal porcentajeIndirectosOficinaCampo,
                              BigDecimal porcentajeFinanciamiento,
                              Boolean financiamientoCalculado,
                              BigDecimal porcentajeUtilidad,
                              BigDecimal porcentajeFianzas,
                              BigDecimal porcentajeImpuestosReflejables,
                              Long version) {
        validarInvariantes(presupuestoId, porcentajeIndirectosOficinaCentral,
                          porcentajeIndirectosOficinaCampo, porcentajeFinanciamiento,
                          porcentajeUtilidad, porcentajeFianzas, porcentajeImpuestosReflejables);
        this.id = Objects.requireNonNull(id);
        this.presupuestoId = Objects.requireNonNull(presupuestoId);
        this.porcentajeIndirectosOficinaCentral = porcentajeIndirectosOficinaCentral != null ? porcentajeIndirectosOficinaCentral : BigDecimal.ZERO;
        this.porcentajeIndirectosOficinaCampo = porcentajeIndirectosOficinaCampo != null ? porcentajeIndirectosOficinaCampo : BigDecimal.ZERO;
        this.porcentajeFinanciamiento = porcentajeFinanciamiento != null ? porcentajeFinanciamiento : BigDecimal.ZERO;
        this.financiamientoCalculado = financiamientoCalculado != null ? financiamientoCalculado : false;
        this.porcentajeUtilidad = porcentajeUtilidad != null ? porcentajeUtilidad : BigDecimal.ZERO;
        this.porcentajeFianzas = porcentajeFianzas != null ? porcentajeFianzas : BigDecimal.ZERO;
        this.porcentajeImpuestosReflejables = porcentajeImpuestosReflejables != null ? porcentajeImpuestosReflejables : BigDecimal.ZERO;
        this.version = version != null ? version : 0L;
    }

    public static AnalisisSobrecosto crear(AnalisisSobrecostoId id, UUID presupuestoId) {
        return new AnalisisSobrecosto(id, presupuestoId,
                                     BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                     false, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }

    public static AnalisisSobrecosto reconstruir(AnalisisSobrecostoId id, UUID presupuestoId,
                                                 BigDecimal porcentajeIndirectosOficinaCentral,
                                                 BigDecimal porcentajeIndirectosOficinaCampo,
                                                 BigDecimal porcentajeFinanciamiento,
                                                 Boolean financiamientoCalculado,
                                                 BigDecimal porcentajeUtilidad,
                                                 BigDecimal porcentajeFianzas,
                                                 BigDecimal porcentajeImpuestosReflejables,
                                                 Long version) {
        return new AnalisisSobrecosto(id, presupuestoId,
                                     porcentajeIndirectosOficinaCentral,
                                     porcentajeIndirectosOficinaCampo,
                                     porcentajeFinanciamiento,
                                     financiamientoCalculado,
                                     porcentajeUtilidad,
                                     porcentajeFianzas,
                                     porcentajeImpuestosReflejables,
                                     version);
    }

    private void validarInvariantes(UUID presupuestoId,
                                   BigDecimal porcentajeIndirectosOficinaCentral,
                                   BigDecimal porcentajeIndirectosOficinaCampo,
                                   BigDecimal porcentajeFinanciamiento,
                                   BigDecimal porcentajeUtilidad,
                                   BigDecimal porcentajeFianzas,
                                   BigDecimal porcentajeImpuestosReflejables) {
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }
        validarPorcentaje(porcentajeIndirectosOficinaCentral, "Indirectos Oficina Central");
        validarPorcentaje(porcentajeIndirectosOficinaCampo, "Indirectos Oficina Campo");
        validarPorcentaje(porcentajeFinanciamiento, "Financiamiento");
        validarPorcentaje(porcentajeUtilidad, "Utilidad");
        validarPorcentaje(porcentajeFianzas, "Fianzas");
        validarPorcentaje(porcentajeImpuestosReflejables, "Impuestos Reflejables");
    }

    private void validarPorcentaje(BigDecimal porcentaje, String nombre) {
        if (porcentaje != null) {
            if (porcentaje.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(String.format("El porcentaje de %s no puede ser negativo", nombre));
            }
            if (porcentaje.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException(String.format("El porcentaje de %s no puede ser mayor a 100%%", nombre));
            }
        }
    }

    public void actualizarIndirectos(BigDecimal porcentajeOficinaCentral, BigDecimal porcentajeOficinaCampo) {
        validarPorcentaje(porcentajeOficinaCentral, "Indirectos Oficina Central");
        validarPorcentaje(porcentajeOficinaCampo, "Indirectos Oficina Campo");
        this.porcentajeIndirectosOficinaCentral = porcentajeOficinaCentral;
        this.porcentajeIndirectosOficinaCampo = porcentajeOficinaCampo;
    }

    public void actualizarFinanciamiento(BigDecimal porcentaje, Boolean calculado) {
        validarPorcentaje(porcentaje, "Financiamiento");
        this.porcentajeFinanciamiento = porcentaje;
        this.financiamientoCalculado = calculado != null ? calculado : false;
    }

    public void actualizarUtilidad(BigDecimal porcentaje) {
        validarPorcentaje(porcentaje, "Utilidad");
        this.porcentajeUtilidad = porcentaje;
    }

    public void actualizarCargosAdicionales(BigDecimal porcentajeFianzas, BigDecimal porcentajeImpuestos) {
        validarPorcentaje(porcentajeFianzas, "Fianzas");
        validarPorcentaje(porcentajeImpuestos, "Impuestos Reflejables");
        this.porcentajeFianzas = porcentajeFianzas;
        this.porcentajeImpuestosReflejables = porcentajeImpuestos;
    }

    public BigDecimal getPorcentajeIndirectosTotal() {
        return porcentajeIndirectosOficinaCentral.add(porcentajeIndirectosOficinaCampo);
    }

    public BigDecimal getPorcentajeCargosAdicionalesTotal() {
        return porcentajeFianzas.add(porcentajeImpuestosReflejables);
    }

    public AnalisisSobrecostoId getId() { return id; }
    public UUID getPresupuestoId() { return presupuestoId; }
    public BigDecimal getPorcentajeIndirectosOficinaCentral() { return porcentajeIndirectosOficinaCentral; }
    public BigDecimal getPorcentajeIndirectosOficinaCampo() { return porcentajeIndirectosOficinaCampo; }
    public BigDecimal getPorcentajeFinanciamiento() { return porcentajeFinanciamiento; }
    public Boolean getFinanciamientoCalculado() { return financiamientoCalculado; }
    public BigDecimal getPorcentajeUtilidad() { return porcentajeUtilidad; }
    public BigDecimal getPorcentajeFianzas() { return porcentajeFianzas; }
    public BigDecimal getPorcentajeImpuestosReflejables() { return porcentajeImpuestosReflejables; }
    public Long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalisisSobrecosto that = (AnalisisSobrecosto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("AnalisisSobrecosto{id=%s, presupuestoId=%s, indirectosTotal=%s%%, utilidad=%s%%}", 
                           id, presupuestoId, getPorcentajeIndirectosTotal(), porcentajeUtilidad);
    }
}
