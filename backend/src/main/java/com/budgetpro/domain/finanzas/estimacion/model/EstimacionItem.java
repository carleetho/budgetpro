package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un item dentro de una estimación. Corresponde a una
 * partida del presupuesto con su avance en el periodo.
 */
public class EstimacionItem {

    private final EstimacionItemId id;
    private final UUID partidaId;
    private final String concepto;
    private final MontoEstimado montoContractual;

    // Estado previo
    private final PorcentajeAvance porcentajeAnterior;
    private final MontoEstimado montoAnterior;

    // Estado actual (Periodo)
    private PorcentajeAvance porcentajeActual;
    private MontoEstimado montoActual;

    private EstimacionItem(EstimacionItemId id, UUID partidaId, String concepto, MontoEstimado montoContractual,
            PorcentajeAvance porcentajeAnterior, MontoEstimado montoAnterior, PorcentajeAvance porcentajeActual,
            MontoEstimado montoActual) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El ID de la partida no puede ser nulo");
        this.concepto = Objects.requireNonNull(concepto, "El concepto no puede ser nulo");
        this.montoContractual = Objects.requireNonNull(montoContractual, "El monto contractual no puede ser nulo");
        this.porcentajeAnterior = Objects.requireNonNull(porcentajeAnterior,
                "El porcentaje anterior no puede ser nulo");
        this.montoAnterior = Objects.requireNonNull(montoAnterior, "El monto anterior no puede ser nulo");

        // Initial validation for current values
        this.porcentajeActual = Objects.requireNonNull(porcentajeActual, "El porcentaje actual no puede ser nulo");
        this.montoActual = Objects.requireNonNull(montoActual, "El monto actual no puede ser nulo");

        validarAcumulado();
    }

    public static EstimacionItem crear(UUID partidaId, String concepto, MontoEstimado montoContractual,
            PorcentajeAvance porcentajeAnterior, MontoEstimado montoAnterior) {
        // Al crear, el avance actual comienza en 0 para este periodo
        return new EstimacionItem(EstimacionItemId.random(), partidaId, concepto, montoContractual, porcentajeAnterior,
                montoAnterior, PorcentajeAvance.zero(), MontoEstimado.zero());
    }

    public static EstimacionItem reconstruir(EstimacionItemId id, UUID partidaId, String concepto,
            MontoEstimado montoContractual, PorcentajeAvance porcentajeAnterior, MontoEstimado montoAnterior,
            PorcentajeAvance porcentajeActual, MontoEstimado montoActual) {
        return new EstimacionItem(id, partidaId, concepto, montoContractual, porcentajeAnterior, montoAnterior,
                porcentajeActual, montoActual);
    }

    /**
     * Registra un avance en el periodo actual. Calcula el monto correspondiente
     * basado en el porcentaje.
     */
    public void registrarAvance(PorcentajeAvance nuevoPorcentajePeriodo) {
        this.porcentajeActual = nuevoPorcentajePeriodo;

        // Calculate amount: MontoContractual * (Porcentaje / 100)
        BigDecimal pctDecimal = nuevoPorcentajePeriodo.getValue().divide(new BigDecimal("100.00"), 4,
                RoundingMode.HALF_UP);
        BigDecimal montoCalculado = this.montoContractual.getValue().multiply(pctDecimal);

        this.montoActual = MontoEstimado.of(montoCalculado);

        validarAcumulado();
    }

    /**
     * Valida que el avance acumulado (anterior + actual) no exceda el 100%. Y que
     * los montos sean consistentes.
     */
    private void validarAcumulado() {
        BigDecimal totalPct = this.porcentajeAnterior.getValue().add(this.porcentajeActual.getValue());
        if (totalPct.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException(
                    "El avance acumulado no puede exceder el 100%. Actual: " + totalPct + "%");
        }

        // Validation of amounts logic could go here (e.g. Total Amount <= Contractual
        // Amount)
        BigDecimal totalMonto = this.montoAnterior.getValue().add(this.montoActual.getValue());
        // Allow a small epsilon for floating point errors if needed, but using
        // BigDecimal should be precise.
        // Optional: strict check against contractual amount?
        if (totalMonto.compareTo(this.montoContractual.getValue()) > 0) {
            throw new IllegalArgumentException("El monto acumulado excede el monto contractual.");
        }
    }

    public PorcentajeAvance getPorcentajeAcumulado() {
        return PorcentajeAvance.of(this.porcentajeAnterior.getValue().add(this.porcentajeActual.getValue()));
    }

    public MontoEstimado getMontoAcumulado() {
        return this.montoAnterior.sumar(this.montoActual);
    }

    public MontoEstimado getSaldoPorEjercer() {
        return montoContractual.restar(getMontoAcumulado());
    }

    // Getters
    public EstimacionItemId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public String getConcepto() {
        return concepto;
    }

    public MontoEstimado getMontoContractual() {
        return montoContractual;
    }

    public PorcentajeAvance getPorcentajeAnterior() {
        return porcentajeAnterior;
    }

    public MontoEstimado getMontoAnterior() {
        return montoAnterior;
    }

    public PorcentajeAvance getPorcentajeActual() {
        return porcentajeActual;
    }

    public MontoEstimado getMontoActual() {
        return montoActual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EstimacionItem that = (EstimacionItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public EstimacionId getSaldoPorEjercer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSaldoPorEjercer'");
    }
}
