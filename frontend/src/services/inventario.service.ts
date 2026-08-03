/**
 * Inventario por proyecto.
 */

import { apiClient } from "@/services/api-client";

export interface InventarioItemDto {
  id: string;
  proyectoId: string;
  recursoId: string;
  cantidadFisica: number;
  costoPromedio?: number;
  ubicacion?: string;
  ultimaActualizacion?: string;
  version?: number;
}

export class InventarioService {
  static async listarPorProyecto(proyectoId: string): Promise<InventarioItemDto[]> {
    return apiClient.get<InventarioItemDto[]>(`/proyectos/${proyectoId}/inventario`);
  }
}
