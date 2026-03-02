package com.budgetpro.application.finanzas.evm.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado de aplicación para la consulta de curva S.
 */
public record SCurveResult(
        UUID proyectoId,
        String moneda,
        BigDecimal bacTotal,
        BigDecimal bacAjustado,
        List<SCurveDataPoint> dataPoints) {
    public SCurveResult {
        dataPoints = dataPoints == null ? List.of() : List.copyOf(dataPoints);
    }

    public record SCurveDataPoint(
            LocalDate fechaCorte,
            int periodo,
            BigDecimal pvAcumulado,
            BigDecimal evAcumulado,
            BigDecimal acAcumulado,
            BigDecimal cpiPeriodo,
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
