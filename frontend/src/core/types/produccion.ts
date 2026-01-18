/**
 * Tipos específicos para Producción (RPC).
 */

export type EstadoReporte = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';

export interface DetalleItemDTO {
  partidaId: string;
  cantidad: number;
}

export interface CrearReporteDTO {
  fechaReporte: string;
  items: DetalleItemDTO[];
}

export interface DetalleItemResponse {
  partidaId: string;
  partidaDescripcion?: string | null;
  cantidad: number;
}

export interface ReporteResponse {
  id: string;
  fechaReporte: string;
  estado: EstadoReporte;
  responsableId: string;
  responsableNombre?: string | null;
  aprobadorId?: string | null;
  aprobadorNombre?: string | null;
  comentario?: string | null;
  ubicacionGps?: string | null;
  items: DetalleItemResponse[];
}

export interface ReporteResumenResponse {
  id: string;
  fechaReporte: string;
  estado: EstadoReporte;
  responsableId: string;
  responsableNombre?: string | null;
  aprobadorId?: string | null;
  aprobadorNombre?: string | null;
  totalItems: number;
}
