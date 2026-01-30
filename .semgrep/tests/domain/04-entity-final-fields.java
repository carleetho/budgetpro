package com.budgetpro.domain.finanzas.presupuesto.model;

// Test para dominio crítico (presupuesto) - debe generar ERROR
public class PresupuestoTestEntity {
    // ruleid: budgetpro.domain.immutability.entity-final-fields.critical
    private String campoMutable;

    // ruleid: budgetpro.domain.immutability.entity-final-fields.critical
    private Integer valorSinFinal;

    // ok: budgetpro.domain.immutability.entity-final-fields.critical
    private final String campoInmutable;

    // ok: budgetpro.domain.immutability.entity-final-fields.critical
    private final Integer valorConFinal;
}

package com.budgetpro.domain.finanzas.estimacion.model;

// Test para dominio crítico (estimacion) - debe generar ERROR
public class EstimacionTestEntity {
    // ruleid: budgetpro.domain.immutability.entity-final-fields.critical
    private String campoMutable;

    // ok: budgetpro.domain.immutability.entity-final-fields.critical
    private final String campoInmutable;
}

package com.budgetpro.domain.catalogo.model;

// Test para dominio no crítico - debe generar WARNING
public class CatalogoTestEntity {
    // ruleid: budgetpro.domain.immutability.entity-final-fields
    private String campoMutable;

    // ruleid: budgetpro.domain.immutability.entity-final-fields
    private Long idSinFinal;

    // ok: budgetpro.domain.immutability.entity-final-fields
    private final String campoInmutable;

    // ok: budgetpro.domain.immutability.entity-final-fields
    private final Long idConFinal;
}

package com.budgetpro.domain.proyecto.model;

// Test para dominio no crítico - debe generar WARNING
public class ProyectoTestEntity {
    // ruleid: budgetpro.domain.immutability.entity-final-fields
    private String nombre;

    // ok: budgetpro.domain.immutability.entity-final-fields
    private final String codigo;
}
