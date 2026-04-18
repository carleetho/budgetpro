import { DEFAULT_TENANT_ID } from "@/core/constants/tenancy";

/**
 * Resuelve `tenantId` para llamadas que lo exigen (p. ej. listado paginado de presupuestos).
 * - Si el JWT incluye `tenantId` o `tid`, se usa.
 * - Si no, se usa el tenant por defecto alineado al backend (proyectos creados con `tenant_id` default).
 */
export function getTenantIdForApi(): string {
  if (typeof window === "undefined") {
    return DEFAULT_TENANT_ID;
  }
  const token = localStorage.getItem("auth_token");
  if (!token) {
    return DEFAULT_TENANT_ID;
  }
  const parts = token.split(".");
  if (parts.length < 2) {
    return DEFAULT_TENANT_ID;
  }
  try {
    const b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = b64.padEnd(b64.length + ((4 - (b64.length % 4)) % 4), "=");
    const json = JSON.parse(atob(padded)) as Record<string, unknown>;
    const tid = json.tenantId ?? json.tid;
    if (typeof tid === "string" && tid.length > 0) {
      return tid;
    }
  } catch {
    /* ignorar payload ilegible */
  }
  return DEFAULT_TENANT_ID;
}
