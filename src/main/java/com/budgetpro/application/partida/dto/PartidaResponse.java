package com.budgetpro.application.partida.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de una partida.
 * 
 * Incluye todos los campos necesarios para el cliente REST,
 * incluyendo el objeto `saldos` desglosado (presupuestado, reservado, ejecutado, disponible).
 */
public record PartidaResponse(
        UUID id,
        UUID proyectoId,
        UUID presupuestoId,
        String codigo,
        String nombre,
        String tipo,
        EstadoPartidaResponse estado,
        SaldosPartidaResponse saldos,
        Long version
) {
    /**
     * DTO anidado que representa el estado de la partida.
     */
    public enum EstadoPartidaResponse {
        BORRADOR,
        APROBADA,
        CERRADA
    }

    /**
     * DTO anidado que representa los saldos desglosados de una partida.
     */
    public record SaldosPartidaResponse(
            BigDecimal presupuestado,
            BigDecimal reservado,
            BigDecimal ejecutado,
            BigDecimal disponible
    ) {
    }
}
