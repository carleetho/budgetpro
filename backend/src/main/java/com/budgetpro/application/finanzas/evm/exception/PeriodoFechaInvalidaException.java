package com.budgetpro.application.finanzas.evm.exception;

import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;

import java.time.LocalDate;

/**
 * Excepción lanzada cuando la fecha de corte no está alineada con la frecuencia
 * de control configurada en el proyecto (HTTP 422).
 */
public class PeriodoFechaInvalidaException extends RuntimeException {

    private final LocalDate fechaCorte;
    private final FrecuenciaControl frecuencia;

    public PeriodoFechaInvalidaException(LocalDate fechaCorte, FrecuenciaControl frecuencia) {
        super("Fecha de corte no alineada con frecuencia " + frecuencia.name());
        this.fechaCorte = fechaCorte;
        this.frecuencia = frecuencia;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public FrecuenciaControl getFrecuencia() {
        return frecuencia;
    }
}
