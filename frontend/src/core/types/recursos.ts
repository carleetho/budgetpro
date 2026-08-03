/**
 * Tipos del catálogo de Recursos — alineados a `TipoRecurso` (Java) y `RecursoResponse`.
 */

/** Valores del enum backend `com.budgetpro.domain.shared.model.TipoRecurso`. */
export type TipoRecurso =
  | "MATERIAL"
  | "MANO_OBRA"
  | "EQUIPO"
  | "EQUIPO_MAQUINA"
  | "EQUIPO_HERRAMIENTA"
  | "SUBCONTRATO";

/** Filtros de UI (agrupan variantes de equipo). */
export type TipoRecursoFiltroUi =
  | "MATERIAL"
  | "MANO_OBRA"
  | "EQUIPO"
  | "SUBCONTRATO";

export interface Recurso {
  id: string;
  nombre: string;
  tipo: TipoRecurso;
  /** Unidad base si el backend la expuso en atributos; si no, placeholder. */
  unidad: string;
  /** El DTO REST actual no expone precio; el PU se captura al armar el APU. */
  precioBase: number | null;
  estado?: string;
  descripcion?: string;
  codigo?: string;
}

/** Body de `POST /api/v1/recursos` (`CrearRecursoRequest`). */
export interface CrearRecursoCommand {
  nombre: string;
  tipo: TipoRecurso;
  unidadBase: string;
  atributos?: Record<string, unknown>;
  esProvisional?: boolean;
}

export function esTipoEquipo(tipo: TipoRecurso): boolean {
  return tipo === "EQUIPO" || tipo === "EQUIPO_MAQUINA" || tipo === "EQUIPO_HERRAMIENTA";
}

export function coincideFiltroTipo(tipo: TipoRecurso, filtro: TipoRecursoFiltroUi): boolean {
  if (filtro === "EQUIPO") return esTipoEquipo(tipo);
  return tipo === filtro;
}

export function etiquetaTipoRecurso(tipo: TipoRecurso): string {
  switch (tipo) {
    case "MANO_OBRA":
      return "MANO OBRA";
    case "EQUIPO_MAQUINA":
      return "EQUIPO MAQUINA";
    case "EQUIPO_HERRAMIENTA":
      return "EQUIPO HERRAMIENTA";
    default:
      return tipo;
  }
}
