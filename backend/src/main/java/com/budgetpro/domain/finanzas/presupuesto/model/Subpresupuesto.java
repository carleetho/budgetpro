package com.budgetpro.domain.finanzas.presupuesto.model;

/**
 * Raíz conceptual del subpresupuesto (Opción B). Convenciones de baseline.
 */
public final class Subpresupuesto {

    private Subpresupuesto() {
    }

    /** Subpresupuesto sintético creado por migración/trigger para cada cabecera de presupuesto. */
    public static final String NOMBRE_PRINCIPAL = "Principal";
}
