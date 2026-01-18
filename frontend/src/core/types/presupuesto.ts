/**
 * Tipos específicos para el módulo de Presupuesto.
 * 
 * Define la estructura jerárquica de Partidas (WBS - Work Breakdown Structure).
 */

/**
 * Nivel jerárquico en el presupuesto.
 */
export type NivelPresupuesto = 'CAPITULO' | 'SUBCAPITULO' | 'PARTIDA';

/**
 * Item del presupuesto (nodo del árbol jerárquico).
 * 
 * Puede ser un Capítulo, Subcapítulo o Partida.
 * Los capítulos y subcapítulos pueden tener hijos.
 * Las partidas son hojas del árbol (no tienen hijos).
 */
export interface ItemPresupuesto {
  /** ID único del item */
  id: string;
  
  /** Código jerárquico (ej: "1.01", "2.03.05") */
  codigo: string;
  
  /** Descripción del item */
  descripcion: string;
  
  /** Nivel jerárquico */
  nivel: NivelPresupuesto;
  
  /** Unidad de medida (solo para PARTIDA) */
  unidad?: string;
  
  /** Cantidad/metrado (solo para PARTIDA) */
  metrado?: number;
  
  /** Precio unitario (solo para PARTIDA) */
  precioUnitario?: number;
  
  /** Parcial calculado (metrado * precioUnitario) */
  parcial?: number;
  
  /** ID del item padre (null para raíz) */
  padreId?: string | null;
  
  /** Items hijos (estructura recursiva) */
  hijos?: ItemPresupuesto[];
}

/**
 * DTO para crear/editar un item del presupuesto.
 */
export interface CrearItemPresupuestoCommand {
  proyectoId: string;
  padreId?: string | null;
  codigo: string;
  descripcion: string;
  nivel: NivelPresupuesto;
  unidad?: string;
  metrado?: number;
  precioUnitario?: number;
}
