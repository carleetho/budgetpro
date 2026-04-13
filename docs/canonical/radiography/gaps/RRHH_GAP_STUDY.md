# RRHH_GAP_STUDY.md — Estudio de brechas (Ola 1)

> **Tipo de PR**: G0 (solo documentación + alineación canónica §8 verificada en código).  
> **Rama**: `feature/gaps-rrhh-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | RRHH |
| % oficial (tablero) | **35%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [RRHH_MODULE_CANONICAL.md](../../modules/RRHH_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first scan `backend/.../infrastructure/rest/rrhh` |

## 2. Superficie de código (evidencia)

- **Dominio / aplicación**: `com.budgetpro.domain.rrhh`, `com.budgetpro.application.rrhh`
- **REST** (`infrastructure/rest/rrhh/controller/`):
  - `ConfiguracionLaboralExtendidaController` → `/api/v1/rrhh/configuracion`
  - `EmpleadoController` → `/api/v1/rrhh/empleados`
  - `AsistenciaController` → `/api/v1/rrhh/asistencias`
  - `NominaController` → `/api/v1/rrhh/nominas`
  - `CuadrillaController` → `/api/v1/rrhh/cuadrillas`
  - `CostosLaboralesController` → `/api/v1/rrhh/costos`
- **Migraciones**: `V15__create_rrhh_schema.sql`, `V26__rrhh_config_laboral_global_nullable_proyecto.sql`
- **Excepciones REST** (muestra): `GlobalExceptionHandler` maneja `ProyectoNoActivoException`, `ConfiguracionLaboralNotFoundException` (`application/rrhh/exception`)
- **Tests de referencia**: `RrhhRegla150ProyectoActivoTest`, `CrearEmpleadoUseCaseImplTest`, `CalculadorFSRTest`, adaptadores bajo `...persistence...rrhh`

## 3. Gaps funcionales (REST / casos de uso)

| ID | Esperado (canónico / negocio) | Observado (código) | Severidad |
|----|-------------------------------|-------------------|-----------|
| GF-01 | §8 del canónico listaba rutas legacy (`/api/v1/configuracion-laboral`, `/api/v1/personal`) | Implementación real bajo prefijo **`/api/v1/rrhh/**` (ver §8 actualizado en el mismo PR) | P2 (doc corregida; riesgo si clientes u OpenAPI siguen la tabla antigua) |
| GF-02 | Asignación de trabajador a proyecto (REGLA-150 / flujos de obra) | `AsignarEmpleadoProyectoUseCaseImpl` existe en **application**; **no** hay `*Controller` REST que exponga `asignar` | P1 |
| GF-03 | Consulta acotada de asistencias | `GET /api/v1/rrhh/asistencias` exige `empleadoId` o `proyectoId` + rango; si ambos faltan devuelve **lista vacía** (200) en lugar de 400 | P2 |
| GF-04 | Roadmap canónico “Next” (personnel registry, tareos polish, planillas) | Cobertura parcial: CRUD empleado, asistencia, nómina calcular/consultar, cuadrillas, costos; sin cierre completo de régimen civil ni motor de reglas | P0 (estratégico; no bloqueante técnico inmediato) |

## 4. Gaps de reglas / invariantes

| ID | Regla (ID canónico) | Estado doc | Estado código | Notas |
|----|---------------------|------------|-----------------|-------|
| GR-01 | R-02 asistencia trabajador inactivo | ✅ | ✅ / tests | `InactiveWorker` / validaciones en flujo asistencia |
| GR-02 | R-03 doble booking / solape | 🟡 Partial | 🟡 | Canónico: solape mismo trabajador; multi-sitio TBD — seguir antes de subir % |
| GR-03 | REGLA-150 proyecto ACTIVO | ✅ | ✅ | `ProyectoNoActivoException` + test `RrhhRegla150ProyectoActivoTest` |
| GR-04 | UC-R04 nómina | 🟡 | 🟡 | ISR fijo `NominaConstants`; deuda ya listada en canónico §11 |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas | Enlace |
|----|------|-------|--------|
| DT-01 | Régimen civil complejo | Lluvias, altitud, feriados — canónico §11 | Canónico §11 |
| DT-02 | Costos laborales / varianza | Placeholder y costo estimado demo — canónico §11 | Canónico §11 |
| DT-03 | Generación asistida de lógica nueva | Canónico advierte contra IA ciega en reglas de obra | Cabecera `RRHH_MODULE_CANONICAL.md` |

## 6. Candidatos de cierre (priorizado)

1. **P0 (negocio / compliance)**: Definir con PO el alcance R-03 multi-sitio y reglas de régimen; no incrementar % sin cierre acordado.
2. **P1**: Exponer o documentar explícitamente **asignación empleado ↔ proyecto** vía REST (o marcar como interno-only y cerrar GF-02 por decisión).
3. **P2**: Endurecer contrato `GET .../asistencias` (400 + mensaje) o documentar comportamiento “vacío”; actualizar OpenAPI/Swagger si aplica.
4. **P2**: Nómina — roadmap hacia tabla ISR progresiva (fuera de este G0).

## 7. Definición de hecho para subir %

- **Hacia ~50%** (alineado al roadmap canónico): cierre verificable de UC-R03 🟡→✅ en los criterios definidos con PO + evidencia (tests + REST); registro de personal / tareos “polish” acotado en canónico.
- Este documento **no** cambia el % solo: actualiza inventario de brechas para la siguiente iteración **I1** si hay código.

## 8. Referencias cruzadas

- Tablero: fila RRHH en [SCOREBOARD_17.md](../SCOREBOARD_17.md) enlaza aquí.
- Hallazgos abiertos: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (O-05, O-06).
- §8 REST del canónico RRHH alineado a controladores reales en el mismo PR.
