/**
 * Catálogo de Recursos — `GET/POST/PUT /api/v1/recursos`.
 */

import type { CrearRecursoCommand, Recurso, TipoRecurso, TipoRecursoFiltroUi } from "@/core/types/recursos";
import { coincideFiltroTipo } from "@/core/types/recursos";
import { apiClient } from "@/services/api-client";

interface RecursoResponseDto {
  id: string;
  nombre: string;
  tipo: string;
  estado: string;
}

const TIPOS_VALIDOS: TipoRecurso[] = [
  "MATERIAL",
  "MANO_OBRA",
  "EQUIPO",
  "EQUIPO_MAQUINA",
  "EQUIPO_HERRAMIENTA",
  "SUBCONTRATO",
];

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
    precioBase: null,
    estado: r.estado,
  };
}

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

  /** Filtrado en cliente. */
  static async buscar(termino: string, tipoFiltro?: TipoRecursoFiltroUi): Promise<Recurso[]> {
    const todos = await this.listar();
    let filtrados = todos;

    if (tipoFiltro) {
      filtrados = filtrados.filter((r) => coincideFiltroTipo(r.tipo, tipoFiltro));
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

  /** `POST /api/v1/recursos` */
  static async crear(command: CrearRecursoCommand): Promise<Recurso> {
    const body = {
      nombre: command.nombre,
      tipo: command.tipo,
      unidadBase: command.unidadBase,
      atributos: command.atributos ?? {},
      esProvisional: command.esProvisional ?? false,
    };
    const r = await apiClient.post<RecursoResponseDto>("/recursos", body);
    const mapped = mapResponseToRecurso(r);
    return { ...mapped, unidad: command.unidadBase };
  }

  /** `PUT /api/v1/recursos/{id}` */
  static async actualizar(
    id: string,
    data: { nombre?: string; unidadBase?: string; atributos?: Record<string, unknown>; estado?: string }
  ): Promise<Recurso> {
    const r = await apiClient.put<RecursoResponseDto>(`/recursos/${id}`, data);
    return mapResponseToRecurso(r);
  }
}
