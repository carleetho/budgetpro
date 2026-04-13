# CRONOGRAMA_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 + ajuste canónico (C-04 / §8 verificados en código).  
> **Rama**: `feature/gaps-cronograma-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Cronograma |
| % oficial (tablero) | **60%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [CRONOGRAMA_MODULE_CANONICAL.md](../../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `CronogramaController`, `CalculoCronogramaService`, `ProgramarActividadUseCaseImpl` |

## 2. Superficie de código (evidencia)

- **REST**: `com.budgetpro.infrastructure.rest.cronograma.controller.CronogramaController` — `@RequestMapping("/api/v1/proyectos")`  
  - `POST /{proyectoId}/cronograma/actividades`  
  - `GET /{proyectoId}/cronograma`
- **Dominio**: `com.budgetpro.domain.finanzas.cronograma.service.CalculoCronogramaService` (usa `WorkingDayCalculator` para `calcularDuracionTotal`), `CronogramaService`.
- **Aplicación**: `ProgramarActividadUseCaseImpl`, `ConsultarCronogramaUseCase` (puertos en `application.cronograma`).
- **Migración**: ver `DATA_MODEL` / Flyway si aplica programa de obra (no re-auditado en esta pasada).

## 3. Gaps funcionales (REST / contrato)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Baseline HTTP en §8 canónico** | El canónico listaba `POST .../cronograma/baseline` como ✅. En `infrastructure/rest` **no** hay mapping para esa ruta en `CronogramaController` ni coincidencia `grep` en controladores (2026-04-12). | P1 (drift doc ↔ REST) |
| GF-02 | **Superficie REST mínima** | Solo programar actividad + consultar cronograma; sin CRUD explícito de dependencias ni baseline vía este controller | P2 (puede ser intencional si el resto es dominio/presupuesto) |
| GF-03 | **Respuesta `POST .../actividades`** | `ResponseEntity.ok()` con header `Location`; no **201 Created** estricto (detalle contract-first / OpenAPI) | P3 |
| GF-04 | **Roadmap canónico** | UC-C04 Calendarios / UC-C05 MS Project siguen 🔴 en canónico §6 | P2 |

## 4. Gaps de reglas / alineación doc

| ID | Regla / nota canónica | Evidencia código | Acción en PR |
|----|------------------------|------------------|--------------|
| GR-01 | **C-04** “no consta reutilización” en cronograma | `CalculoCronogramaService` instancia `WorkingDayCalculator` y usa `workingDaysBetween` en `calcularDuracionTotal` (comentario C-04 en fuente) | Actualizar canónico §2: C-04 🟡 con nota acotada (días hábiles Lun–Vie en cálculo de duración; calendario de excepciones sigue en roadmap UC-C04) |
| GR-02 | C-02 / C-03 | Sigue 🟡 según canónico; sin cambio en esta pasada | Seguimiento en I1 si PO prioriza |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas |
|----|------|--------|
| DT-01 | Ordenamiento topológico “naive” | Canónico §11 |
| DT-02 | Exponer o retirar baseline de §8 | Alinear OpenAPI y clientes tras decisión |

## 6. Candidatos de cierre (priorizado)

1. **P1**: Corregir §8 (estado de baseline) o implementar `POST .../baseline` si es requisito P0 de producto.
2. **P2**: Normalizar `POST .../actividades` a **201** si el contrato público lo exige.
3. **P2**: Avanzar UC-C04 / histograma según roadmap §1.

## 7. Definición de hecho para subir %

- **Hacia ~75%**: hitos del roadmap §1 (histograma, excepciones de calendario) + contrato REST y OpenAPI sin filas fantasmas.
- **60%** se mantiene mientras UC-C04/C-02/C-03 sigan en estado parcial sin cierre medible.

## 8. Referencias cruzadas

- Tablero: [SCOREBOARD_17.md](../SCOREBOARD_17.md).
- Hallazgos: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-10**).
