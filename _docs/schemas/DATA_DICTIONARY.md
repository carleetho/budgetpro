# 📊 DICCIONARIO DE DATOS - BUDGETPRO

> **Meta-Info:**
> - **Fecha de Generación:** 2026-01-10
> - **Fuente:** Migraciones Flyway (V1, V2) + Entidades JPA
> - **Estado:** Baseline Reconstruido (Forensic Analysis)
> - **Última Actualización:** 2026-01-10

---

## 🎯 PROPÓSITO

Este documento es el **Diccionario de Datos Físico** del sistema BUDGETPRO, reconstruido mediante ingeniería inversa del código existente. Refleja la estructura real de las tablas PostgreSQL tal como están definidas en las migraciones Flyway y mapeadas en las entidades JPA.

**REGLAS CRÍTICAS:**
- Este documento NO debe modificarse sin actualizar primero las migraciones SQL correspondientes.
- Cualquier cambio en el esquema debe reflejarse aquí INMEDIATAMENTE.
- Este es el documento de referencia para QA y validación de integridad de BD.

---

## 📋 ENUMERADOS GLOBALES (PostgreSQL ENUM Types)

### `recurso_tipo`

| Valor | Descripción | Origen |
|-------|-------------|--------|
| `MATERIAL` | Material de construcción | Dominio |
| `MANO_OBRA` | Mano de obra / Trabajadores | Dominio |
| `SUBCONTRATO` | Subcontrato | Dominio |
| `ACTIVO` | Activo fijo | Dominio |

**Nota:** El ERD menciona `EQUIPO` y `SERVICIO`, pero el dominio actual solo define estos 4 valores.

**Migración:** `V1__create_recurso_types_and_table.sql`

---

### `recurso_estado`

| Valor | Descripción | Uso |
|-------|-------------|-----|
| `ACTIVO` | Recurso activo y disponible | Estado por defecto |
| `EN_REVISION` | Recurso provisional, pendiente de revisión | Wireflow 1 (Compra Directa) |
| `DEPRECADO` | Recurso desactivado | Soft delete |

**Migración:** `V1__create_recurso_types_and_table.sql`

---

## 🗄️ TABLAS

### 1. `recurso`

**Descripción:** Catálogo maestro de recursos (Shared Kernel). Cada recurso representa un concepto económico global único en BUDGETPRO.

**Mapeo JPA:** `com.budgetpro.infrastructure.persistence.entity.RecursoEntity`

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `nombre` | `TEXT` | - | `NO` | - | `String nombre` | Nombre original del recurso |
| `nombre_normalizado` | `TEXT` | `UNIQUE (uq_recurso_nombre)` | `NO` | - | `String nombreNormalizado` | Nombre normalizado (Trim + UpperCase + espacios únicos) |
| `tipo` | `recurso_tipo` | - | `NO` | - | `TipoRecurso tipo` | Tipo de recurso (ENUM) |
| `unidad_base` | `VARCHAR(20)` | - | `NO` | - | `String unidadBase` | Unidad de medida (ej: "m²", "kg", "hr") |
| `atributos` | `JSONB` | - | `NO` | `'{}'` | `Map<String, Object> atributos` | Atributos adicionales (JSON) |
| `estado` | `recurso_estado` | - | `NO` | `'ACTIVO'` | `EstadoRecurso estado` | Estado del recurso (ENUM) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime updatedAt` | Auditoría: Fecha de actualización |
| `created_by` | `UUID` | - | `NO` | - | `UUID createdBy` | Auditoría: Usuario creador |

**Índices:**
- `idx_recurso_tipo` (`tipo`)
- `idx_recurso_estado` (`estado`)
- **IMPLÍCITO:** `idx_recurso_nombre_normalizado` (UNIQUE constraint)

**Constraint UNIQUE:**
- `uq_recurso_nombre` (`nombre_normalizado`) - Previene duplicados semánticos

**Migración:** `V1__create_recurso_types_and_table.sql`

**Anotaciones JPA Especiales:**
- `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` en `tipo` y `estado` (Hibernate 6)
- `@JdbcTypeCode(SqlTypes.JSON)` en `atributos` (JSONB nativo)
- `@CreationTimestamp` en `created_at`
- `@UpdateTimestamp` en `updated_at`

---

### 2. `proyecto`

**Descripción:** Tabla maestra de proyectos de ingeniería civil.

**Mapeo JPA:** No detectado en el código actual (tabla creada solo para FK de `billetera`).

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | - | Identificador único |
| `nombre` | `TEXT` | - | `NO` | - | - | Nombre del proyecto |
| `estado` | `VARCHAR(30)` | - | `NO` | - | - | Estado del proyecto |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | - | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | - | Auditoría: Fecha de actualización |

**Migración:** `V2__create_finanzas_schema.sql`

**Nota:** Esta tabla fue creada con `CREATE TABLE IF NOT EXISTS` para soportar la FK de `billetera`. No se encontró entidad JPA correspondiente en el código actual.

---

### 3. `billetera`

**Descripción:** Billetera financiera de un proyecto. Relación 1:1 con proyecto (UNIQUE constraint).

**Mapeo JPA:** `com.budgetpro.infrastructure.persistence.entity.BilleteraEntity`

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `proyecto_id` | `UUID` | `UNIQUE`, `FK → proyecto(id)` | `NO` | - | `UUID proyectoId` | Relación 1:1 con proyecto |
| `saldo_actual` | `NUMERIC(19,4)` | `CHECK (saldo_actual >= 0)` | `NO` | `0` | `BigDecimal saldoActual` | Saldo actual (escala 4 decimales) |
| `version` | `BIGINT` | - | `NO` | `0` | `Long version` | Optimistic Locking (Hibernate @Version) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime updatedAt` | Auditoría: Fecha de actualización |

