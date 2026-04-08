package com.budgetpro.application.rrhh.constant;

import java.math.BigDecimal;

/**
 * Constantes de negocio para nómina (capa aplicación).
 */
public final class NominaConstants {

    private NominaConstants() {
    }

    /** ISR como factor decimal sobre percepciones brutas (10%). */
    public static final BigDecimal PORCENTAJE_ISR = new BigDecimal("0.10");
}
