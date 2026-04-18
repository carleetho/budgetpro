/**
 * Contratos REST canónicos — Presupuesto, Partidas WBS, listados y FSR (RRHH).
 * Alineado a DTOs Java (`PresupuestoResponse`, `PartidaResponse`, `WbsNodeResponse`, etc.).
 */

export type EstadoPresupuestoRest = "BORRADOR" | "CONGELADO" | "INVALIDADO";

/** `GET /api/v1/presupuestos/{id}` */
export interface PresupuestoResponseDto {
  id: string;
  proyectoId: string;
  nombre: string;
  estado: EstadoPresupuestoRest;
  esContractual: boolean;
  costoTotal: number;
  precioVenta: number;
  version: number;
  createdAt?: string;
  updatedAt?: string;
}

/** `GET /api/v1/presupuestos?tenantId=&proyectoId=` */
export interface ListarPresupuestosPaginadosResponseDto {
  content: PresupuestoResponseDto[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/** `GET /api/v1/partidas/wbs?presupuestoId=` — nodo anidado */
export interface PartidaResponseDto {
  id: string;
  presupuestoId: string;
  padreId: string | null;
  item: string;
  descripcion: string;
  unidad: string | null;
  /** Metrado contractual base expuesto por la API (inmutable si presupuesto CONGELADO — REGLA-047). */
  metrado: number | null;
  nivel: number;
  version: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface WbsNodeResponseDto {
  partida: PartidaResponseDto;
  children: WbsNodeResponseDto[] | null;
}

/** `PUT /api/v1/rrhh/configuracion/proyectos/{proyectoId}` cuerpo (ConfigurarLaboralExtendidaRequest). */
export interface ConfigurarLaboralExtendidaRequestDto {
  fechaInicio: string;
  diasAguinaldo: number;
  diasVacaciones: number;
  porcentajeSeguridadSocial: number;
  diasNoTrabajados: number;
  diasLaborablesAno: number;
  factorHorasExtras: number;
  factorTurnoNocturno: number;
  factorRiesgo: number;
  factorRegional: number;
}

/** Respuesta extendida FSR (ConfiguracionLaboralExtendidaResponse). */
export interface ConfiguracionLaboralExtendidaResponseDto {
  id: string;
  proyectoId: string | { value: string } | null;
  fechaInicio: string;
  fechaFin: string | null;
  activa: boolean;
  fsrBase: number;
  fsrExtendido: number;
  diasAguinaldo: number;
  diasVacaciones: number;
  porcentajeSeguridadSocial: number;
  diasNoTrabajados: number;
  diasLaborablesAno: number;
  factorHorasExtras: number;
  factorTurnoNocturno: number;
  factorRiesgo: number;
  factorRegional: number;
}

export interface HistorialFSRResponseDto {
  historial: ConfiguracionLaboralExtendidaResponseDto[];
}

/**
 * [P-01 / REGLA-046]: baseline inmutable = presupuesto `CONGELADO`.
 * `INVALIDADO` no forma parte del texto de P-01, pero la UI lo trata también como no editable (defensivo).
 */
export function isPresupuestoWbsReadOnly(estado: EstadoPresupuestoRest): boolean {
  return estado === "CONGELADO" || estado === "INVALIDADO";
}

/** [P-03]: hoja WBS = sin hijos o lista vacía. */
export function isWbsLeafNode(node: WbsNodeResponseDto): boolean {
  return !node.children || node.children.length === 0;
}

/**
 * [REGLA-110]: un solo presupuesto ACTIVO por proyecto (BORRADOR o CONGELADO).
 * Nota: con paginación, el consumidor debe revisar todas las páginas si `totalElements > size`.
 */
export function presupuestoEstaActivo(estado: EstadoPresupuestoRest): boolean {
  return estado === "BORRADOR" || estado === "CONGELADO";
}

export function listadoTienePresupuestoActivo(
  page: ListarPresupuestosPaginadosResponseDto
): boolean {
  return page.content.some((p) => presupuestoEstaActivo(p.estado));
}
