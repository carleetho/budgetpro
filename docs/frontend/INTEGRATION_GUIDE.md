# Guía de integración Frontend ↔ API (Phase 1) — REQ-47

> **Stack FE as-built:** Next.js 16 (App Router), React 19, TypeScript, Zod + React Hook Form, TanStack Table.  
> **Cliente HTTP:** `frontend/src/services/api-client.ts` + `BudgetProApiError`.  
> **Base URL:** `NEXT_PUBLIC_API_BASE_URL` → default `http://localhost:8080/api/v1`.  
> **Docs relacionadas:** `VALIDATION_STRATEGY.md`, `AI_SAFETY_GUIDE.md`, Swagger `http://localhost:8080/swagger-ui.html`.

## 1. Quick start

```bash
# Frontend
cd frontend && cp .env.example .env.local  # si existe; o exporta:
# NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1

# Backend
cd backend && ./mvnw spring-boot:run
# OpenAPI: http://localhost:8080/v3/api-docs
```

Primera llamada autenticada (patrón del repo):

```typescript
import { apiClient } from "@/services/api-client";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

// Tras login: localStorage.setItem("auth_token", token)
const page = await apiClient.get<{ content: unknown[]; totalElements: number }>(
  "/presupuestos",
  { tenantId, proyectoId, page: 0, size: 20 }
);
```

`apiClient` antepone `API_BASE_URL` y adjunta `Authorization: Bearer` desde `localStorage.auth_token`.

## 2. Autenticación (JWT)

| Endpoint | Método | Auth | Notas |
|----------|--------|------|-------|
| `/auth/login` | POST | No | Rate limit `auth-login` (5 / 15 min por IP) |
| `/auth/register` | POST | No | 409 si email duplicado |
| `/auth/me` | GET | Bearer | Perfil actual |

```typescript
type AuthResponse = {
  token: string;
  userId: string;
  email: string;
  rol: string;
};

const auth = await apiClient.post<AuthResponse>("/auth/login", {
  email: "user@example.com",
  password: "secret",
});
localStorage.setItem("auth_token", auth.token);
// Opcional: localStorage.setItem("auth_user", JSON.stringify({ email: auth.email, rol: auth.rol }));
```

- Tokens de acceso ~24h (`jwt.expiration-hours`). Hoy: almacenar en `localStorage` (as-built); preferible a medio plazo: memoria + refresh corto.
- En **401**, `apiClient` limpia token/usuario y redirige a `/login`.
- Logout: `localStorage.removeItem("auth_token")` (+ `auth_user`) y navegar a `/login`.
- Rutas protegidas: layout/guard que compruebe token o llame `/auth/me`.

## 3. Cliente HTTP (as-built)

No reinventar un segundo cliente. Usar:

| Pieza | Ruta |
|-------|------|
| Cliente | `frontend/src/services/api-client.ts` |
| Errores | `frontend/src/lib/budget-pro-api-error.ts` |
| Env | `frontend/src/core/config/env.ts` → `API_BASE_URL` |

Métodos: `get`, `post`, `put`, `delete` (paths relativos a `/api/v1`).  
204 No Content → `undefined`.  
Errores canónicos (400/409/412/422) → `BudgetProApiError` con `businessCode`, `message`, `fieldErrors?`.

## 4. Presupuestos

Tag OpenAPI: **Presupuestos**. Canónico: `PRESUPUESTO_MODULE_CANONICAL.md`.

| Operación | HTTP | Reglas / notas |
|-----------|------|----------------|
| Listar paginado | `GET /presupuestos?tenantId&proyectoId&page&size` | size 1–100 |
| Crear | `POST /presupuestos` | Body `{ proyectoId, nombre }` — **REGLA-098** |
| Consultar | `GET /presupuestos/{id}` | |
| Aprobar / congelar | `POST /presupuestos/{id}/aprobar` | → CONGELADO; **P-01** no modificar congelado |
| Control costos | `GET /presupuestos/{id}/control-costos` | Plan vs Real |
| Explosión insumos | `GET /presupuestos/{id}/explosion-insumos` | Partidas hoja |

```typescript
await apiClient.post("/presupuestos", {
  proyectoId: "...",
  nombre: "Presupuesto base",
});

await apiClient.post(`/presupuestos/${presupuestoId}/aprobar`); // 204
```

UI existente: `frontend/src/modules/presupuestos/`, workspace bajo `/proyectos/[id]/presupuestos/...`.

## 5. Estimaciones

Tag OpenAPI: **Estimaciones**. Canónico: `ESTIMACION_MODULE_CANONICAL.md`.

