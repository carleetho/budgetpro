package com.budgetpro.application.presupuesto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Excepción lanzada cuando un presupuesto no puede aprobarse.
 * 
 * Razones posibles: - Ya está aprobado - Faltan APUs en partidas hoja - Estado
 * inválido
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class PresupuestoNoPuedeAprobarseException extends RuntimeException {

    public PresupuestoNoPuedeAprobarseException(UUID presupuestoId, String razon) {
        super(String.format("El presupuesto con ID '%s' no puede aprobarse: %s", presupuestoId, razon));
    }
}
