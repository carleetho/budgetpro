/**
 * WBS anidado por presupuesto — sin listado plano ni paginación.
 */

import { apiClient } from '@/services/api-client';
import type { WbsNodeResponseDto } from '@/core/types/presupuesto-contract';

export class PartidasWbsService {
  /** `GET /api/v1/partidas/wbs?presupuestoId={uuid}` */
  static async obtenerArbol(presupuestoId: string): Promise<WbsNodeResponseDto[]> {
    return apiClient.get<WbsNodeResponseDto[]>('/partidas/wbs', {
      params: { presupuestoId },
    });
  }
}
