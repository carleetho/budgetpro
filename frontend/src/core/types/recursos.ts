/**
 * Tipos específicos para el módulo de Recursos (Catálogo).
 * 
 * Define los recursos que pueden ser utilizados en los Análisis de Precios Unitarios (APU).
 */

/**
 * Tipo de recurso disponible en el sistema.
 */
export type TipoRecurso = 'MATERIAL' | 'MANO_DE_OBRA' | 'EQUIPO' | 'SUBCONTRATO';

/**
 * Recurso del catálogo.
 * 
 * Representa un insumo que puede ser utilizado en un APU:
 * - MATERIAL: Cemento, Arena, Ladrillos, etc.
 * - MANO_DE_OBRA: Peón, Operario, Maestro, etc.
 * - EQUIPO: Mezcladora, Vibrador, etc.
 * - SUBCONTRATO: Servicios externos
 */
export interface Recurso {
  /** ID único del recurso */
  id: string;
  
  /** Nombre del recurso */
  nombre: string;
  
  /** Unidad de medida (m³, kg, gal, día, etc.) */
  unidad: string;
  
  /** Precio base del recurso (en USD) */
  precioBase: number;
  
  /** Tipo de recurso */
  tipo: TipoRecurso;
  
  /** Descripción opcional */
  descripcion?: string;
  
  /** Código interno opcional */
  codigo?: string;
}

/**
 * DTO para crear/editar un recurso.
 */
export interface CrearRecursoCommand {
  nombre: string;
  unidad: string;
  precioBase: number;
  tipo: TipoRecurso;
  descripcion?: string;
  codigo?: string;
}
