package com.budgetpro.domain.finanzas.estimacion.model;

/**
 * Enum que representa el estado de una Estimación.
 * 
 * Estados posibles: - BORRADOR: Estimación en proceso de creación - APROBADA:
 * Estimación autorizada, lista para facturar - PAGADA: Estimación pagada,
 * ingreso registrado en billetera
 */
public enum EstadoEstimacion {
    /**
     * Estado inicial cuando se crea la estimación. Permite modificaciones en los
     * items y montos.
     */
    BORRADOR,

    /**
     * La estimación ha sido revisada y aprobada. Ya no se pueden modificar los
     * items, pero aún no se ha facturado.
     */
    APROBADA,

    /**
     * La estimación ha sido procesada para facturación. Estado final exitoso.
     */
    FACTURADA,

    /**
     * La estimación ha sido cancelada o invalidada. Estado final fallido.
     */
    ANULADA
}
