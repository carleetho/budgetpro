## [2026-01-14 19:43] TAREA: Núcleo inmutable + Producción + Cambios (JPA/DDL)
**Estado:** ✅ COMPLETADO
**Cambios:** 
- Añadidas entidades AuditEntity, ReporteProduccion, DetalleRPC y OrdenCambio con validaciones y relaciones LAZY.
- Actualizadas entidades Proyecto, Presupuesto, Partida, Recurso y MovimientoAlmacen con campos/constraints nuevos.
- Actualizados enums de estado/tipo y reglas de inmutabilidad en Presupuesto y Partida.
- Migración V16 con constraints, índices y parcial único de línea base.
**Validación:**
- [x] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 20:05] TAREA: Auditoría created_by + migración datos
**Estado:** ✅ COMPLETADO
**Cambios:**
- Configurada auditoría JPA con `AuditorAware<UUID>` para poblar `created_by`.
- Actualizado `AuditEntity` con `@CreatedBy`.
- Ajustada migración V16 para backfill de `created_by` y conversión de enums a VARCHAR con CHECK.
- Migración de estados antiguos de Proyecto/Presupuesto y tipo ACTIVO de Recurso.
**Validación:**
- [x] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 20:20] TAREA: Servicio RPC (Producción)
**Estado:** ✅ COMPLETADO
**Cambios:**
- Implementados `ProduccionService` y `ProduccionServiceImpl` con reglas de negocio RPC.
- Añadido `ProduccionValidator` con validaciones de exceso de metrado, estado de proyecto y fecha.
- Añadida `BusinessRuleException`.
- Repositorios JPA para `ReporteProduccionEntity` y `DetalleRPCEntity` con query eficiente.
**Validación:**
- [ ] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 20:45] TAREA: REST RPC + Tests de integración
**Estado:** ✅ COMPLETADO
**Cambios:**
- Endpoints REST para crear/actualizar/aprobar/rechazar/eliminar reportes de producción.
- DTOs de request/response y advice de errores de negocio.
- Tests de integración para reglas críticas (exceso, inmutabilidad, fecha futura).
**Validación:**
- [x] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 21:05] TAREA: API Producción REST v1
**Estado:** ✅ COMPLETADO
**Cambios:**
- DTOs nuevos en `infrastructure/rest/dto/produccion` (crear, rechazar, respuestas).
- Nuevo `ProduccionController` con endpoints versionados bajo `/api/v1`.
- `GlobalExceptionHandler` con BusinessRuleException->409 y EntityNotFound->404.
- Tests de integración para endpoints v1.
**Validación:**
- [x] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 23:02] TAREA: Frontend RPC Service
**Estado:** ✅ COMPLETADO
**Cambios:**
- Tipos RPC en `frontend/src/core/types/produccion.ts`.
- Servicio HTTP `frontend/src/services/produccion.service.ts`.
- Añadido `patch` en `api-client` para consumir endpoints PATCH.
**Validación:**
- [ ] Compila (`npm run build`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 22:56] TAREA: Ejecución tests de integración
**Estado:** ❌ FALLIDO
**Cambios:**
- Ajuste de enums en tests (BORRADOR/EJECUCION) para compilar.
**Validación:**
- [x] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [ ] Alineado con ERD/Arquitectura
**Notas:** Fallo por Testcontainers: Docker API 1.32 es demasiado antiguo (mínimo 1.44). No se pudo iniciar postgres:16-alpine.

## [2026-01-14 23:08] TAREA: Fix migración V16 enum recurso_tipo
**Estado:** ✅ COMPLETADO
**Cambios:**
- Reordenado V16 para convertir enum a VARCHAR antes de actualizar tipo a EQUIPO.
**Validación:**
- [ ] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

## [2026-01-14 23:11] TAREA: Fix columna created_by en movimiento_almacen
**Estado:** ✅ COMPLETADO
**Cambios:**
- Añadida columna `created_by` en `movimiento_almacen` dentro de V16 con backfill y NOT NULL.
**Validación:**
- [ ] Compila (`./mvnw -q -DskipTests compile`)
- [ ] Tests actualizados/creados
- [x] Alineado con ERD/Arquitectura

