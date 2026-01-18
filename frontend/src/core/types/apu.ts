/**
 * Tipos específicos para Análisis de Precios Unitarios (APU).
 * 
 * Define la estructura de un APU que descompone el costo de una partida
 * en sus insumos (Materiales, Mano de Obra, Equipos).
 */

import type { Recurso } from './recursos';

/**
 * Detalle de un recurso dentro de un APU.
 * 
 * Representa la cantidad y precio de un recurso específico
 * utilizado en el análisis de una partida.
 */
export interface DetalleAPU {
  /** ID único del detalle */
  id: string;
  
  /** Referencia al recurso del catálogo */
  recursoId: string;
  
  /** Datos del recurso (para evitar búsquedas) */
  recurso?: Recurso;
  
  /** Cantidad/rendimiento del recurso por unidad de partida */
  rendimiento: number;
  
  /** Precio unitario del recurso (snapshot al momento de agregar) */
  precio: number;
  
  /** Parcial calculado: rendimiento * precio */
  parcial: number;
  
  /** Observaciones opcionales */
  observacion?: string;
}

/**
 * Análisis de Precio Unitario completo.
 * 
 * Descompone el costo de una partida en sus componentes:
 * - Materiales
 * - Mano de Obra
 * - Equipos
 * - Subcontratos
 */
export interface AnalisisUnitario {
  /** ID único del APU */
  id: string;
  
  /** ID de la partida asociada */
  partidaId: string;
  
  /** Rendimiento diario de la partida (opcional) */
  rendimientoDiario?: number;
  
  /** Costo directo total (suma de todos los parciales) */
  costoDirecto: number;
  
  /** Detalles del APU agrupados por tipo */
  detalles: DetalleAPU[];
  
  /** Fecha de creación */
  createdAt?: string;
  
  /** Fecha de última actualización */
  updatedAt?: string;
}

/**
 * DTO para crear/actualizar un detalle de APU.
 */
export interface CrearDetalleAPUCommand {
  partidaId: string;
  recursoId: string;
  rendimiento: number;
  precio: number;
  observacion?: string;
}

/**
 * DTO para crear/actualizar un APU completo.
 */
export interface CrearAPUCommand {
  partidaId: string;
  rendimientoDiario?: number;
  detalles: CrearDetalleAPUCommand[];
}
