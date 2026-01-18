package com.budgetpro.application.alertas.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para un análisis de presupuesto.
 */
public record AnalisisPresupuestoResponse(
    UUID id,
    UUID presupuestoId,
    LocalDateTime fechaAnalisis,
    int totalAlertas,
    int alertasCriticas,
    int alertasWarning,
    int alertasInfo,
    List<AlertaParametricaResponse> alertas
) {
    
    /**
     * DTO de respuesta para una alerta paramétrica.
     */
    public record AlertaParametricaResponse(
        UUID id,
        String tipoAlerta,
        String nivel,
        UUID partidaId,
        UUID recursoId,
        String mensaje,
        Double valorDetectado,
        Double valorEsperadoMin,
        Double valorEsperadoMax,
        String sugerencia
    ) {}
}
