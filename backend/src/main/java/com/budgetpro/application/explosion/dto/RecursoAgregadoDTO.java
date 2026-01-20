package com.budgetpro.application.explosion.dto;

import java.math.BigDecimal;

/**
 * DTO que representa un recurso agregado en la explosión de insumos.
 */
public record RecursoAgregadoDTO(
        String recursoExternalId,
        String recursoNombre,
        BigDecimal cantidadTotal,
        String unidad,
        BigDecimal cantidadBase,
        BigDecimal factorConversion
) {
}
