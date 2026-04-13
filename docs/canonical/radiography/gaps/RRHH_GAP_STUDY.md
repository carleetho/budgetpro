# RRHH_GAP_STUDY.md — Estudio de brechas (Ola 1)

> **Tipo de PR**: G0 (solo documentación + alineación canónica §8 verificada en código).  
> **Rama**: `feature/gaps-rrhh-ola1`  
> **Fecha**: 2026-04-12 · **I1**: 2026-04-13 GF-03, GF-02 · **G0**: 2026-04-13 GF-01 (canónico §8.1)

## 1. Baseline


| Campo               | Valor                                                              |
| ------------------- | ------------------------------------------------------------------ |
| Módulo              | RRHH                                                               |
| % oficial (tablero) | **35%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md)                  |
| Notebook            | [RRHH_MODULE_CANONICAL.md](../../modules/RRHH_MODULE_CANONICAL.md) |
| Fecha revisión      | 2026-04-13 (GF-01/02/03 cerrados según fila)                         |
| Autor / revisores   | Code-first scan `backend/.../infrastructure/rest/rrhh`             |


## 2. Superficie de código (evidencia)

- **Dominio / aplicación**: `com.budgetpro.domain.rrhh`, `com.budgetpro.application.rrhh`
- **REST** (`infrastructure/rest/rrhh/controller/`):
  - Paralelo Presupuesto/FSR: `LaboralController` → `PUT /api/v1/configuracion-laboral`, `PUT /api/v1/proyectos/{id}/configuracion-laboral` (ver canónico §8.1; **no** es prefijo `rrhh`)
  - `ConfiguracionLaboralExtendidaController` → `/api/v1/rrhh/configuracion`
  - `EmpleadoController` → `/api/v1/rrhh/empleados` (+ `POST .../empleados/{id}/asignaciones` → `AsignarEmpleadoProyectoUseCase`)
  - `AsistenciaController` → `/api/v1/rrhh/asistencias`
  - `NominaController` → `/api/v1/rrhh/nominas`
  - `CuadrillaController` → `/api/v1/rrhh/cuadrillas`
  - `CostosLaboralesController` → `/api/v1/rrhh/costos`
- **Migraciones**: `V15__create_rrhh_schema.sql`, `V26__rrhh_config_laboral_global_nullable_proyecto.sql`
- **Excepciones REST** (muestra): `GlobalExceptionHandler` maneja `ProyectoNoActivoException`, `ConfiguracionLaboralNotFoundException`, `FiltrosConsultaAsistenciaIncompletosException`, `AsignacionProyectoConflictoException` (409 `ASIGNACION_PROYECTO_CONFLICTO`)
- **Tests de referencia**: `RrhhRegla150ProyectoActivoTest`, `CrearEmpleadoUseCaseImplTest`, `CalculadorFSRTest`, `AsistenciaControllerTest`, `EmpleadoControllerTest`, adaptadores bajo `...persistence...rrhh`

## 3. Gaps funcionales (REST / casos de uso)


| ID    | Esperado (canónico / negocio)                                                              | Observado (código)                                                                                                                                   | Severidad                                                                |
| ----- | ------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| GF-01 | Contrato documental vs rutas “fuera” de `/api/v1/rrhh` (`configuracion-laboral`, inexistencia de `/personal`) | **§8.1** en [RRHH_MODULE_CANONICAL.md](../../modules/RRHH_MODULE_CANONICAL.md): `LaboralController` documentado como paralelo; empleados solo bajo `/api/v1/rrhh/empleados` | P2 **cerrado G0** (2026-04-13)                                           |
| GF-02 | Asignación de trabajador a proyecto (REGLA-150 / flujos de obra)                           | `POST /api/v1/rrhh/empleados/{empleadoId}/asignaciones` + `AsignarEmpleadoProyectoRequest` → `AsignarEmpleadoProyectoUseCase` (201 + `Location`)       | P1 **cerrado I1** (2026-04-13)                                           |
| GF-03 | Consulta acotada de asistencias                                                            | `GET /api/v1/rrhh/asistencias`: sin `empleadoId` ni `proyectoId` útiles (null o en blanco) → **400** + `ErrorResponses` (`MISSING_ATTENDANCE_FILTERS`); con uno de los dos + `fechaInicio`/`fechaFin` → 200 | P2 **cerrado I1** (2026-04-13)                                           |
| GF-04 | Roadmap canónico “Next” (personnel registry, tareos polish, planillas)                     | Cobertura parcial: CRUD empleado, asistencia, nómina calcular/consultar, cuadrillas, costos; sin cierre completo de régimen civil ni motor de reglas | P0 (estratégico; no bloqueante técnico inmediato)                        |


## 4. Gaps de reglas / invariantes


| ID    | Regla (ID canónico)                 | Estado doc | Estado código | Notas                                                                        |
| ----- | ----------------------------------- | ---------- | ------------- | ---------------------------------------------------------------------------- |
| GR-01 | R-02 asistencia trabajador inactivo | ✅          | ✅ / tests     | `InactiveWorker` / validaciones en flujo asistencia                          |
| GR-02 | R-03 doble booking / solape         | 🟡 Partial | 🟡            | Canónico: solape mismo trabajador; multi-sitio TBD — seguir antes de subir % |
| GR-03 | REGLA-150 proyecto ACTIVO           | ✅          | ✅             | `ProyectoNoActivoException` + test `RrhhRegla150ProyectoActivoTest`          |
| GR-04 | UC-R04 nómina                       | 🟡         | 🟡            | ISR fijo `NominaConstants`; deuda ya listada en canónico §11                 |


