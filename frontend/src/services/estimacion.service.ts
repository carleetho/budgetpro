/**
 * Servicio para operaciones relacionadas con Estimaciones.
 */

import { apiClient } from "./api-client";
import type { Estimacion } from "@/core/types";

export class EstimacionService {
  static async generar(
    proyectoId: string,
    data: {
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
    }
  ): Promise<Estimacion> {
    return apiClient.post<Estimacion>(`/proyectos/${proyectoId}/estimaciones`, data);
  }

  static async listar(proyectoId: string): Promise<Estimacion[]> {
    return apiClient.get<Estimacion[]>(`/proyectos/${proyectoId}/estimaciones`);
  }

  static async obtenerPorId(estimacionId: string): Promise<Estimacion> {
    return apiClient.get<Estimacion>(`/proyectos/estimaciones/${estimacionId}`);
  }

  static async aprobar(id: string): Promise<void> {
    return apiClient.put<void>(`/proyectos/estimaciones/${id}/aprobar`);
  }
}
