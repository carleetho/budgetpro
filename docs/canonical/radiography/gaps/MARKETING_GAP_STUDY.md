# MARKETING_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad en radiografía).  
> **Rama**: `feature/gaps-marketing-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Marketing (leads / demo) |
| % oficial (tablero) | **55%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [MARKETING_MODULE_CANONICAL.md](../../modules/MARKETING_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `PublicController`, `MarketingLeadController`, `LeadService` |

## 2. Superficie de código (evidencia)

- **Servicio**: `com.budgetpro.application.marketing.service.LeadService` — `crearLead` fija `LeadEstado.NUEVO`, notificación email, TODO CRM.
- **REST**:
  - `PublicController` — `@RequestMapping("/api/public/v1")` → `POST /demo-request` (cuerpo `CrearLeadRequest`).
  - `MarketingLeadController` — `@RequestMapping("/api/v1/marketing/leads")` → `GET /` (paginación `page`, `size`), `GET /{leadId}`.
- **Persistencia**: `LeadEntity`, `LeadJpaRepository`, Flyway `V30__create_marketing_lead.sql` (citado en canónico).
- **Seguridad**: `/api/public/**` permitAll; `/api/v1/marketing/**` requiere JWT (`SecurityConfig`).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Transiciones de estado (REGLA-108)** | Estados permitidos en BD; **no** hay endpoints autenticados `PUT`/`PATCH` para pasar NUEVO → CONTACTADO / CONVERTIDO; solo creación pública fija NUEVO | P1 (alineado a deuda en canónico Apéndice A) |
| GF-02 | **Listado interno paginado** | `findAll()` + `subList` en JVM — mismo patrón que [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) **O-01** | P2 |
| GF-03 | **Respuesta `LeadResponse` en APIs** | Interno devuelve `id`, `estado`, `fechaSolicitud` únicamente; campos de contacto no expuestos en listado/detalle vía este DTO (puede ser intencional por privacidad; documentar decisión) | P3 |
| GF-04 | **Integración CRM** | `LeadService`: comentario TODO HubSpot; sin cliente ni cola asíncrona en repo | P2 |
| GF-05 | **Superficie pública** | `POST` sin autenticación; no se observa *rate limiting* dedicado en `SecurityConfig` (revisión superficial) — riesgo abuso / spam | P2 |

## 4. Gaps de reglas / invariantes

| ID | Regla (canónico) | Notas |
|----|------------------|--------|
| GR-01 | REGLA-108 / 123 / 124 | Creación y constraints coherentes con entidad y SQL; sin brecha nueva en esta pasada |
| GR-02 | Máquina de estados | Sin API interna, el cumplimiento de transiciones depende de procesos externos o futuros casos de uso |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas |
|----|------|--------|
| DT-01 | Paginación BD | Sustituir `findAll` por consulta paginada Spring Data o criterios |
| DT-02 | Workflow CRM | Definir si transiciones viven en backend (REST) o solo en integración |
| DT-03 | Hardening público | Rate limit, captcha o *API key* ligera según amenaza aceptada |

## 6. Candidatos de cierre (priorizado)

1. **P1**: Diseñar **I1** con `PATCH /api/v1/marketing/leads/{id}` (o subrecurso `/estado`) + caso de uso y tests de REGLA-108.
2. **P2**: Cerrar **O-01** para este controlador (query paginada).
3. **P2**: Política anti-abuso en `POST /api/public/v1/demo-request`.
4. **P2**: Spike CRM (cola o cliente) o eliminar TODO con decisión explícita.

## 7. Definición de hecho para subir %

- **Hacia ~65–70%**: mutación controlada de estado vía API autenticada + tests + documentación OpenAPI; paginación en BD para listado interno.
- Permanecer en **~55%** mientras el flujo sea “crear público + solo lectura interna” sin cierre de GF-01.

## 8. Referencias cruzadas

- Tablero: [SCOREBOARD_17.md](../SCOREBOARD_17.md).
- Hallazgos: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-01** compartido; **O-09** marketing).
