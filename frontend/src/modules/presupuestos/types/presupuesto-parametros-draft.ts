/**
 * Borrador local de parámetros económicos del presupuesto hasta que exista PATCH en API.
 * Clave: `budgetpro:cabecera-draft:{presupuestoId}` (histórico; no renombrar para no invalidar borradores).
 */

export type TipoApuCabecera = "EDIFICACIONES" | "CARRETERAS";

export interface PresupuestoParametrosDraft {
  nombrePresupuesto: string;
  subpresupuestoNombre: string;
  catalogoGrupo: string;
  catalogoSubgrupo: string;
  decimalesMetrados: number;
  tipoApu: TipoApuCabecera;
  requiereFormulaPolinomica: boolean;
  distritoTexto: string;
  fechaElaboracion: string;
  plazoDias: number;
  jornadaDiaria: number;
  monedaBase: string;
  dobleMoneda: boolean;
  monedaAlterna: string;
  factorCambio: string;
  presupuestoBaseReferencia: string;
  historicoFechaLugar: { fecha: string; lugar: string }[];
  tieneLogotipo: boolean;
  preciosContextoInvalidado: boolean;
}

/** @deprecated usar PresupuestoParametrosDraft */
export type PresupuestoCabeceraDraft = PresupuestoParametrosDraft;

const STORAGE_PREFIX = "budgetpro:cabecera-draft:";

export function parametrosDraftStorageKey(presupuestoId: string): string {
  return `${STORAGE_PREFIX}${presupuestoId}`;
}

export function loadParametrosDraft(presupuestoId: string): PresupuestoParametrosDraft | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    const raw = window.localStorage.getItem(parametrosDraftStorageKey(presupuestoId));
    if (!raw) {
      return null;
    }
    return JSON.parse(raw) as PresupuestoParametrosDraft;
  } catch {
    return null;
  }
}

/** Alias retrocompatible */
export const loadCabeceraDraft = loadParametrosDraft;

export function saveParametrosDraft(presupuestoId: string, draft: PresupuestoParametrosDraft): void {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(parametrosDraftStorageKey(presupuestoId), JSON.stringify(draft));
}

export const saveCabeceraDraft = saveParametrosDraft;

export function defaultParametrosDraft(partial: Partial<PresupuestoParametrosDraft>): PresupuestoParametrosDraft {
  return {
    nombrePresupuesto: "",
    subpresupuestoNombre: "Principal",
    catalogoGrupo: "",
    catalogoSubgrupo: "",
    decimalesMetrados: 4,
    tipoApu: "EDIFICACIONES",
    requiereFormulaPolinomica: false,
    distritoTexto: "",
    fechaElaboracion: new Date().toISOString().slice(0, 10),
    plazoDias: 180,
    jornadaDiaria: 8,
    monedaBase: "PEN",
    dobleMoneda: false,
    monedaAlterna: "USD",
    factorCambio: "",
    presupuestoBaseReferencia: "",
    historicoFechaLugar: [],
    tieneLogotipo: false,
    preciosContextoInvalidado: false,
    ...partial,
  };
}

export const defaultCabeceraDraft = defaultParametrosDraft;

export function cabeceraDraftStorageKey(presupuestoId: string): string {
  return parametrosDraftStorageKey(presupuestoId);
}