**Índices:**
- `idx_billetera_proyecto_id` (`proyecto_id`)

**Constraints:**
- `uq_billetera_proyecto` (`proyecto_id`) - UNIQUE: Un proyecto tiene UNA sola billetera
- `fk_billetera_proyecto` (`proyecto_id`) - FOREIGN KEY → `proyecto(id)`
- `chk_saldo_no_negativo` (`saldo_actual >= 0`) - Invariante: Saldo nunca negativo

**Migración:** `V2__create_finanzas_schema.sql`

**Anotaciones JPA Especiales:**
- `@Version` en `version` (Optimistic Locking automático)
- `@CreationTimestamp` en `created_at`
- `@UpdateTimestamp` en `updated_at`
- `@OneToMany(mappedBy = "billetera", cascade = CascadeType.ALL, orphanRemoval = true)` en `movimientos` (relación bidireccional)

**Relación Bidireccional:**
- `BilleteraEntity` tiene `List<MovimientoEntity> movimientos` (lado inverso, `mappedBy`)
- `CascadeType.ALL` propaga todas las operaciones a los movimientos hijos
- `orphanRemoval = true` elimina movimientos huérfanos al ser removidos de la colección

**Nota:** El ERD original define `version INT`, pero se usa `BIGINT` porque Hibernate `@Version` requiere `Long` (64 bits) en Java.

---

### 4. `movimiento_caja`

**Descripción:** Registro histórico de movimientos de fondos (ingresos y egresos) de una billetera.

