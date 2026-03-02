package com.budgetpro.infrastructure.rest.evm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de respuesta REST para la curva S del endpoint EVM.
 */
public record SCurveResponse(
        UUID proyectoId,
        String moneda,
        BigDecimal bacTotal,
        BigDecimal bacAjustado,
        List<SCurveDataPoint> dataPoints) {
    public SCurveResponse {
        dataPoints = dataPoints == null ? List.of() : List.copyOf(dataPoints);
    }

    public record SCurveDataPoint(
            LocalDate fechaCorte,
            int periodo,
            BigDecimal pvAcumulado,
            BigDecimal evAcumulado,
            BigDecimal acAcumulado,
            /*
             * scale 4 y HALF_UP; métrica por periodo (no acumulada).
             */
            BigDecimal cpiPeriodo,
            /*
             * scale 4 y HALF_UP; métrica por periodo (no acumulada).
             */
            BigDecimal spiPeriodo) {
        public SCurveDataPoint {
            Objects.requireNonNull(fechaCorte, "fechaCorte no puede ser null");
            Objects.requireNonNull(pvAcumulado, "pvAcumulado no puede ser null");
            Objects.requireNonNull(evAcumulado, "evAcumulado no puede ser null");
            Objects.requireNonNull(acAcumulado, "acAcumulado no puede ser null");
            cpiPeriodo = cpiPeriodo == null ? BigDecimal.ZERO : cpiPeriodo;
            spiPeriodo = spiPeriodo == null ? BigDecimal.ZERO : spiPeriodo;
        }
    }
}
