# Guía de integración Frontend ↔ API (Phase 1 + 2) — REQ-47

> **Stack FE as-built:** Next.js 16 (App Router), React 19, TypeScript, Zod + React Hook Form, TanStack Table.  
> **Cliente HTTP:** `frontend/src/services/api-client.ts` + `BudgetProApiError`.  
> **Base URL:** `NEXT_PUBLIC_API_BASE_URL` → default `http://localhost:8080/api/v1`.  
> **Docs relacionadas:** `VALIDATION_STRATEGY.md`, `AI_SAFETY_GUIDE.md`, `RRHH_UNDER_DEVELOPMENT.md`, Swagger `http://localhost:8080/swagger-ui.html`.  
> **Madurez:** cifras de `AI_SAFETY_GUIDE.md` / radiografía (no inventar %).

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

## 8. Tipos desde OpenAPI

```bash
# 1) Spec real (backend arriba) — el YAML committed puede ser placeholder
./scripts/generate-openapi-spec.sh

# 2) Tipos TS (desde frontend/)
cd frontend && npm run generate:types
# → frontend/src/types/api.ts (commitear tras regenerar)
```

Uso típico (cuando el spec ya no es placeholder):

```typescript
import type { paths } from "@/types/api";

type LoginBody = paths["/api/v1/auth/login"]["post"]["requestBody"]["content"]["application/json"];
```

Hasta regenerar el YAML, tipar DTOs mirando controllers o `http://localhost:8080/v3/api-docs`. No enganchar `generate:types` a cada `next build` mientras el baseline sea placeholder.

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

## 11. Phase 2 — plantilla de aviso

### ⚠️ Medium maturity (XX%)

- Funcionalidad core expuesta por API; **edge cases** pueden estar incompletos en canónico.
- Código FE generado por IA: **revisión humana** antes de merge.
- Fuente de negocio: notebook del módulo + radiografía; no inventar validaciones en cliente.

---

## 12. Cronograma (~60%)

| Endpoint | Método | Notas |
|----------|--------|-------|
| `/proyectos/{proyectoId}/cronograma` | GET | Datos Gantt |
| `/proyectos/{proyectoId}/cronograma/actividades` | POST | Programar/actualizar actividad + predecesoras |

```typescript
await apiClient.get(`/proyectos/${proyectoId}/cronograma`);
await apiClient.post(`/proyectos/${proyectoId}/cronograma/actividades`, {
  partidaId,
  fechaInicio,
  fechaFin,
  predecesoras: [] as string[],
});
```

### ⚠️ Medium maturity (~60%)

- CPM / ciclos / días hábiles: **revisión humana** (C-04). No calcular ruta crítica solo en FE.
- Dependencias complejas pueden fallar o comportarse distinto a lo esperado por IA.

Notebook: `CRONOGRAMA_MODULE_CANONICAL.md`.

---

## 13. EVM (~95% backend; UX FE con cuidado)

| Endpoint | Método | Notas |
|----------|--------|-------|
| `/evm/{proyectoId}` | GET | Snapshot PV/EV/AC/CPI/SPI (`fechaCorte` opcional ISO) |
| `/evm/{proyectoId}/s-curve` | GET | Serie temporal |
| `/evm/{proyectoId}/forecast` | GET | Fecha fin proyectada (SPI) |
| `/evm/{proyectoId}/cerrar-periodo` | POST | Cierre de periodo (E-04) |

```typescript
const snap = await apiClient.get(`/evm/${proyectoId}`, { fechaCorte: "2026-07-01T00:00:00" });
const curve = await apiClient.get(`/evm/${proyectoId}/s-curve`);
```

### ⚠️ High maturity backend / review FE

- Mostrar métricas del API; **no** inventar reglas UI tipo “bloquear si CPI &lt; 1”.
- Escenarios de varianza raros / dashboard: validar con notebook EVM.
- Cierre de periodo: impacto irreversible → confirmación humana en UI.

Notebook: `EVM_MODULE_CANONICAL.md`.

---

## 14. Compras / Órdenes de compra (~75%)

| Endpoint | Método | Notas |
|----------|--------|-------|
| `/compras` | POST | Registro directo + impacto partidas |
| `/ordenes-compra` | POST/GET | Ciclo de vida OC |
| `/ordenes-compra/{id}/solicitar` … `/aprobar` … `/enviar` … `/confirmar-recepcion` | POST | Transiciones |

```typescript
await apiClient.post("/compras", { /* RegistrarCompraRequest */ });
const oc = await apiClient.get(`/ordenes-compra/${id}`);
await apiClient.post(`/ordenes-compra/${id}/aprobar`);
```

### ⚠️ Medium maturity (~75%)

- Preferir **OrdenCompraController** para flujo de aprobación; `CompraController` es registro directo.
- Edge cases de aprobación / recepción: revisión humana; no inventar estados en FE.

Notebook: `COMPRAS_MODULE_CANONICAL.md`.

---

## 15. Inventario / almacén (~70%)

| Endpoint | Método | Notas |
|----------|--------|-------|
| `/proyectos/{proyectoId}/inventario` | GET | Stock del proyecto |
| `/almacen/movimientos` | POST/GET | Movimientos de almacén |

```typescript
await apiClient.get(`/proyectos/${proyectoId}/inventario`);
await apiClient.post("/almacen/movimientos", { /* payload as-built */ });
```

### ⚠️ Medium maturity (~70%)

- Multi-almacén / escenarios complejos: parcialmente definidos → no inventar en cliente.
- Integración con compras: vía dominio; FE solo refleja respuestas del API.

Notebook: `INVENTARIO_MODULE_CANONICAL.md`.

---

## 16. Producción — reportes (~55%)

| Endpoint | Método | Notas |
|----------|--------|-------|
| `/produccion/reportes` | GET/POST | Listar / crear |
| `/produccion/reportes/{reporteId}` | GET/PUT/DELETE | Detalle / editar / borrar |
| `.../aprobar` · `.../rechazar` | POST | Transiciones |

```typescript
await apiClient.post("/produccion/reportes", { /* payload */ });
await apiClient.post(`/produccion/reportes/${reporteId}/aprobar`);
```

### ⚠️ Medium-low maturity (~55%)

- Metrado / REGLA-004: validar contra canónico; no duplicar reglas en Zod sin notebook.
- Superficie dual / gaps: tratar como Phase 2 con revisión; ver `AI_SAFETY_GUIDE.md`.

Notebook: `PRODUCCION_MODULE_CANONICAL.md`.

---

## 17. RRHH (Phase 3 — no integrar UI completa)

Ver `RRHH_UNDER_DEVELOPMENT.md`. Tag OpenAPI: **RRHH (Under Development)**. Madurez ~50% + riesgo alto de alucinación.

---

## 18. Fuera de alcance

- Generación automática de tipos en CI → Task 12.
- Documentación FE en notebooks canónicos → Task 13 (incremental, solo as-built).
- Completar gaps de negocio Phase 2/3 (fuera de REQ-47).
