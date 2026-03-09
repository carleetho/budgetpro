package com.budgetpro.domain.finanzas.proyecto.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Enum que define la frecuencia de control/corte para reportes de proyecto.
 *
 * <ul>
 *   <li>SEMANAL: períodos de 7 días</li>
 *   <li>QUINCENAL: períodos de 15 días</li>
 *   <li>MENSUAL: períodos de mes calendario</li>
 * </ul>
 *
 * Invariante E-04: Period Consistency — valida que fechaCorte sea una fecha de
 * corte válida respecto a fechaInicio según la frecuencia configurada.
 */
public enum FrecuenciaControl {
    SEMANAL(7),
    QUINCENAL(15),
    MENSUAL(-1); // -1 indica mes calendario (variable)

    private final int dias;

    FrecuenciaControl(int dias) {
        this.dias = dias;
    }

    /**
     * Indica si {@code fechaCorte} es una fecha de corte válida respecto a
     * {@code fechaInicio} según esta frecuencia.
     *
     * @param fechaInicio inicio del período de referencia
     * @param fechaCorte  fecha de corte a validar
     * @return true si la fecha de corte es válida (aligns con la frecuencia)
     */
    public boolean esFechaValida(LocalDate fechaInicio, LocalDate fechaCorte) {
        Objects.requireNonNull(fechaInicio, "fechaInicio no puede ser nula");
        Objects.requireNonNull(fechaCorte, "fechaCorte no puede ser nula");

        long days = ChronoUnit.DAYS.between(fechaInicio, fechaCorte);
        if (days < 0) {
            return false;
        }

        return switch (this) {
            case SEMANAL -> days % dias == 0;
            case QUINCENAL -> days % dias == 0;
            case MENSUAL -> {
                long months = ChronoUnit.MONTHS.between(fechaInicio, fechaCorte);
                if (months < 0) {
                    yield false;
                }
                long expectedDays =
                        ChronoUnit.DAYS.between(fechaInicio, fechaInicio.plusMonths(months));
                yield days == expectedDays;
            }
        };
    }
}
