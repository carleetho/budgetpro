package com.budgetpro.domain.recurso.model;

/**
 * Enum que representa el tipo de recurso en el catálogo compartido.
 * Valores extraídos de la sección "Shared Kernel — Catálogo de Recursos"
 * del documento de modelo de agregados DDD.
 */
public enum TipoRecurso {
    MATERIAL,
    MANO_OBRA,
    /**
     * @deprecated Usar EQUIPO_MAQUINA en su lugar. Mantenido para backward compatibility.
     * Los registros existentes con EQUIPO serán migrados automáticamente a EQUIPO_MAQUINA.
     */
    @Deprecated
    EQUIPO,
    EQUIPO_MAQUINA,
    EQUIPO_HERRAMIENTA,
    SUBCONTRATO
}
