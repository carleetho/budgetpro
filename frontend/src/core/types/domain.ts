/**
 * Tipos de dominio sincronizados con el Backend.
 * 
 * Estos tipos representan las entidades del dominio y deben
 * estar alineados con los DTOs del backend Spring Boot.
 */

/**
 * Estado de un proyecto.
 */
export type EstadoProyecto = 'BORRADOR' | 'ACTIVO' | 'SUSPENDIDO' | 'CERRADO';

/**
 * Estado de un presupuesto.
 */
export type EstadoPresupuesto = 'EN_EDICION' | 'APROBADO';

/**
 * Estado de una estimación.
 */
export type EstadoEstimacion = 'BORRADOR' | 'APROBADA' | 'PAGADA';

/**
 * Tipo de recurso.
 */
export type TipoRecurso = 'MATERIAL' | 'MANO_OBRA' | 'EQUIPO';

/**
 * Proyecto.
 */
export interface Proyecto {
  id: string;
  nombre: string;
  ubicacion?: string;
  estado: EstadoProyecto;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Presupuesto.
 */
export interface Presupuesto {
  id: string;
  proyectoId: string;
  nombre: string;
  estado: EstadoPresupuesto;
  esContractual: boolean;
  costoTotal?: number;
  precioVenta?: number;
}

/**
 * Partida.
 */
export interface Partida {
  id: string;
  presupuestoId: string;
  padreId?: string;
  item: string;
  descripcion: string;
  unidad?: string;
  metrado: number;
  nivel: number;
}

/**
 * Estimación.
 */
export interface Estimacion {
  id: string;
  proyectoId: string;
  numeroEstimacion: number;
  fechaCorte: string;
  periodoInicio: string;
  periodoFin: string;
  montoBruto: number;
  amortizacionAnticipo: number;
  retencionFondoGarantia: number;
  montoNetoPagar: number;
  estado: EstadoEstimacion;
  detalles: DetalleEstimacion[];
}

/**
 * Detalle de Estimación.
 */
export interface DetalleEstimacion {
  id: string;
  partidaId: string;
  cantidadAvance: number;
  precioUnitario: number;
  importe: number;
  acumuladoAnterior: number;
}
