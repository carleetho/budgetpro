package com.budgetpro.domain.finanzas.estimacion.exception;

import java.time.LocalDate;
import java.util.UUID;

public class PeriodoSolapadoException extends RuntimeException {

    public PeriodoSolapadoException(UUID proyectoId, LocalDate inicio, LocalDate fin) {
        super(String.format("Ya existe una estimación para el proyecto %s que se solapa con el periodo %s - %s",
                proyectoId, inicio, fin));
    }
}
