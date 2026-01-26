package com.budgetpro.domain.finanzas.ordencambio.port;

import java.util.UUID;

/**
 * Puerto para la generación de números correlativos de órdenes de cambio.
 */
public interface NumeroOrdenGenerator {

    /**
     * Genera el siguiente número correlativo para una orden de cambio en un
     * proyecto. Formato esperado: "OC-001", "OC-002", etc.
     *
     * @param proyectoId ID del proyecto
     * @return El siguiente número disponible
     */
    String generarNumeroOrden(UUID proyectoId);
}
