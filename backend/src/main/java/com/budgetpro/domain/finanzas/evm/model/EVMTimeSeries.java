package com.budgetpro.domain.finanzas.evm.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Read-model que representa un período en la serie temporal de métricas EVM.
 * 
 * Este objeto es inmutable y representa los valores acumulados y deltas de período
 * para un proyecto en una fecha de corte específica. No es un aggregate root,
 * sino un modelo de lectura optimizado para consultas de series temporales.
 * 
 * Los valores acumulados se convierten en deltas de período comparándolos con
 * los valores del período anterior.
 */
public final class EVMTimeSeries {

    private final EVMTimeSeriesId id;
    private final UUID proyectoId;
    private final LocalDate fechaCorte;
    private final int periodo;
    private final String moneda;

    // Valores acumulados (desde inicio del proyecto hasta fechaCorte)
    private final BigDecimal pvAcumulado; // Planned Value acumulado
    private final BigDecimal evAcumulado; // Earned Value acumulado
    private final BigDecimal acAcumulado; // Actual Cost acumulado
    private final BigDecimal bacTotal; // Budget at Completion total
    private final BigDecimal bacAjustado; // Budget at Completion ajustado

    // Métricas de período (calculadas como deltas desde período anterior)
    private final BigDecimal cpiPeriodo; // Cost Performance Index del período
    private final BigDecimal spiPeriodo; // Schedule Performance Index del período