## 5. Deuda técnica y riesgos


| ID    | Tema                                | Notas                                               | Enlace                              |
| ----- | ----------------------------------- | --------------------------------------------------- | ----------------------------------- |
| DT-01 | Régimen civil complejo              | Lluvias, altitud, feriados — canónico §11           | Canónico §11                        |
| DT-02 | Costos laborales / varianza         | Placeholder y costo estimado demo — canónico §11    | Canónico §11                        |
| DT-03 | Generación asistida de lógica nueva | Canónico advierte contra IA ciega en reglas de obra | Cabecera `RRHH_MODULE_CANONICAL.md` |


## 6. Candidatos de cierre (priorizado)

1. **P0 (negocio / compliance)**: Definir con PO el alcance R-03 multi-sitio y reglas de régimen; no incrementar % sin cierre acordado.
   - **I1 (2026-04-13) — base técnica R-03 (sin cierre de negocio)**: en dominio RRHH existe el puerto `com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator` (empleado, ventana `fechaInicio`/`fechaFin`, colección de `AsignacionProyecto`). La implementación de referencia `AmbiguityBlockedAsignacionSolapeValidator` **no** contiene algoritmo de solape: lanza `UnsupportedOperationException` explícita por `[AMBIGUITY_DETECTED]` / decisión de PO pendiente. Tests JUnit 5 cubren el bloqueo del stub y el contrato de error en asignación (`AsignacionProyectoConflictoException` → 409 `ASIGNACION_PROYECTO_CONFLICTO` vía `GlobalExceptionHandler` cuando el repositorio detecta solape). **Pendiente**: definición multi-sitio + sustitución del stub por política acordada y cableado del puerto al flujo de aplicación.
2. ~~**P1**: Asignación empleado ↔ proyecto vía REST~~ **Hecho (2026-04-13)**: `POST .../empleados/{empleadoId}/asignaciones`; conflicto solape → 409 documentado en handler.
3. ~~**P2**: Endurecer contrato `GET .../asistencias`~~ **Hecho (2026-04-13)**: 400 + `GlobalExceptionHandler` + `FiltrosConsultaAsistenciaIncompletosException`; pendiente OpenAPI/Swagger si aplica.
4. **P2**: Nómina — roadmap hacia tabla ISR progresiva (fuera de este G0).
5. ~~**P2**: GF-01 doc/OpenAPI~~ **Hecho (2026-04-13)**: §8.1 canónico; OpenAPI genérico pendiente si el proyecto publica spec global.

## 7. Definición de hecho para subir %

- **Hacia ~50%** (alineado al roadmap canónico): cierre verificable de UC-R03 🟡→✅ en los criterios definidos con PO + evidencia (tests + REST); registro de personal / tareos “polish” acotado en canónico.
- Este documento **no** cambia el % solo: actualiza inventario de brechas para la siguiente iteración **I1** si hay código.

## 8. Referencias cruzadas

- Tablero: fila RRHH en [SCOREBOARD_17.md](../SCOREBOARD_17.md) enlaza aquí.
- Hallazgos: cierres **O-05** → **H-12**, **O-06** → **H-11** en [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §2; §3 sin O-* abiertos propios de RRHH.
- §8 REST del canónico RRHH alineado a controladores reales en el mismo PR.

## 9. PR I1 acotado sugerido (wave 1)

Un **solo** objetivo por PR; no mezclar cierre completo de **R-03 / GR-02** (multi-sitio, criterios con PO) con cambios de contrato menores.

### Decisión de producto (GF-02)

- **Ejecutado (2026-04-13)**: asignación **expuesta vía REST** como `POST /api/v1/rrhh/empleados/{empleadoId}/asignaciones` con cuerpo `proyectoId`, `fechaInicio`, `fechaFin` (opcional), `tarifaHora` y `rolProyecto` opcionales — misma política de seguridad que el resto de `/api/v1/rrhh/**`.

### Cambio de contrato o código recomendado (elige uno por PR)


| Opción | Alcance                                                                                                                                                                                                     | Evidencia esperada                                                               |
| ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| **A**  | Exponer asignación según decisión PO → nuevo endpoint + delegación a `AsignarEmpleadoProyectoUseCaseImpl`                                                                                                   | Test de integración o WebMvcTest del controller; §8 canónico + OpenAPI si aplica |
| **B**  | **GF-03 (O-06)**: `GET /api/v1/rrhh/asistencias` sin `empleadoId` ni `proyectoId` → **400** + cuerpo `ErrorResponses` (o documentar explícitamente 200 vacío y cerrar O-06 en review log con justificación) | Test de request inválido; alinear `GlobalExceptionHandler` si hace falta         |


### Git y radiografía

- **Rama**: `feature/i1-rrhh-asignacion-rest` (opción A) o `feature/i1-rrhh-asistencias-contract` (opción B).
- **Mismo PR I1**: código + cabecera `Status` del [canónico](../../modules/RRHH_MODULE_CANONICAL.md) y fila del scoreboard **solo si** aplica salto Ola 2 (DoD P0/P1); si no sube %, actualizar solo gap study + `CODE_DOC_REVIEW_LOG.md` (cerrar O-* o dejar nota).
- **R-03 (GR-02)**: el PR dedicado con criterios de solape multi-sitio acordados sigue pendiente; el **2026-04-13** añadió solo puerto dominio + stub bloqueado + tests de contrato (ver §6 ítem 1).

