package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AvancePartidaResponse {

    private UUID partidaId;
    private BigDecimal avanceAcumulado;

    public AvancePartidaResponse() {
    }

    public AvancePartidaResponse(UUID partidaId, BigDecimal avanceAcumulado) {
        this.partidaId = partidaId;
        this.avanceAcumulado = avanceAcumulado;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public BigDecimal getAvanceAcumulado() {
        return avanceAcumulado;
    }

    public void setAvanceAcumulado(BigDecimal avanceAcumulado) {
        this.avanceAcumulado = avanceAcumulado;
    }
}
