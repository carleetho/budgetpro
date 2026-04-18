package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Implementación temporal del puerto {@link AsignacionSolapeValidator}: sin algoritmo de solape hasta
 * cierre de ambigüedad R-03 (multi-sitio / régimen) con PO.
 */
public final class AmbiguityBlockedAsignacionSolapeValidator implements AsignacionSolapeValidator {

    @Override
    @SuppressWarnings("unused")
    public void validar(EmpleadoId empleadoId, LocalDate ventanaInicio, LocalDate ventanaFin,
            Collection<AsignacionProyecto> asignacionesExistentes) {
        throw new UnsupportedOperationException(
                "Bloqueado por AMBIGUITY_DETECTED en R-03. Pendiente definición de PO");
    }
}
