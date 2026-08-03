# Estrategia de validación UI ↔ Backend (REQ-47)

> **Alcance:** Phase 1 (Proyecto, Presupuesto, Partida, Recurso, Estimación).  
> **Autoridad:** el backend es la fuente de verdad; la UI solo anticipa feedback.  
> **Evidencia as-built:** DTOs Jakarta, `ErrorResponses`, `BudgetProApiError`, React Hook Form + Zod.

## 1. Principio

| Capa | Rol | Qué puede hacer | Qué no debe hacer |
|------|-----|-----------------|-------------------|
| **UI** | Feedback inmediato | Required, formatos, rangos, UUID shape, UX | Enforzar REGLA-XXX, FK existence, state machines, cálculos financieros |
| **Backend** | Autoridad | Bean Validation + dominio + seguridad + integridad | Confiar en que la UI ya validó |

Reglas de negocio (`REGLA-XXX`, invariantes de notebook) **solo en backend**. Si la UI las “replica”, es opcional y no sustituye la respuesta del API.

## 2. Responsabilidades UI

Usar **Zod + React Hook Form** (`@hookform/resolvers/zod`), como en `frontend/src/components/landing/DemoForm.tsx`.

- Campos obligatorios (`min(1)`, `uuid()`, etc.).
- Formato: email, fechas ISO, números, UUID.
- Rangos: no negativos, longitudes, `periodoFin >= periodoInicio` (UX).
- Patrones regex de formato (códigos WBS simples, etc.).
- Reglas UX no críticas (p. ej. avisar metrado vacío en títulos).

Tras un fallo de backend: **no limpiar el formulario**; mapear `fieldErrors` a `setError` / mensajes inline y mostrar toast con `businessCode` cuando aplique.

## 3. Responsabilidades backend

- Jakarta Bean Validation en request DTOs (`@Valid` / `@Validated` en controllers).
- Anotaciones típicas: `@NotNull`, `@NotBlank`, `@DecimalMin`, y anidación `@Valid` en listas.
- Reglas de dominio / REGLA-XXX (existencia de agregados, presupuesto congelado, secuencias, etc.).
- Transiciones de estado, autorizaciones JWT, integridad referencial y cálculos financieros.

Contrato de error canónico (`backend/.../rest/error/ErrorResponses.java`):

- `ErrorResponse`: `timestamp`, `status`, `error`, `message`, `traceId`, `details?`
- `ValidationErrorResponse`: `timestamp`, `status`, `error`, `fieldErrors`, `traceId`

Ejemplo as-built (`RecursoControllerAdvice`): validación Bean → HTTP 400 con `error: "BAD_REQUEST"` y mapa `fieldErrors`; conflicto de negocio → HTTP 409 con `error: "CONFLICT"`.

El frontend ya parsea esto en `frontend/src/lib/budget-pro-api-error.ts` (`BudgetProApiError`: `businessCode`, `fieldErrors`, `message`, `traceId` vía raw).

## 4. Ejemplos Phase 1

### 4.1 Proyecto

| UI (Zod) | Backend |
|----------|---------|
| `nombre` no vacío | `CrearProyectoRequest`: `@NotBlank` nombre (**REGLA-097**); `ubicacion` opcional |

### 4.2 Presupuesto

| UI (Zod) | Backend |
|----------|---------|
| `proyectoId` UUID válido; `nombre` no vacío | `CrearPresupuestoRequest`: `@NotNull` `proyectoId`, `@NotBlank` `nombre` (**REGLA-098** en DTO). Existencia del proyecto y reglas de congelado/aprobación (p. ej. invariantes P-*) **solo dominio**. |

```typescript
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

const presupuestoSchema = z.object({
  proyectoId: z.string().uuid("ID de proyecto inválido"),
  nombre: z.string().min(1, "Nombre es obligatorio"),
});

type PresupuestoForm = z.infer<typeof presupuestoSchema>;

const form = useForm<PresupuestoForm>({
  resolver: zodResolver(presupuestoSchema),
  // Mantener valores tras submit fallido (default RHF)
});
```

### 4.3 Partida

