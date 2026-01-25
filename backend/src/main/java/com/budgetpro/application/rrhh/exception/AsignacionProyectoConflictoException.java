package com.budgetpro.application.rrhh.exception;

import com.budgetpro.domain.rrhh.model.EmpleadoId;

import java.time.LocalDate;

public class AsignacionProyectoConflictoException extends RuntimeException {
    public AsignacionProyectoConflictoException(EmpleadoId empleadoId, LocalDate inicio, LocalDate fin) {
        super(String.format("El empleado %s ya tiene una asignación activa que se solapa con el periodo %s a %s",
                empleadoId, inicio, fin != null ? fin : "indefinido"));
    }
}
