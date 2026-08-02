/**
 * Definiciones de tipos TypeScript compartidas con el Backend.
 * 
 * Estos tipos deben estar sincronizados con los DTOs del backend
 * para garantizar type-safety end-to-end.
 */

// Re-exportar tipos comunes
export * from './api';
export * from './domain';
export * from './presupuesto';
// Recursos: re-exportar solo lo que no está en domain
export type { Recurso, CrearRecursoCommand, TipoRecursoFiltroUi } from './recursos';
export type { TipoRecurso } from './recursos';
export { esTipoEquipo, coincideFiltroTipo, etiquetaTipoRecurso } from './recursos';
export * from './apu';
export * from './produccion';
