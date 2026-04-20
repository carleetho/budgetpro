/**
 * Cliente REST para Análisis de Precio Unitario por partida.
 */

import type { ApuResponseDto, CrearApuRequestDto } from "@/core/types/apu-contract";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";
import { apiClient } from "@/services/api-client";

function isApuNotFoundError(e: unknown): boolean {
  return (
    BudgetProApiError.isInstance(e) &&
    e.status === 400 &&
    /APU no encontrado/i.test(e.message)
  );
}

export class ApuApiService {
  /**
   * `GET /api/v1/partidas/{partidaId}/apu`
   * Si no hay APU, el backend responde 400 INVALID_ARGUMENT ("APU no encontrado…").
   */
  static async obtenerPorPartidaOpcional(partidaId: string): Promise<ApuResponseDto | null> {
    try {
      return await apiClient.get<ApuResponseDto>(`/partidas/${partidaId}/apu`);
    } catch (e: unknown) {
      if (isApuNotFoundError(e)) {
        return null;
      }
      const status =
        typeof e === "object" && e !== null && "status" in e
          ? Number((e as { status?: number }).status)
          : NaN;
      if (status === 404) {
        return null;
      }
      throw e;
    }
  }

  /** `POST /api/v1/partidas/{partidaId}/apu` */
  static async crear(partidaId: string, body: CrearApuRequestDto): Promise<ApuResponseDto> {
    return apiClient.post<ApuResponseDto>(`/partidas/${partidaId}/apu`, body);
  }
}
