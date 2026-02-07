package com.budgetpro.domain.rrhh.exception;

import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;

import java.util.Objects;

/**
 * Excepción de dominio lanzada cuando se intenta registrar asistencia para un
 * trabajador que no está en estado ACTIVO.
 * 
 * Esta excepción implementa la regla de negocio R-02 que establece que solo los
 * trabajadores activos pueden registrar asistencia: - Trabajadores INACTIVOS no
 * pueden registrar asistencia (violación laboral) - Trabajadores SUSPENDIDOS no
 * pueden registrar asistencia - Solo trabajadores ACTIVOS pueden marcar
 * entrada/salida
 * 
 * **HTTP Status:** 409 Conflict **AXIOM Enforcement:** Critical labor law
 * compliance **Severity:** CRITICAL
 * 
 * **Contexto Legal:** Registrar asistencia de trabajadores inactivos puede: -
 * Violar regulaciones laborales - Generar pasivos no reconocidos - Crear
 * inconsistencias en nómina y costos de proyecto
 * 
 * @see com.budgetpro.domain.rrhh.model.Empleado#getEstado()
 * @see com.budgetpro.domain.rrhh.model.EstadoEmpleado#ACTIVO
 */
public class InactiveWorkerException extends RuntimeException {

    private final EmpleadoId empleadoId;
    private final EstadoEmpleado estadoActual;

    /**
     * Constructor para violaciones de registro de asistencia de trabajador
     * inactivo.
     * 
     * @param empleadoId   ID del empleado inactivo
     * @param estadoActual Estado actual del empleado (INACTIVO o SUSPENDIDO)
     * @param message      Mensaje adicional descriptivo
     * @throws NullPointerException si empleadoId o estadoActual son nulos
     */
    public InactiveWorkerException(EmpleadoId empleadoId, EstadoEmpleado estadoActual, String message) {
        super(formatMessage(empleadoId, estadoActual, message));
        this.empleadoId = Objects.requireNonNull(empleadoId, "El ID del empleado no puede ser nulo");
        this.estadoActual = Objects.requireNonNull(estadoActual, "El estado actual no puede ser nulo");
    }

    /**
     * Constructor simplificado con mensaje por defecto.
     * 
     * @param empleadoId   ID del empleado inactivo
     * @param estadoActual Estado actual del empleado
     */
    public InactiveWorkerException(EmpleadoId empleadoId, EstadoEmpleado estadoActual) {
        this(empleadoId, estadoActual,
                String.format("Cannot register attendance: Worker is %s (must be ACTIVO)", estadoActual));
    }

    /**
     * Formatea el mensaje de la excepción con contexto completo.
     */
    private static String formatMessage(EmpleadoId empleadoId, EstadoEmpleado estadoActual, String message) {
        return String.format("Inactive worker violation: %s. Empleado ID: %s, Estado: %s (expected: ACTIVO)", message,
                empleadoId.getValue(), estadoActual);
    }

    public EmpleadoId getEmpleadoId() {
        return empleadoId;
    }

    public EstadoEmpleado getEstadoActual() {
        return estadoActual;
    }
}
