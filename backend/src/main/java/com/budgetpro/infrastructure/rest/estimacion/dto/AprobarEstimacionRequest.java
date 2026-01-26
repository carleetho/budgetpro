package com.budgetpro.infrastructure.rest.estimacion.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AprobarEstimacionRequest {

    @NotNull
    private UUID aprobadoPor;

    public UUID getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(UUID aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }
}