**Mapeo JPA:** `com.budgetpro.infrastructure.persistence.entity.MovimientoEntity`

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `billetera_id` | `UUID` | `FK → billetera(id) ON DELETE CASCADE` | `NO` | - | `BilleteraEntity billetera` | Relación N:1 bidireccional (propietario de FK) |
| `monto` | `NUMERIC(19,4)` | - | `NO` | - | `BigDecimal monto` | Monto del movimiento (escala 4 decimales) |
| `tipo` | `VARCHAR(20)` | `CHECK (tipo IN ('INGRESO', 'EGRESO'))` | `NO` | - | `TipoMovimiento tipo` | Tipo de movimiento (ENUM Java) |
| `fecha` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime fecha` | Fecha del movimiento |
| `referencia` | `VARCHAR(255)` | - | `NO` | - | `String referencia` | Descripción/referencia del movimiento |
| `evidencia_url` | `VARCHAR(500)` | - | `YES` | `NULL` | `String evidenciaUrl` | URL opcional de evidencia documental |
| `estado` | `VARCHAR(20)` | - | `NO` | `'ACTIVO'` | `String estado` | Estado del movimiento (por defecto ACTIVO) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |

**Índices:**
- `idx_movimiento_caja_billetera` (`billetera_id`)
- `idx_movimiento_caja_fecha` (`fecha DESC`)
- `idx_movimiento_caja_tipo` (`tipo`)
- `idx_movimiento_caja_estado` (`estado`)

**Constraints:**
- `fk_movimiento_caja_billetera` (`billetera_id`) - FOREIGN KEY → `billetera(id) ON DELETE CASCADE`
- **CHECK:** `tipo IN ('INGRESO', 'EGRESO')`

**Migración:** `V2__create_finanzas_schema.sql`

**Anotaciones JPA Especiales:**
- `@ManyToOne(fetch = FetchType.LAZY)` en `billetera` (relación bidireccional, propietario de FK)
- `@JoinColumn(name = "billetera_id")` con `@ForeignKey`
- `@Enumerated(EnumType.STRING)` en `tipo` (mapeo a VARCHAR)
- `@CreationTimestamp` en `created_at`

**Relación Bidireccional:**
- `MovimientoEntity` (propietario) → `BilleteraEntity` mediante `@ManyToOne`
- `BilleteraEntity` (lado inverso) → `List<MovimientoEntity>` mediante `@OneToMany(mappedBy = "billetera", cascade = CascadeType.ALL)`
- El método helper `agregarMovimiento()` mantiene la sincronización bidireccional

**Nota:** El campo `tipo` se mapea como `VARCHAR(20)` en SQL pero como Enum Java (`TipoMovimiento`) en el dominio. El campo `estado` es de tipo VARCHAR y no está mapeado al dominio (solo existe en la capa de persistencia).

---

## 🔗 RELACIONES

| Tabla Origen | Tabla Destino | Tipo | Constraint | On Delete | Mapeo JPA | Descripción |
|--------------|---------------|------|------------|-----------|-----------|-------------|
| `billetera` | `proyecto` | 1:1 | `fk_billetera_proyecto` | - | - | Un proyecto tiene UNA sola billetera |
| `movimiento_caja` | `billetera` | N:1 | `fk_movimiento_caja_billetera` | `CASCADE` | `@ManyToOne` bidireccional | Una billetera tiene N movimientos |
| `billetera` (inverso) | `movimiento_caja` | 1:N | - | - | `@OneToMany(mappedBy, cascade=ALL)` | Relación bidireccional manejada por el lado "many" |

---

## 📝 NOTAS CRÍTICAS

1. **Precisión Numérica:**
   - Todos los montos financieros usan `NUMERIC(19,4)` (19 dígitos totales, 4 decimales).
   - Esto coincide con el Value Object `Monto` del dominio que usa escala 4.

2. **Optimistic Locking:**
   - La tabla `billetera` usa `version BIGINT` con `@Version` de Hibernate.
   - Esto previene actualizaciones perdidas (lost updates) en operaciones concurrentes.

3. **Normalización de Nombres:**
   - La tabla `recurso` tiene dos columnas: `nombre` (original) y `nombre_normalizado` (canónico).
   - El constraint UNIQUE está en `nombre_normalizado`, permitiendo deduplicación semántica.

4. **Soft Delete:**
   - No se implementa soft delete explícito. Los recursos se marcan como `DEPRECADO` en lugar de eliminarse.

5. **Auditoría:**
   - Todas las tablas tienen `created_at` y `updated_at`.
   - La tabla `recurso` incluye `created_by` (UUID del usuario creador).

---

## ✅ VERIFICACIÓN DE INTEGRIDAD

**Última Verificación:** 2026-01-10

- ✅ Todas las tablas tienen PK definida
- ✅ Todas las FKs tienen constraints explícitos
- ✅ Los tipos ENUM coinciden entre SQL y Java
- ✅ Los índices están definidos para consultas frecuentes
- ✅ Los check constraints protegen invariantes críticas

---

**FIN DEL DOCUMENTO**
