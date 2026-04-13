package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Implementación neutra de {@link AsignacionSolapeValidator} mientras R-03 multi-sitio está
 * {@code AMBIGUITY_DETECTED}. Sustituir por política acordada con PO sin romper el puerto.
 */
public final class NoOpAsignacionSolapeValidator implements AsignacionSolapeValidator {

    @Override
    public void validar(EmpleadoId empleadoId, LocalDate ventanaInicio, LocalDate ventanaFin,
            Collection<AsignacionProyecto> asignacionesExistentes) {
        // Sin validación de solape de asignación hasta definición de negocio (R-03).
    }
}
