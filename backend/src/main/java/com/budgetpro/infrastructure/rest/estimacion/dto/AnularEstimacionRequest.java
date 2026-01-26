package com.budgetpro.infrastructure.rest.estimacion.dto;

import jakarta.validation.constraints.NotEmpty;

public class AnularEstimacionRequest {

    @NotEmpty
    private String motivoAnulacion;

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }
}