| Operación | HTTP | Reglas |
|-----------|------|--------|
| Generar | `POST /proyectos/{proyectoId}/estimaciones` | REGLA-087/088 en DTO; volumen **REGLA-016** |
| Listar | `GET /proyectos/{proyectoId}/estimaciones` | |
| Obtener | `GET /proyectos/estimaciones/{id}` | |
| Aprobar | `PUT /proyectos/estimaciones/{id}/aprobar` | **ES-01** secuencial; **ES-02** ingreso billetera |

```typescript
await apiClient.post(`/proyectos/${proyectoId}/estimaciones`, {
  fechaCorte: "2026-07-30",
  periodoInicio: "2026-07-01",
  periodoFin: "2026-07-31",
  detalles: [{ partidaId, cantidadAvance: 10, precioUnitario: 100 }],
  porcentajeAnticipo: 0,
  porcentajeRetencionFondoGarantia: 0,
});

await apiClient.put(`/proyectos/estimaciones/${estimacionId}/aprobar`); // 204
```

## 6. Billetera

Tag OpenAPI: **Billetera**. Canónico: `BILLETERA_MODULE_CANONICAL.md`.

| Operación | HTTP | Notas |
|-----------|------|--------|
| Registrar movimiento | `POST /billeteras/{id}/movimientos` | `tipo` INGRESO/EGRESO; moneda uppercased |
| Saldo / historial | `GET .../saldo`, `GET .../movimientos` | `BilleteraQueryController` (consultas) |

La aprobación de estimación (**ES-02**) crea ingreso en dominio; el POST de movimientos es para operaciones explícitas.

```typescript
await apiClient.post(`/billeteras/${billeteraId}/movimientos`, {
  monto: 1500.5,
  moneda: "PEN",
  tipo: "INGRESO",
  referencia: `EST-${estimacionId}`,
  evidenciaUrl: null,
});
```

## 7. Errores

Formato canónico backend: `ErrorResponses` (`error`, `message`, `status`, `traceId`, `fieldErrors?`).

| Código típico (`businessCode`) | HTTP | Acción UI |
|--------------------------------|------|-----------|
| `VALIDATION_ERROR` / `BAD_REQUEST` | 400 | Inline `fieldErrors` + toast |
| `INVALID_ARGUMENT` | 400 | Toast / campo |
| `NOT_FOUND` | 404 | Empty state |
| `ILLEGAL_STATE` / `BUSINESS_RULE` / `CONFLICT` | 409 | Toast con código; no reset form |
| `RATE_LIMIT_EXCEEDED` | 429 | Respetar `Retry-After`; backoff |

```typescript
try {
  await apiClient.post("/presupuestos", body);
} catch (e) {
  if (BudgetProApiError.isInstance(e)) {
    if (e.status === 429) {
      // leer Retry-After del raw si se expone; reintentar después
    }
    toast.error(`[${e.businessCode}] ${e.message}`);
  }
}
```

Detalle de validación Zod vs Bean: `VALIDATION_STRATEGY.md`.

## 8. Tipos desde OpenAPI (recomendado)

Cuando el spec esté exportado (Task 3 / CI):

```bash
# Ejemplo futuro — alinear path del YAML con el artefacto CI
npx openapi-typescript http://localhost:8080/v3/api-docs -o frontend/src/types/api.ts
```

Hasta entonces, tipar DTOs a mano mirando controllers/`Crear*Request` o el JSON de `/v3/api-docs`.

## 9. Rate limits (headers)

Respuestas `/api/**` pueden incluir `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset` (expuestos en CORS).

| Tier | Límite |
|------|--------|
| auth login/register | 5 / 15 min por IP |
| `/api/public/**` | 100 / h por IP |
| API autenticada | 1000 / h por usuario |

**CORS:** el backend permite orígenes en `CORS_ALLOWED_ORIGINS` (default `http://localhost:3000,http://localhost:3001`) y headers `Authorization`, `Content-Type`, `Accept`, `X-Correlation-ID`.

## 10. Checklist de arranque FE

- [ ] Backend up + login OK → token en `localStorage`
- [ ] `GET /auth/me` con Bearer
- [ ] Crear/listar presupuesto del proyecto
- [ ] Probar 400 con body inválido → `fieldErrors`
- [ ] Probar aprobar presupuesto y ver bloqueo post-congelado (P-01)
- [ ] Revisar tags Phase 1 en Swagger

## 11. Fuera de alcance

Phase 2/3 (Compras, RRHH, EVM UI, etc.) → Task 11. Generación automática de tipos en CI → Task 12 / 3.
