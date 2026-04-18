package com.budgetpro.domain.rrhh.port;

import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Puerto de dominio para la validación de solapes de asignación empleado ↔ proyecto (invariante R-03).
 * <p>
 * Recibe el identificador del empleado, la ventana temporal candidata (inicio/fin inclusive; fin {@code null}
 * = abierta) y las asignaciones ya conocidas para ese contexto de validación.
 * </p>
 */
public interface AsignacionSolapeValidator {

    /**
     * Garantiza que la ventana candidata no viola las reglas de solape R-03 (intersección inclusiva de
     * intervalos de fechas frente a las asignaciones del mismo empleado).
     * <p>
     * Implementación de referencia: {@link com.budgetpro.domain.rrhh.service.RegimenCivilSolapeValidator}.
     * </p>
     *
     * @param empleadoId              trabajador bajo validación
     * @param ventanaInicio           inicio de la ventana candidata (no null)
     * @param ventanaFin              fin de la ventana candidata (null = abierta hacia adelante, según política futura)
     * @param asignacionesExistentes  asignaciones a considerar (p. ej. las vigentes del empleado); no null
     */
    void validar(EmpleadoId empleadoId, LocalDate ventanaInicio, LocalDate ventanaFin,
            Collection<AsignacionProyecto> asignacionesExistentes);
}
