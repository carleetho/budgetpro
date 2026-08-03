/**
 * Cronograma de obra — `POST/GET /proyectos/{id}/cronograma…`
 */

import { apiClient } from "@/services/api-client";

export interface ActividadCronogramaDto {
  id: string;
  partidaId: string;
  programaObraId?: string;
  fechaInicio: string;
  fechaFin: string;
  duracionDias?: number;
  predecesoras?: string[];
  version?: number;
}

export interface CronogramaResponseDto {
  programaObraId?: string;
  proyectoId: string;
  fechaInicio?: string;
  fechaFinEstimada?: string;
  duracionTotalDias?: number;
  duracionMeses?: number;
  actividades: ActividadCronogramaDto[];
  version?: number;
}

export class CronogramaService {
  static async obtener(proyectoId: string): Promise<CronogramaResponseDto> {
    return apiClient.get<CronogramaResponseDto>(`/proyectos/${proyectoId}/cronograma`);
  }

  static async crearActividad(
    proyectoId: string,
    body: {
      partidaId: string;
      fechaInicio: string;
      fechaFin: string;
      predecesoras?: string[];
    }
  ): Promise<ActividadCronogramaDto> {
    return apiClient.post<ActividadCronogramaDto>(
      `/proyectos/${proyectoId}/cronograma/actividades`,
      body
    );
  }
}
