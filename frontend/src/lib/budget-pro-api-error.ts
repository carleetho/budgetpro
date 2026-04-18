/**
 * Errores REST alineados a {@code ErrorResponses} del backend.
 * El cuerpo unificado usa el campo JSON {@code error} como código de negocio;
 * se acepta también {@code code} por compatibilidad defensiva.
 */

export type BudgetProValidationBody = {
  status: number;
  error: string;
  fieldErrors: Record<string, string>;
  traceId?: string;
  timestamp?: string;
};

export type BudgetProErrorBody = {
  status: number;
  error?: string;
  code?: string;
  message?: string;
  traceId?: string;
  timestamp?: string;
  details?: Record<string, unknown>;
};

export class BudgetProApiError extends Error {
  readonly status: number;
  /** Código de negocio (p. ej. INVALID_ARGUMENT, BUSINESS_RULE). */
  readonly businessCode: string;
  readonly fieldErrors?: Record<string, string>;
  readonly raw?: unknown;

  constructor(params: {
    status: number;
    businessCode: string;
    message: string;
    fieldErrors?: Record<string, string>;
    raw?: unknown;
  }) {
    super(params.message);
    this.name = "BudgetProApiError";
    this.status = params.status;
    this.businessCode = params.businessCode;
    this.fieldErrors = params.fieldErrors;
    this.raw = params.raw;
  }

  static isInstance(e: unknown): e is BudgetProApiError {
    return e instanceof BudgetProApiError;
  }
}

function isRecord(v: unknown): v is Record<string, unknown> {
  return typeof v === "object" && v !== null && !Array.isArray(v);
}

function pickBusinessCode(body: Record<string, unknown>): string {
  const fromError = body.error;
  const fromCode = body.code;
  if (typeof fromError === "string" && fromError.length > 0) return fromError;
  if (typeof fromCode === "string" && fromCode.length > 0) return fromCode;
  return "UNKNOWN";
}

function isValidationShape(body: Record<string, unknown>): body is BudgetProValidationBody {
  return (
    typeof body.status === "number" &&
    typeof body.error === "string" &&
    isRecord(body.fieldErrors)
  );
}

/**
 * Intenta interpretar el cuerpo JSON como error canónico (400, 409, 412, 422).
 * Devuelve null si no hay JSON reconocible.
 */
export function tryParseBudgetProApiError(
  status: number,
  text: string
): BudgetProApiError | null {
  if (![400, 409, 412, 422].includes(status) || !text) {
    return null;
  }
  let parsed: unknown;
  try {
    parsed = JSON.parse(text);
  } catch {
    return null;
  }
  if (!isRecord(parsed)) return null;

  if (isValidationShape(parsed)) {
    const firstMsg =
      Object.values(parsed.fieldErrors).find((m) => typeof m === "string" && m.length > 0) ??
      parsed.error;
    return new BudgetProApiError({
      status,
      businessCode: parsed.error,
      message: typeof firstMsg === "string" ? firstMsg : "Error de validación",
      fieldErrors: parsed.fieldErrors,
      raw: parsed,
    });
  }

  const code = pickBusinessCode(parsed);
  const message =
    typeof parsed.message === "string" && parsed.message.length > 0
      ? parsed.message
      : `HTTP ${status}`;

  if (
    typeof parsed.status === "number" ||
    typeof parsed.error === "string" ||
    typeof parsed.code === "string" ||
    typeof parsed.message === "string"
  ) {
    return new BudgetProApiError({
      status,
      businessCode: code,
      message,
      raw: parsed,
    });
  }

  return null;
}
