/**
 * Fachada legacy de Presupuesto — delega a APIs reales.
 * El árbol WBS canónico vive en `PartidasWbsService` / workspace.
 */

import { apiClient } from "./api-client";
import type { Presupuesto } from "@/core/types";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import { getTenantIdForApi } from "@/lib/jwt-tenant";
import {
  presupuestoEstaActivo,
  type PresupuestoResponseDto,
} from "@/core/types/presupuesto-contract";

export interface ControlCostosPartidaDto {
  id: string;
  item: string;
  descripcion: string;
  unidad?: string | null;
  nivel?: number | null;
  metrado?: number | null;
  gastoAcumulado?: number | null;
  saldo?: number | null;
  hijos?: ControlCostosPartidaDto[] | null;
}

export interface ControlCostosResponseDto {
  partidas: ControlCostosPartidaDto[];
}

export interface ExplosionInsumoDto {
  recursoId?: string;
  recursoNombre?: string;
  tipo?: string;
  unidad?: string;
  cantidadTotal?: number;
  precioUnitario?: number;
  importeTotal?: number;
}

export interface ExplosionInsumosResponseDto {
  presupuestoId?: string;
  insumos?: ExplosionInsumoDto[];
  [key: string]: unknown;
}

/**
 * Servicio de Presupuestos (compatibilidad con pantallas existentes).
 */
export class PresupuestoService {
  static async crear(data: { proyectoId: string; nombre: string }): Promise<Presupuesto> {
    return apiClient.post<Presupuesto>("/presupuestos", data);
  }

  static async obtenerPorId(id: string): Promise<Presupuesto> {
    return apiClient.get<Presupuesto>(`/presupuestos/${id}`);
  }

  static async aprobar(id: string): Promise<void> {
    return apiClient.post<void>(`/presupuestos/${id}/aprobar`);
  }

  /** `GET /presupuestos/{id}/control-costos` */
  static async obtenerControlCostos(id: string): Promise<ControlCostosResponseDto> {
    return apiClient.get<ControlCostosResponseDto>(`/presupuestos/${id}/control-costos`);
  }

  /** `GET /presupuestos/{id}/explosion-insumos` */
  static async obtenerExplosionInsumos(id: string): Promise<ExplosionInsumosResponseDto> {
    return apiClient.get<ExplosionInsumosResponseDto>(`/presupuestos/${id}/explosion-insumos`);
  }

  /**
   * Primer presupuesto ACTIVO (BORRADOR o CONGELADO) del proyecto.
   * Sustituye el endpoint fantasma `/presupuestos/proyecto/{id}/activo`.
   */
  static async obtenerActivo(proyectoId: string): Promise<PresupuestoResponseDto | null> {
    const todos = await PresupuestoApiService.listarTodosPorProyecto(
      getTenantIdForApi(),
      proyectoId
    );
    return todos.find((p) => presupuestoEstaActivo(p.estado)) ?? null;
  }
}