| UI (Zod) | Backend |
|----------|---------|
| `presupuestoId` UUID; `item` y `descripcion` no vacíos; `metrado >= 0` si se envía; `nivel` entero | `CrearPartidaRequest`: `@NotNull` presupuestoId/nivel; `@NotBlank` item/descripcion; `@DecimalMin("0.0")` metrado (**REGLA-096**). Jerarquía/`padreId` y presupuesto no editable si congelado → dominio. |

### 4.4 Recurso

| UI (Zod) | Backend |
|----------|---------|
| `nombre`, `tipo`, `unidadBase` no vacíos | `CrearRecursoRequest`: `@NotBlank` en los tres (**REGLA-099**); atributos default (**REGLA-144**). Duplicados → 409 `CONFLICT` (`RecursoDuplicadoException`). |

### 4.5 Estimación

| UI (Zod) | Backend |
|----------|---------|
| Fechas obligatorias; `periodoFin >= periodoInicio`; anticipo/retención ≥ 0; detalles con `partidaId`, cantidades y PU ≥ 0 | `GenerarEstimacionRequest`: `@NotNull` fechas y detalles; `@DecimalMin("0.0")` anticipo (**REGLA-087**), avance y PU (**REGLA-088**). Aprobación secuencial, billetera, límites de volumen → **solo backend** (invariantes ES-* / REGLA de notebook). |

## 5. Manejo de errores en frontend

Patrón alineado a `apiClient` + `BudgetProApiError` (as-built en workspace de presupuestos):

```typescript
import { apiClient } from "@/services/api-client";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";
import { toast } from "sonner";
import type { UseFormSetError, FieldValues, Path } from "react-hook-form";

async function submitPresupuesto<T extends FieldValues>(
  data: T,
  setError: UseFormSetError<T>
) {
  try {
    await apiClient.post("/presupuestos", data);
  } catch (e) {
    if (!BudgetProApiError.isInstance(e)) {
      toast.error("Error inesperado al guardar.");
      return;
    }

    // Validación Bean → fieldErrors (400)
    if (e.fieldErrors && Object.keys(e.fieldErrors).length > 0) {
      for (const [field, message] of Object.entries(e.fieldErrors)) {
        setError(field as Path<T>, { type: "server", message });
      }
      toast.error("Revisa los campos marcados.");
      return;
    }

    // Negocio / conflicto / regla (409, 412, 422, o message con código)
    toast.error(`[${e.businessCode}] ${e.message}`);
    // No resetear el form: el usuario corrige y reenvía
  }
}
```

Notas:

- Códigos observados en advice actuales incluyen `BAD_REQUEST` y `CONFLICT`; `BudgetProApiError.businessCode` lee el campo JSON `error` (o `code` defensivo).
- Conservar `traceId` del cuerpo (si se expone en `raw`) para soporte.
- HTTP relevantes parseados: **400, 409, 412, 422**.

## 6. Preservación de input

1. No llamar `reset()` tras error de API.
2. Preferir `mode: "onBlur"` o `"onSubmit"` en RHF para no borrar estado.
3. Errores de campo vía `setError`; errores globales vía toast.
4. Deshabilitar submit solo mientras `isSubmitting`; rehabilitar automáticamente al fallar.

## 7. Checklist por operación Phase 1

| Operación | UI mínima | Backend (DTO / dominio) |
|-----------|-----------|-------------------------|
| Crear proyecto | nombre | `@NotBlank` + REGLA-097 |
| Crear presupuesto | proyectoId UUID, nombre | CrearPresupuestoRequest + existencia/congelado |
| Crear partida | presupuestoId, item, descripción, nivel, metrado≥0 | CrearPartidaRequest + árbol/congelado |
| Crear recurso | nombre, tipo, unidadBase | CrearRecursoRequest + unicidad |
| Generar estimación | fechas, periodos coherentes, detalles ≥0 | GenerarEstimacionRequest + ES-*/billetera |

## 8. Fuera de alcance (otras tasks REQ-47)

- Anotaciones OpenAPI en controllers (Tasks 2 / 9).
- Guías Phase 2/3 y AI Safety (Tasks 6, 10, 11).
- Generación de tipos TypeScript desde OpenAPI (Task 12).
- Implementar validadores nuevos en UI o dominio (solo documentación aquí).
