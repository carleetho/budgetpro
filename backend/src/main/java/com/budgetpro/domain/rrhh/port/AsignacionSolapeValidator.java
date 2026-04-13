package com.budgetpro.domain.rrhh.port;

import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Puerto de dominio para la validación de solapes de asignación empleado ↔ proyecto (invariante R-03).
 * <p>
 * Recibe el identificador del empleado, la ventana temporal candidata (inicio/fin inclusive según política
 * que defina el adaptador) y las asignaciones ya conocidas para ese contexto de validación.
 * </p>
 */
public interface AsignacionSolapeValidator {

    /**
     * Garantiza que la ventana candidata no viola las reglas de solape acordadas para R-03.
     * <p>
     * La semántica multi-sitio permanece marcada como {@code AMBIGUITY_DETECTED} en el canónico RRHH;
     * la implementación de referencia en código permanece bloqueada hasta decisión de PO.
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
