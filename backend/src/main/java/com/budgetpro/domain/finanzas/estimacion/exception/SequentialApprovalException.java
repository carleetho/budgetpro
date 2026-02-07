package com.budgetpro.domain.finanzas.estimacion.exception;

import java.util.UUID;
import java.util.Objects;

/**
 * Excepción de dominio lanzada cuando se intenta aprobar una estimación sin que
 * la estimación precedente (N-1) esté aprobada.
 * 
 * Esta excepción implementa la regla de negocio ES-01 (REGLA-010) que establece
 * que las estimaciones deben ser aprobadas en secuencia: - Estimación N no
 * puede ser aprobada si estimación N-1 no está en estado APROBADA - Esto
 * previene brechas financieras en la cadena de cobros - Garantiza integridad en
 * la contabilidad de avance y facturación
 * 
 * **HTTP Status:** 409 Conflict **AXIOM Enforcement:** Critical financial
 * governance rule **Severity:** CRITICAL
 * 
 * **Ejemplo:** - Proyecto tiene estimaciones #1 (APROBADA), #2 (BORRADOR), #3
 * (BORRADOR) - Intentar aprobar #3 lanza SequentialApprovalException (debe
 * aprobar #2 primero)
 * 
 * @see com.budgetpro.domain.finanzas.estimacion.model.Estimacion#aprobar()
 * @see com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion#APROBADA
 */
public class SequentialApprovalException extends RuntimeException {

    private final UUID proyectoId;
    private final Integer numeroEstimacion;
    private final Integer numeroPredecesor;

    /**
     * Constructor para violaciones de aprobación secuencial.
     * 
     * @param proyectoId       ID del proyecto
     * @param numeroEstimacion Número de la estimación que se intenta aprobar
     * @param numeroPredecesor Número de la estimación precedente que debe estar
     *                         aprobada
     * @param message          Mensaje adicional descriptivo
     * @throws NullPointerException si proyectoId es nulo
     */
    public SequentialApprovalException(UUID proyectoId, Integer numeroEstimacion, Integer numeroPredecesor,
            String message) {
        super(formatMessage(proyectoId, numeroEstimacion, numeroPredecesor, message));
        this.proyectoId = Objects.requireNonNull(proyectoId, "El ID del proyecto no puede ser nulo");
        this.numeroEstimacion = numeroEstimacion;
        this.numeroPredecesor = numeroPredecesor;
    }

    /**
     * Constructor simplificado con mensaje por defecto.
     * 
     * @param proyectoId       ID del proyecto
     * @param numeroEstimacion Número de la estimación que se intenta aprobar
     * @param numeroPredecesor Número de la estimación precedente
     */
    public SequentialApprovalException(UUID proyectoId, Integer numeroEstimacion, Integer numeroPredecesor) {
        this(proyectoId, numeroEstimacion, numeroPredecesor, String.format(
                "Previous estimation #%d must be APROBADA before approving #%d", numeroPredecesor, numeroEstimacion));
    }

    /**
     * Formatea el mensaje de la excepción con contexto completo.
     */
    private static String formatMessage(UUID proyectoId, Integer numeroEstimacion, Integer numeroPredecesor,
            String message) {
        return String.format(
                "Sequential approval violation: %s. Proyecto: %s, Estimation #%d cannot be approved (predecessor #%d not found or not APROBADA)",
                message, proyectoId, numeroEstimacion, numeroPredecesor);
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public Integer getNumeroEstimacion() {
        return numeroEstimacion;
    }

    public Integer getNumeroPredecesor() {
        return numeroPredecesor;
    }
}
