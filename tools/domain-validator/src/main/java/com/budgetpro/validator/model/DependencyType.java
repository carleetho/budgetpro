package com.budgetpro.validator.model;

/**
 * Tipo de dependencia entre módulos del dominio.
 */
public enum DependencyType {
    /**
     * Dependencia de estado: el módulo requiere que otro módulo esté en un estado específico.
     * Ejemplo: Compras requiere Presupuesto.estado === CONGELADO
     */
    STATE_DEPENDENCY,
    
    /**
     * Dependencia de datos: el módulo requiere entidades o estructuras de datos de otro módulo.
     * Ejemplo: Compra.presupuesto_id → Presupuesto.id
     */
    DATA_DEPENDENCY,
    
    /**
     * Dependencia temporal: el módulo debe ejecutarse o congelarse junto con otro módulo.
     * Ejemplo: Schedule freeze debe ocurrir con Budget freeze
     */
    TEMPORAL_DEPENDENCY,
    
    /**
     * Dependencia de lógica de negocio: el módulo requiere servicios o lógica de otro módulo.
     * Ejemplo: APU debe existir antes de que Partida pueda calcular costos
     */
    BUSINESS_LOGIC
}
