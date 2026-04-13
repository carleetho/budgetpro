# SEGURIDAD_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Seguridad (JWT, auth, filtro) |
| % oficial (tablero) | **75%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [SEGURIDAD_MODULE_CANONICAL.md](../../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `AuthController`, `SecurityConfig`, `JwtService` |

## 2. Superficie de código (evidencia)

- **`AuthController`** — `/api/v1/auth` → `login`, `register`, `me` (según canónico Apéndice A).
- **`SecurityConfig`** — `permitAll` para `/api/public/**`, `/api/v1/auth/**`, OpenAPI/Swagger; `anyRequest().authenticated()`.
- **JWT:** `JwtService` (validación longitud secreto REGLA-051).
- **CORS:** `corsConfigurationSource` con orígenes permitidos (p. ej. `localhost:3000` — **configuración dev-oriented**).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **RBAC fino** | Roles enum (`RolUsuario`); canónico Cross-Cutting / Seguridad: permisos dinámicos en roadmap | P2 |
| GF-02 | **CORS fijo** | Lista de orígenes en código; despliegue multi-origen requiere configuración externa | P2 |
| GF-03 | **Gestión de usuarios admin** | `register` fija rol por defecto; sin CRUD REST amplio de usuarios en `AuthController` (puede ser intencional) | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-052 / 075 / 078 / 079: coherentes con controlador y validaciones |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Rate limiting / brute-force en `login` — no auditado en profundidad en esta pasada |
| DT-02 | REGLA-054 variables de entorno (email/resend) — 🟡 en canónico cross-cutting |

## 6. Candidatos de cierre (priorizado)

1. **P2**: CORS y JWT desde `application.yml` / perfiles por entorno.
2. **P2**: Matriz de permisos por recurso (fase 2 RBAC).

## 7. Definición de hecho para subir %

- **Hacia ~80%**: RBAC extendido o política de permisos documentada + hardening login.
- **~75%** con JWT + roles actuales suficientes para MVP.

## 8. Referencias cruzadas

- [CROSS_CUTTING_MODULE_CANONICAL.md](../../modules/CROSS_CUTTING_MODULE_CANONICAL.md) (REGLA-051–056, RBAC roadmap).
