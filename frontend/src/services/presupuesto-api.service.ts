/**
 * Superficies REST canónicas del agregado Presupuesto (sin sub-recursos inventados).
 */

import { apiClient } from '@/services/api-client';
import type {
  ListarPresupuestosPaginadosResponseDto,
  PresupuestoResponseDto,
} from '@/core/types/presupuesto-contract';

const BASE = '/presupuestos';

export class PresupuestoApiService {
  /** `GET /api/v1/presupuestos/{id}` */
  static async obtenerPorId(presupuestoId: string): Promise<PresupuestoResponseDto> {
    return apiClient.get<PresupuestoResponseDto>(`${BASE}/${presupuestoId}`);
  }

  /** `POST /api/v1/presupuestos/{id}/aprobar` → 204 sin cuerpo. */
  static async aprobar(presupuestoId: string): Promise<void> {
    return apiClient.post<void>(`${BASE}/${presupuestoId}/aprobar`);
  }

  /** `POST /api/v1/presupuestos` — cuerpo alineado a `CrearPresupuestoRequest`. */
  static async crear(body: { proyectoId: string; nombre: string }): Promise<PresupuestoResponseDto> {
    return apiClient.post<PresupuestoResponseDto>(BASE, body);
  }

  /**
   * `GET /api/v1/presupuestos?tenantId=&proyectoId=&page=&size=`
   * Paginación explícita (backend exige `tenantId` + `proyectoId`).
   * Por defecto: `page=0`, `size=50`.
   */
  static async listarPorProyecto(params: {
    tenantId: string;
    proyectoId: string;
    page?: number;
    size?: number;
  }): Promise<ListarPresupuestosPaginadosResponseDto> {
    return apiClient.get<ListarPresupuestosPaginadosResponseDto>(BASE, {
      params: {
        tenantId: params.tenantId,
        proyectoId: params.proyectoId,
        page: params.page ?? 0,
        size: params.size ?? 50,
      },
    });
  }

  /**
   * Recorre **todas** las páginas (`size` máx. 100 según `@Max` en `PresupuestoController`)
   * para [REGLA-110] sin falsos negativos si el presupuesto ACTIVO quedó fuera de la primera página.
   */
  static async listarTodosPorProyecto(
    tenantId: string,
    proyectoId: string
  ): Promise<PresupuestoResponseDto[]> {
    const pageSize = 100;
    const all: PresupuestoResponseDto[] = [];
    let page = 0;
    let totalElements = Number.POSITIVE_INFINITY;

    while (page * pageSize < totalElements) {
      const res = await this.listarPorProyecto({
        tenantId,
        proyectoId,
        page,
        size: pageSize,
      });
      all.push(...res.content);
      totalElements = res.totalElements;
      page += 1;
      if (res.content.length === 0) {
        break;
      }
    }
    return all;
  }
}
