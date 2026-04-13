package com.budgetpro.domain.rrhh.exception;

import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.util.Objects;

/**
 * REGLA-150 en dominio: la operación de tareo exige proyecto en estado {@code ACTIVO}.
 * La capa de aplicación puede mapear a {@code ProyectoNoActivoException} para contrato REST establecido.
 */
public final class ProyectoNoActivoParaOperacionException extends RuntimeException {

    private final ProyectoId proyectoId;
    private final EstadoProyecto estado;

    public ProyectoNoActivoParaOperacionException(ProyectoId proyectoId, EstadoProyecto estado, String message) {
        super(message);
        this.proyectoId = Objects.requireNonNull(proyectoId, "proyectoId");
        this.estado = Objects.requireNonNull(estado, "estado");
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }
}
