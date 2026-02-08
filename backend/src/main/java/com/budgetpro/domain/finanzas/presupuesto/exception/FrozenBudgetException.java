package com.budgetpro.domain.finanzas.presupuesto.exception;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.util.Objects;

/**
 * Excepción de dominio lanzada cuando se intenta modificar un presupuesto
 * congelado.
 * 
 * Un presupuesto congelado (ESTADO = CONGELADO) no permite operaciones de
 * modificación en su estructura, incluyendo: - Creación de nuevas partidas -
 * Modificación de partidas existentes - Eliminación de partidas - Cambios en la
 * jerarquía del presupuesto
 * 
 * Esta excepción implementa fail-fast validation en la capa de aplicación para
 * prevenir violaciones de la regla de negocio P-01 (REGLA-001).
 * 
 * **HTTP Status:** 409 Conflict **AXIOM Enforcement:** Critical domain
 * invariant protection
 * 
 * @see com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto#isAprobado()
 * @see com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto#CONGELADO
 */
public class FrozenBudgetException extends RuntimeException {

    private final PresupuestoId presupuestoId;

    /**
     * Constructor para violaciones de presupuesto congelado.
     * 
     * @param presupuestoId ID del presupuesto congelado
     * @param message       Mensaje descriptivo de la operación bloqueada
     * @throws NullPointerException si presupuestoId es nulo
     */
    public FrozenBudgetException(PresupuestoId presupuestoId, String message) {
        super(formatMessage(presupuestoId, message));
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El ID del presupuesto no puede ser nulo");
    }

    /**
     * Constructor simplificado con mensaje por defecto.
     * 
     * @param presupuestoId ID del presupuesto congelado
     */
    public FrozenBudgetException(PresupuestoId presupuestoId) {
        this(presupuestoId, "Cannot modify frozen budget");
    }

    /**
     * Formatea el mensaje de la excepción para contexto completo.
     */
    private static String formatMessage(PresupuestoId presupuestoId, String message) {
        return String.format("Frozen budget violation: %s. Budget ID: %s (ESTADO=CONGELADO)", message,
                presupuestoId.getValue());
    }

    /**
     * Obtiene el ID del presupuesto congelado.
     * 
     * @return El ID del presupuesto
     */
    public PresupuestoId getPresupuestoId() {
        return presupuestoId;
    }
}
