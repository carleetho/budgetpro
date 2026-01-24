package com.budgetpro.domain.logistica.transferencia.port.out;

import java.util.UUID;

public interface ExcepcionValidator {
    /**
     * Verifica si una excepción de tipo TRANSFERENCIA_MATERIAL_ENTRE_PROYECTOS
     * existe y está aprobada.
     * 
     * @param excepcionId ID de la excepción
     * @return true si es válida y aprobada, false en otro caso
     */
    boolean esExcepcionAprobada(UUID excepcionId);
}
