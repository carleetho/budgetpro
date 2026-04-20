/**
 * Servicio para operaciones relacionadas con Recursos (Catálogo).
 * Datos desde `GET /api/v1/recursos` y `GET /api/v1/recursos/{id}`.
 */

import type { Recurso, TipoRecurso } from "@/core/types/recursos";
import { apiClient } from "@/services/api-client";

/** Respuesta REST alineada a {@code RecursoResponse} (snapshot mínimo). */
interface RecursoResponseDto {
  id: string;
  nombre: string;
  tipo: string;
  estado: string;
}

const TIPOS_VALIDOS: TipoRecurso[] = ["MATERIAL", "MANO_DE_OBRA", "EQUIPO", "SUBCONTRATO"];

function parseTipo(raw: string): TipoRecurso {
  const upper = raw?.toUpperCase?.() ?? "";
  return TIPOS_VALIDOS.includes(upper as TipoRecurso) ? (upper as TipoRecurso) : "MATERIAL";
}

function mapResponseToRecurso(r: RecursoResponseDto): Recurso {
  return {
    id: r.id,
    nombre: r.nombre,
    tipo: parseTipo(r.tipo),
    unidad: "—",
    precioBase: 0,
  };
}

/** Extrae status de errores lanzados por apiClient.handleResponse */
function httpStatus(e: unknown): number | undefined {
  if (typeof e === "object" && e !== null && "status" in e) {
    const s = (e as { status?: unknown }).status;
    return typeof s === "number" ? s : undefined;
  }
  return undefined;
}

export class RecursosService {
  /** `GET /api/v1/recursos` */
  static async listar(): Promise<Recurso[]> {
    const rows = await apiClient.get<RecursoResponseDto[]>("/recursos");
    return rows.map(mapResponseToRecurso);
  }

  /** Filtrado en cliente (el backend expone listado completo). */
  static async buscar(termino: string, tipo?: TipoRecurso): Promise<Recurso[]> {
    const todos = await this.listar();

    let filtrados = todos;

    if (tipo) {
      filtrados = filtrados.filter((r) => r.tipo === tipo);
    }

    if (termino.trim()) {
      const terminoLower = termino.toLowerCase();
      filtrados = filtrados.filter(
        (r) =>
          r.nombre.toLowerCase().includes(terminoLower) ||
          r.codigo?.toLowerCase().includes(terminoLower) ||
          r.descripcion?.toLowerCase().includes(terminoLower)
      );
    }

    return filtrados;
  }

  /** `GET /api/v1/recursos/{id}` */
  static async obtenerPorId(id: string): Promise<Recurso | null> {
    try {
      const r = await apiClient.get<RecursoResponseDto>(`/recursos/${id}`);
      return mapResponseToRecurso(r);
    } catch (e: unknown) {
      if (httpStatus(e) === 404) {
        return null;
      }
      throw e;
    }
  }
}
