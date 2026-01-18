/**
 * Servicio para operaciones relacionadas con Estimaciones.
 */

import { apiClient } from './api-client';
import type { Estimacion } from '@/core/types';

/**
 * Servicio de Estimaciones.
 */
export class EstimacionService {
  /**
   * Genera una nueva estimación.
   */
  static async generar(proyectoId: string, data: {
    fechaCorte: string;
    periodoInicio: string;
    periodoFin: string;
    detalles: Array<{
      partidaId: string;
      cantidadAvance: number;
      precioUnitario: number;
    }>;
    porcentajeAnticipo?: number;
    porcentajeRetencionFondoGarantia?: number;
  }): Promise<Estimacion> {
    return apiClient.post<Estimacion>(`/proyectos/${proyectoId}/estimaciones`, data);
  }

  /**
   * Aprueba una estimación.
   */
  static async aprobar(id: string): Promise<void> {
    return apiClient.put<void>(`/proyectos/estimaciones/${id}/aprobar`);
  }
}
