/**
 * Servicio para operaciones relacionadas con Producción (RPC).
 */

import { apiClient } from './api-client';
import type {
  CrearReporteDTO,
  ReporteResponse,
  ReporteResumenResponse,
} from '@/core/types/produccion';

/**
 * Servicio de Producción.
 */
export class ProduccionService {
  /**
   * Crea un nuevo reporte de producción (PENDIENTE).
   */
  static async crear(proyectoId: string, data: CrearReporteDTO): Promise<ReporteResponse> {
    return apiClient.post<ReporteResponse>(`/proyectos/${proyectoId}/produccion`, data);
  }

  /**
   * Lista reportes de producción del proyecto (resumido).
   */
  static async listar(proyectoId: string): Promise<ReporteResumenResponse[]> {
    return apiClient.get<ReporteResumenResponse[]>(`/proyectos/${proyectoId}/produccion`);
  }

  /**
   * Obtiene el detalle completo de un reporte.
   */
  static async obtenerDetalle(reporteId: string): Promise<ReporteResponse> {
    return apiClient.get<ReporteResponse>(`/produccion/${reporteId}`);
  }

  /**
   * Aprueba un reporte (PATCH).
   */
  static async aprobar(reporteId: string): Promise<ReporteResponse> {
    return apiClient.patch<ReporteResponse>(`/produccion/${reporteId}/aprobar`);
  }

  /**
   * Rechaza un reporte (PATCH).
   */
  static async rechazar(reporteId: string, motivo: string): Promise<ReporteResponse> {
    return apiClient.patch<ReporteResponse>(`/produccion/${reporteId}/rechazar`, { motivo });
  }
}
