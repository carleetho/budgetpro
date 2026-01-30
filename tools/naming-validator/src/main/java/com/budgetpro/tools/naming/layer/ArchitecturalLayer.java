package com.budgetpro.tools.naming.layer;

/**
 * Representa las capas arquitectónicas identificadas en el proyecto.
 */
public enum ArchitecturalLayer {
    /** Entidad de dominio puro. */
    DOMAIN_ENTITY,
    /** Entidad de persistencia JPA. */
    JPA_ENTITY,
    /** Mapper para conversión entre capas. */
    MAPPER,
    /** Objeto de valor (Value Object). */
    VALUE_OBJECT,
    /** Servicio de dominio. */
    DOMAIN_SERVICE,
    /** Capa no identificada o genérica. */
    UNKNOWN
}
