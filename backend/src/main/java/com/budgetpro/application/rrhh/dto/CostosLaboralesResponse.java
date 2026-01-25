package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CostosLaboralesResponse {
    private final BigDecimal totalCosto;
    private final String moneda;
    private final List<DesgloseCostoLaboral> desglose;
    private final Optional<VarianzaCostoLaboral> varianza;

    public CostosLaboralesResponse(BigDecimal totalCosto, String moneda, List<DesgloseCostoLaboral> desglose,
            Optional<VarianzaCostoLaboral> varianza) {
        this.totalCosto = totalCosto;
        this.moneda = moneda;
        this.desglose = desglose;
        this.varianza = varianza;
    }

    public BigDecimal getTotalCosto() {
        return totalCosto;
    }

    public String getMoneda() {
        return moneda;
    }

    public List<DesgloseCostoLaboral> getDesglose() {
        return desglose;
    }

    public Optional<VarianzaCostoLaboral> getVarianza() {
        return varianza;
    }
}
