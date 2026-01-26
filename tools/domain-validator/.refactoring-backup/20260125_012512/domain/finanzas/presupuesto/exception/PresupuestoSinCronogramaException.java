package com.budgetpro.domain.finanzas.presupuesto.exception;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando se intenta aprobar un presupuesto
 * sin que exista un cronograma (ProgramaObra) asociado al proyecto.
 * 
 * Esta excepción protege el principio de baseline: el presupuesto y el cronograma
 * deben congelarse juntos para establecer el baseline del proyecto.
 * 
 * **Cadena de Dependencias:**
 * Proyecto → Presupuesto (CONGELADO) → Tiempo (CONGELADO)
 * 
 * **Escenarios de uso:**
 * 
 * 1. **Aprobación sin cronograma:**
 *    - Intentar aprobar un presupuesto cuando no existe ProgramaObra para el proyecto
 *    - El baseline requiere tanto presupuesto como cronograma
 * 
 * **Principio de Baseline:**
 * Un proyecto no puede tener baseline parcial. Si el presupuesto se congela,
 * el cronograma también debe congelarse simultáneamente para mantener la
 * integridad del baseline del proyecto.
 */
public class PresupuestoSinCronogramaException extends IllegalStateException {

    private final PresupuestoId presupuestoId;
    private final UUID proyectoId;

    /**
     * Constructor para cuando falta el cronograma al aprobar el presupuesto.
     * 
     * @param presupuestoId ID del presupuesto que se intenta aprobar
     * @param proyectoId ID del proyecto asociado
     * @throws NullPointerException si presupuestoId o proyectoId son nulos
     */
    public PresupuestoSinCronogramaException(PresupuestoId presupuestoId, UUID proyectoId) {
        super(formatMessage(presupuestoId, proyectoId));
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El ID del presupuesto no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El ID del proyecto no puede ser nulo");
    }

    /**
     * Formatea el mensaje de la excepción con información detallada sobre la cadena de dependencias.
     */
    private static String formatMessage(PresupuestoId presupuestoId, UUID proyectoId) {
        return String.format(
                "No se puede aprobar el presupuesto %s del proyecto %s porque no existe un cronograma (ProgramaObra). " +
                "El principio de baseline requiere que Presupuesto y Cronograma se congelen simultáneamente. " +
                "Cadena de dependencias: Proyecto → Presupuesto (CONGELADO) → Tiempo (CONGELADO)",
                presupuestoId.getValue(),
                proyectoId
        );
    }

    /**
     * Obtiene el ID del presupuesto que se intentó aprobar.
     * 
     * @return El ID del presupuesto
     */
    public PresupuestoId getPresupuestoId() {
        return presupuestoId;
    }

    /**
     * Obtiene el ID del proyecto asociado.
     * 
     * @return El ID del proyecto
     */
    public UUID getProyectoId() {
        return proyectoId;
    }
}