    private EVMTimeSeries(
            EVMTimeSeriesId id,
            UUID proyectoId,
            LocalDate fechaCorte,
            int periodo,
            String moneda,
            BigDecimal pvAcumulado,
            BigDecimal evAcumulado,
            BigDecimal acAcumulado,
            BigDecimal bacTotal,
            BigDecimal bacAjustado,
            BigDecimal cpiPeriodo,
            BigDecimal spiPeriodo) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.fechaCorte = Objects.requireNonNull(fechaCorte, "La fecha de corte no puede ser nula");
        this.periodo = periodo;
        this.moneda = Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        this.pvAcumulado = Objects.requireNonNull(pvAcumulado, "El PV acumulado no puede ser nulo");
        this.evAcumulado = Objects.requireNonNull(evAcumulado, "El EV acumulado no puede ser nulo");
        this.acAcumulado = Objects.requireNonNull(acAcumulado, "El AC acumulado no puede ser nulo");
        this.bacTotal = Objects.requireNonNull(bacTotal, "El BAC total no puede ser nulo");
        this.bacAjustado = Objects.requireNonNull(bacAjustado, "El BAC ajustado no puede ser nulo");
        this.cpiPeriodo = Objects.requireNonNull(cpiPeriodo, "El CPI del período no puede ser nulo");
        this.spiPeriodo = Objects.requireNonNull(spiPeriodo, "El SPI del período no puede ser nulo");
    }

    /**
     * Factory method para crear una instancia de EVMTimeSeries.
     * 
     * Calcula automáticamente los deltas de período y los índices CPI/SPI
     * comparando los valores acumulados con los del período anterior.
     * 
     * @param id Identificador único de la serie temporal
     * @param proyectoId ID del proyecto
     * @param fechaCorte Fecha de corte del período
     * @param periodo Número de período (1, 2, 3, ...)
     * @param pvAcumulado Planned Value acumulado hasta esta fecha
     * @param evAcumulado Earned Value acumulado hasta esta fecha
     * @param acAcumulado Actual Cost acumulado hasta esta fecha
     * @param bacTotal Budget at Completion total
     * @param bacAjustado Budget at Completion ajustado
     * @param pvAcumuladoAnterior PV acumulado del período anterior (0 para período 1)
     * @param evAcumuladoAnterior EV acumulado del período anterior (0 para período 1)
     * @param acAcumuladoAnterior AC acumulado del período anterior (0 para período 1)
     * @param moneda Código de moneda (ISO 4217, ej: "USD", "PEN")
     * @return Una nueva instancia de EVMTimeSeries con métricas calculadas
     */
    public static EVMTimeSeries crear(
            EVMTimeSeriesId id,
            UUID proyectoId,
            LocalDate fechaCorte,
            int periodo,
            BigDecimal pvAcumulado,
            BigDecimal evAcumulado,
            BigDecimal acAcumulado,
            BigDecimal bacTotal,
            BigDecimal bacAjustado,
            BigDecimal pvAcumuladoAnterior,
            BigDecimal evAcumuladoAnterior,
            BigDecimal acAcumuladoAnterior,
            String moneda) {

        // Calcular deltas de período
        BigDecimal pvPeriodo = pvAcumulado.subtract(pvAcumuladoAnterior);
        BigDecimal evPeriodo = evAcumulado.subtract(evAcumuladoAnterior);
        BigDecimal acPeriodo = acAcumulado.subtract(acAcumuladoAnterior);

        // Calcular CPI del período: EV_periodo / AC_periodo
        // Si AC_periodo = 0, retornar 0 (división por cero)
        BigDecimal cpiPeriodo = dividirSeguro(evPeriodo, acPeriodo);

        // Calcular SPI del período: EV_periodo / PV_periodo
        // Si PV_periodo = 0, retornar 0 (división por cero)
        BigDecimal spiPeriodo = dividirSeguro(evPeriodo, pvPeriodo);

        return new EVMTimeSeries(
                id,
                proyectoId,
                fechaCorte,
                periodo,
                moneda,
                pvAcumulado,
                evAcumulado,
                acAcumulado,
                bacTotal,
                bacAjustado,
                cpiPeriodo,
                spiPeriodo);
    }

    /**
     * Factory method para reconstruir una instancia desde persistencia.
     *
     * Usa directamente los índices de período almacenados (CPI/SPI) para evitar
     * recalcularlos con deltas inexistentes en el contexto de lectura.
     */
    public static EVMTimeSeries reconstruir(
            EVMTimeSeriesId id,
            UUID proyectoId,
            LocalDate fechaCorte,
            int periodo,
            BigDecimal pvAcumulado,
            BigDecimal evAcumulado,
            BigDecimal acAcumulado,
            BigDecimal bacTotal,
            BigDecimal bacAjustado,
            BigDecimal cpiPeriodo,
            BigDecimal spiPeriodo,
            String moneda) {
        return new EVMTimeSeries(
                id,
                proyectoId,
                fechaCorte,
                periodo,
                moneda,
                pvAcumulado,
                evAcumulado,
                acAcumulado,
                bacTotal,
                bacAjustado,
                cpiPeriodo,
                spiPeriodo);
    }

    /**
     * Realiza una división segura que retorna BigDecimal.ZERO si el denominador es cero.
     * 
     * Utiliza RoundingMode.HALF_UP con scale 4 para todas las divisiones.
     * 
     * @param numerador El valor a dividir
     * @param denominador El divisor
     * @return El resultado de la división, o BigDecimal.ZERO si denominador es cero
     */
    private static BigDecimal dividirSeguro(BigDecimal numerador, BigDecimal denominador) {
        if (numerador == null || denominador == null || denominador.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerador.divide(denominador, 4, RoundingMode.HALF_UP);
    }

    // Getters

    public EVMTimeSeriesId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public int getPeriodo() {
        return periodo;
    }

    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getPvAcumulado() {
        return pvAcumulado;
    }

    public BigDecimal getEvAcumulado() {
        return evAcumulado;
    }

    public BigDecimal getAcAcumulado() {
        return acAcumulado;
    }

    public BigDecimal getBacTotal() {
        return bacTotal;
    }

    public BigDecimal getBacAjustado() {
        return bacAjustado;
    }

    public BigDecimal getCpiPeriodo() {
        return cpiPeriodo;
    }

    public BigDecimal getSpiPeriodo() {
        return spiPeriodo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EVMTimeSeries that = (EVMTimeSeries) o;
        return periodo == that.periodo
                && Objects.equals(id, that.id)
                && Objects.equals(proyectoId, that.proyectoId)
                && Objects.equals(fechaCorte, that.fechaCorte)
                && Objects.equals(moneda, that.moneda)
                && Objects.equals(pvAcumulado, that.pvAcumulado)
                && Objects.equals(evAcumulado, that.evAcumulado)
                && Objects.equals(acAcumulado, that.acAcumulado)
                && Objects.equals(bacTotal, that.bacTotal)
                && Objects.equals(bacAjustado, that.bacAjustado)
                && Objects.equals(cpiPeriodo, that.cpiPeriodo)
                && Objects.equals(spiPeriodo, that.spiPeriodo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, proyectoId, fechaCorte, periodo, moneda, pvAcumulado, evAcumulado,
                acAcumulado, bacTotal, bacAjustado, cpiPeriodo, spiPeriodo);
    }

    @Override
    public String toString() {
        return String.format(
                "EVMTimeSeries{id=%s, proyectoId=%s, fechaCorte=%s, periodo=%d, moneda='%s', "
                        + "pvAcumulado=%s, evAcumulado=%s, acAcumulado=%s, bacTotal=%s, bacAjustado=%s, "
                        + "cpiPeriodo=%s, spiPeriodo=%s}",
                id, proyectoId, fechaCorte, periodo, moneda, pvAcumulado, evAcumulado, acAcumulado,
                bacTotal, bacAjustado, cpiPeriodo, spiPeriodo);
    }
}
