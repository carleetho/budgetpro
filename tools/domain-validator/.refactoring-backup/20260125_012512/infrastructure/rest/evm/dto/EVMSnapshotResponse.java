package com.budgetpro.infrastructure.rest.evm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object para respuestas de métricas EVM.
 */
public record EVMSnapshotResponse(UUID id, UUID proyectoId, LocalDateTime fechaCorte, LocalDateTime fechaCalculo,
        BigDecimal pv, BigDecimal ev, BigDecimal ac, BigDecimal bac, BigDecimal cv, BigDecimal sv, BigDecimal cpi,
        BigDecimal spi, BigDecimal eac, BigDecimal etc, BigDecimal vac, String interpretacion) {
}
