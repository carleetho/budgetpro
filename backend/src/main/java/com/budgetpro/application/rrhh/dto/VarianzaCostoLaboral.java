package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;

public class VarianzaCostoLaboral {
    private final BigDecimal costoReal;
    private final BigDecimal costoEstimado;
    private final BigDecimal diferencia;
    private final BigDecimal porcentajeVarianza;

    public VarianzaCostoLaboral(BigDecimal costoReal, BigDecimal costoEstimado, BigDecimal diferencia,
            BigDecimal porcentajeVarianza) {
        this.costoReal = costoReal;
        this.costoEstimado = costoEstimado;
        this.diferencia = diferencia;
        this.porcentajeVarianza = porcentajeVarianza;
    }

    public BigDecimal getCostoReal() {
        return costoReal;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public BigDecimal getPorcentajeVarianza() {
        return porcentajeVarianza;
    }
}
