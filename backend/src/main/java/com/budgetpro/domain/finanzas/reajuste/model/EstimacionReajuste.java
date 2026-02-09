package com.budgetpro.domain.finanzas.reajuste.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raíz que representa una Estimación de Reajuste de Costos.
 */
public final class EstimacionReajuste {

    private final EstimacionReajusteId id;
    private final UUID proyectoId;
    private final UUID presupuestoId;
    private final Integer numeroEstimacion;
    private final LocalDate fechaCorte;
    private final String indiceBaseCodigo;
    private final LocalDate indiceBaseFecha;
    private final String indiceActualCodigo;
    private final LocalDate indiceActualFecha;
    private final BigDecimal valorIndiceBase;
    private final BigDecimal valorIndiceActual;
    private final BigDecimal montoBase;
    private final BigDecimal montoReajustado;
    private final BigDecimal diferencial;
    private final BigDecimal porcentajeVariacion;
    private final EstadoEstimacionReajuste estado;
    private final String observaciones;
    private final List<DetalleReajustePartida> detalles;
    private final Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private EstimacionReajuste(EstimacionReajusteId id, UUID proyectoId, UUID presupuestoId, Integer numeroEstimacion,
            LocalDate fechaCorte, String indiceBaseCodigo, LocalDate indiceBaseFecha, String indiceActualCodigo,
            LocalDate indiceActualFecha, BigDecimal valorIndiceBase, BigDecimal valorIndiceActual, BigDecimal montoBase,
            BigDecimal montoReajustado, BigDecimal diferencial, BigDecimal porcentajeVariacion,
            EstadoEstimacionReajuste estado, String observaciones, List<DetalleReajustePartida> detalles,
            Long version) {
        validarInvariantes(proyectoId, presupuestoId, numeroEstimacion, valorIndiceBase, valorIndiceActual);

        this.id = Objects.requireNonNull(id, "El ID de la estimación no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.numeroEstimacion = numeroEstimacion;
        this.fechaCorte = fechaCorte;
        this.indiceBaseCodigo = indiceBaseCodigo;
        this.indiceBaseFecha = indiceBaseFecha;
        this.indiceActualCodigo = indiceActualCodigo;
        this.indiceActualFecha = indiceActualFecha;
        this.valorIndiceBase = valorIndiceBase;
        this.valorIndiceActual = valorIndiceActual;
        this.montoBase = montoBase != null ? montoBase : BigDecimal.ZERO;
        this.montoReajustado = montoReajustado != null ? montoReajustado : BigDecimal.ZERO;
        this.diferencial = diferencial != null ? diferencial : BigDecimal.ZERO;
        this.porcentajeVariacion = porcentajeVariacion != null ? porcentajeVariacion : BigDecimal.ZERO;
        this.estado = estado != null ? estado : EstadoEstimacionReajuste.BORRADOR;
        this.observaciones = observaciones;
        this.detalles = detalles != null ? Collections.unmodifiableList(new ArrayList<>(detalles))
                : Collections.emptyList();
        this.version = version != null ? version : 0L;
    }

    public static EstimacionReajuste crear(EstimacionReajusteId id, UUID proyectoId, UUID presupuestoId,
            Integer numeroEstimacion, LocalDate fechaCorte, String indiceBaseCodigo, LocalDate indiceBaseFecha,
            String indiceActualCodigo, LocalDate indiceActualFecha, BigDecimal valorIndiceBase,
            BigDecimal valorIndiceActual, BigDecimal montoBase) {
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual, montoBase,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, EstadoEstimacionReajuste.BORRADOR, null, null, 0L);
    }

