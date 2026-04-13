package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.rrhh.exception.AsignacionSuperpuestaException;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

/**
 * R-03 según decisión de PO: bloqueo duro si la ventana candidata intersecta el intervalo de fechas de cualquier
 * asignación existente del mismo empleado (presencia en dos obras con solape de calendario prohibido).
 */
public final class RegimenCivilSolapeValidator implements AsignacionSolapeValidator {

    @Override
    public void validar(EmpleadoId empleadoId, LocalDate ventanaInicio, LocalDate ventanaFin,
            Collection<AsignacionProyecto> asignacionesExistentes) {
        Objects.requireNonNull(empleadoId, "empleadoId must not be null");
        Objects.requireNonNull(ventanaInicio, "ventanaInicio must not be null");
        Objects.requireNonNull(asignacionesExistentes, "asignacionesExistentes must not be null");

        LocalDate vEnd = ventanaFin != null ? ventanaFin : LocalDate.MAX;

        for (AsignacionProyecto existente : asignacionesExistentes) {
            Objects.requireNonNull(existente, "asignación existente must not be null");
            if (!existente.getEmpleadoId().equals(empleadoId)) {
                continue;
            }
            LocalDate aEnd = existente.getFechaFin() != null ? existente.getFechaFin() : LocalDate.MAX;
            if (intervalosSolapanInclusive(ventanaInicio, vEnd, existente.getFechaInicio(), aEnd)) {
                throw new AsignacionSuperpuestaException(String.format(
                        "Solape de asignación (R-03): el empleado %s ya tiene cobertura en el periodo que intersecta [%s .. %s].",
                        empleadoId.getValue(), existente.getFechaInicio(),
                        existente.getFechaFin() != null ? existente.getFechaFin() : "abierto"));
            }
        }
    }

    /**
     * Intersección inclusiva de [s1, e1] y [s2, e2] en fechas calendario (fin inclusive).
     */
    static boolean intervalosSolapanInclusive(LocalDate s1, LocalDate e1, LocalDate s2, LocalDate e2) {
        Objects.requireNonNull(s1, "s1");
        Objects.requireNonNull(e1, "e1");
        Objects.requireNonNull(s2, "s2");
        Objects.requireNonNull(e2, "e2");
        return !s1.isAfter(e2) && !s2.isAfter(e1);
    }
}
