/**
 * FSR / configuración laboral extendida — perímetro RRHH (no usar `PUT /api/v1/configuracion-laboral` legacy en UI).
 */

import { apiClient } from '@/services/api-client';
import type {
  ConfiguracionLaboralExtendidaResponseDto,
  ConfigurarLaboralExtendidaRequestDto,
  HistorialFSRResponseDto,
} from '@/core/types/presupuesto-contract';

const BASE = '/rrhh/configuracion';

export class RrhhFsrConfigService {
  /** `PUT /api/v1/rrhh/configuracion/proyectos/{proyectoId}` */
  static async configurarPorProyecto(
    proyectoId: string,
    body: ConfigurarLaboralExtendidaRequestDto
  ): Promise<ConfiguracionLaboralExtendidaResponseDto> {
    return apiClient.put<ConfiguracionLaboralExtendidaResponseDto>(
      `${BASE}/proyectos/${proyectoId}`,
      body
    );
  }

  /** `GET /api/v1/rrhh/configuracion/proyectos/{proyectoId}/historial` */
  static async consultarHistorial(
    proyectoId: string,
    fechaInicio: string,
    fechaFin: string
  ): Promise<HistorialFSRResponseDto> {
    return apiClient.get<HistorialFSRResponseDto>(`${BASE}/proyectos/${proyectoId}/historial`, {
      params: { fechaInicio, fechaFin },
    });
  }
}
