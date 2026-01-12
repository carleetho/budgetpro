package com.budgetpro.infrastructure.persistence.exception;

import java.util.UUID;

/**
 * Excepción de infraestructura lanzada cuando se intenta crear una partida
 * con un código que ya existe en el mismo presupuesto.
 * 
 * Esta excepción captura violaciones de la constraint UNIQUE (presupuesto_id, codigo).
 */
public class PartidaDuplicadaException extends RuntimeException {

    private final String codigo;
    private final UUID presupuestoId;

    public PartidaDuplicadaException(String codigo, UUID presupuestoId) {
        super(String.format("Ya existe una partida con código '%s' en el presupuesto %s", codigo, presupuestoId));
        this.codigo = codigo;
        this.presupuestoId = presupuestoId;
    }

    public PartidaDuplicadaException(String codigo, UUID presupuestoId, Throwable cause) {
        super(String.format("Ya existe una partida con código '%s' en el presupuesto %s", codigo, presupuestoId), cause);
        this.codigo = codigo;
        this.presupuestoId = presupuestoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }
}