    public static EstimacionReajuste reconstruir(EstimacionReajusteId id, UUID proyectoId, UUID presupuestoId,
            Integer numeroEstimacion, LocalDate fechaCorte, String indiceBaseCodigo, LocalDate indiceBaseFecha,
            String indiceActualCodigo, LocalDate indiceActualFecha, BigDecimal valorIndiceBase,
            BigDecimal valorIndiceActual, BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial,
            BigDecimal porcentajeVariacion, EstadoEstimacionReajuste estado, String observaciones,
            List<DetalleReajustePartida> detalles, Long version) {
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual, montoBase,
                montoReajustado, diferencial, porcentajeVariacion, estado, observaciones, detalles, version);
    }

    private void validarInvariantes(UUID proyectoId, UUID presupuestoId, Integer numeroEstimacion,
            BigDecimal valorIndiceBase, BigDecimal valorIndiceActual) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }
        if (numeroEstimacion != null && numeroEstimacion <= 0) {
            throw new IllegalArgumentException("El número de estimación debe ser positivo");
        }
        if (valorIndiceBase != null && valorIndiceBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del índice base debe ser mayor a cero");
        }
        if (valorIndiceActual != null && valorIndiceActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del índice actual debe ser mayor a cero");
        }
    }

    public EstimacionReajuste calcularMontoReajustado(BigDecimal montoBaseTotal) {
        if (valorIndiceBase == null || valorIndiceActual == null) {
            throw new IllegalStateException("Los índices deben estar configurados para calcular el reajuste");
        }

        BigDecimal nuevoMontoReajustado = montoBaseTotal.multiply(valorIndiceActual).divide(valorIndiceBase, 4,
                RoundingMode.HALF_UP);

        BigDecimal nuevoDiferencial = nuevoMontoReajustado.subtract(montoBaseTotal).setScale(4, RoundingMode.HALF_UP);

        BigDecimal factor = valorIndiceActual.divide(valorIndiceBase, 4, RoundingMode.HALF_UP);
        BigDecimal nuevoPorcentajeVariacion = factor.subtract(BigDecimal.ONE).multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual,
                montoBaseTotal, nuevoMontoReajustado, nuevoDiferencial, nuevoPorcentajeVariacion, estado, observaciones,
                detalles, version);
    }

    public EstimacionReajuste agregarDetalle(DetalleReajustePartida detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        List<DetalleReajustePartida> nuevosDetalles = new ArrayList<>(this.detalles);
        nuevosDetalles.add(detalle);
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual, montoBase,
                montoReajustado, diferencial, porcentajeVariacion, estado, observaciones, nuevosDetalles, version);
    }

    public EstimacionReajuste aprobar() {
        if (this.estado != EstadoEstimacionReajuste.BORRADOR) {
            throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR");
        }
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual, montoBase,
                montoReajustado, diferencial, porcentajeVariacion, EstadoEstimacionReajuste.APROBADA, observaciones,
                detalles, version);
    }

    public EstimacionReajuste aplicar() {
        if (this.estado != EstadoEstimacionReajuste.APROBADA) {
            throw new IllegalStateException("Solo se pueden aplicar estimaciones en estado APROBADA");
        }
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual, montoBase,
                montoReajustado, diferencial, porcentajeVariacion, EstadoEstimacionReajuste.APLICADA, observaciones,
                detalles, version);
    }

    public EstimacionReajuste actualizarObservaciones(String nuevasObservaciones) {
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte, indiceBaseCodigo,
                indiceBaseFecha, indiceActualCodigo, indiceActualFecha, valorIndiceBase, valorIndiceActual, montoBase,
                montoReajustado, diferencial, porcentajeVariacion, estado, nuevasObservaciones, detalles, version);
    }

    // Getters

    public EstimacionReajusteId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public Integer getNumeroEstimacion() {
        return numeroEstimacion;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public String getIndiceBaseCodigo() {
        return indiceBaseCodigo;
    }

    public LocalDate getIndiceBaseFecha() {
        return indiceBaseFecha;
    }

    public String getIndiceActualCodigo() {
        return indiceActualCodigo;
    }

    public LocalDate getIndiceActualFecha() {
        return indiceActualFecha;
    }

    public BigDecimal getValorIndiceBase() {
        return valorIndiceBase;
    }

    public BigDecimal getValorIndiceActual() {
        return valorIndiceActual;
    }

    public BigDecimal getMontoBase() {
        return montoBase;
    }

    public BigDecimal getMontoReajustado() {
        return montoReajustado;
    }

    public BigDecimal getDiferencial() {
        return diferencial;
    }

    public BigDecimal getPorcentajeVariacion() {
        return porcentajeVariacion;
    }

    public EstadoEstimacionReajuste getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public List<DetalleReajustePartida> getDetalles() {
        return detalles;
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
        EstimacionReajuste that = (EstimacionReajuste) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("EstimacionReajuste{id=%s, numero=%d, estado=%s}", id, numeroEstimacion, estado);
    }
}
