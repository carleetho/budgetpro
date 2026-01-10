# 📊 DICCIONARIO DE DATOS - BUDGETPRO

> **Meta-Info:**
> - **Fecha de Generación:** 2026-01-10
> - **Fuente:** Migraciones Flyway (`src/main/resources/db/migration/`) y JPA Entities
> - **Estado:** Baseline Reconstruido (Forensic Analysis)
> - **Última Actualización:** 2026-01-10

---

## 🎯 PROPÓSITO

Este documento es el **Diccionario de Datos** de BUDGETPRO, reconstruido mediante ingeniería inversa de las migraciones Flyway y las entidades JPA existentes. Refleja la estructura física real de las tablas, sus columnas, tipos SQL, constraints, índices y mapeos JPA.

**REGLAS CRÍTICAS:**
- Documenta SOLO lo que está en el código (migraciones SQL y entidades JPA)
- Marca elementos faltantes si es necesario
- Usa Markdown profesional optimizado para lectura humana y parsing de IA

---

## 📋 TABLAS DEL SISTEMA

### 1. `proyecto`

**Descripción:** Tabla que representa un proyecto en el sistema. Es un "proyecto pasivo del sistema" usado principalmente para integridad referencial.

**Migración:** `V3__create_presupuesto_partida_schema.sql` (creado con `CREATE TABLE IF NOT EXISTS`)

**Mapeo JPA:** ⚠️ **NO existe entidad JPA correspondiente** (es un proyecto pasivo del sistema)

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | - | Identificador único |
| `nombre` | `TEXT` | - | `NO` | - | - | Nombre del proyecto |
| `estado` | `VARCHAR(30)` | - | `NO` | - | - | Estado del proyecto |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | - | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | - | Auditoría: Fecha de actualización |

**Índices:**
- `idx_proyecto_estado (estado)` - Búsqueda por estado

**Constraints:**
- `PRIMARY KEY (id)`

**Nota:** Esta tabla fue creada con `CREATE TABLE IF NOT EXISTS` para soportar FKs de `presupuesto` y `partida`. No tiene entidad JPA correspondiente (es un proyecto pasivo).

---

### 2. `presupuesto`

**Descripción:** Tabla que representa un presupuesto de un proyecto.

**Migración:** `V3__create_presupuesto_partida_schema.sql`

**Mapeo JPA:** `com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity`

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `proyecto_id` | `UUID` | `FK → proyecto(id)` | `NO` | - | `UUID proyectoId` | Relación N:1 con proyecto |
| `nombre` | `VARCHAR(200)` | - | `NO` | - | `String nombre` | Nombre del presupuesto |
| `total_asignado` | `NUMERIC(19,4)` | - | `NO` | `0` | `BigDecimal totalAsignado` | Total asignado (escala 4 decimales) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime updatedAt` | Auditoría: Fecha de actualización |

**Índices:**
- `idx_presupuesto_proyecto (proyecto_id)` - Búsqueda por proyecto

**Constraints:**
- `PRIMARY KEY (id)`
- `FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)` - Integridad referencial

**Relaciones JPA:**
- `@OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)` → `List<PartidaEntity> partidas`

**Nota:** Esta es una entidad técnica para soportar la FK de `partida`. No tiene agregado de dominio correspondiente aún (solo la entidad técnica).

---

### 3. `partida`

**Descripción:** Tabla que representa una partida presupuestaria de un proyecto.

**Migración:** `V3__create_presupuesto_partida_schema.sql`

**Mapeo JPA:** `com.budgetpro.infrastructure.persistence.entity.PartidaEntity`

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `presupuesto_id` | `UUID` | `FK → presupuesto(id)`, `UNIQUE(presupuesto_id, codigo)` | `NO` | - | `PresupuestoEntity presupuesto` (bidireccional) | Relación N:1 con presupuesto |
| `proyecto_id` | `UUID` | `FK → proyecto(id)` | `NO` | - | `UUID proyectoId` | ID del proyecto (denormalizado para consultas) |
| `codigo` | `VARCHAR(50)` | `UNIQUE(presupuesto_id, codigo)` | `NO` | - | `String codigo` | Código único de la partida (ej: "MAT-01") |
| `nombre` | `TEXT` | - | `NO` | - | `String nombre` | Nombre/descripción de la partida |
| `tipo` | `VARCHAR(20)` | - | `NO` | - | `TipoRecurso tipo` (Enum) | Tipo de recurso (MATERIAL, MANO_OBRA, etc) |
| `monto_presupuestado` | `NUMERIC(19,4)` | `CHECK (monto_presupuestado >= 0)` | `NO` | `0` | `BigDecimal montoPresupuestado` | Monto presupuestado (escala 4 decimales) |
| `monto_reservado` | `NUMERIC(19,4)` | `CHECK (monto_reservado >= 0)` | `NO` | `0` | `BigDecimal montoReservado` | Monto reservado (escala 4 decimales) |
| `monto_ejecutado` | `NUMERIC(19,4)` | `CHECK (monto_ejecutado >= 0)` | `NO` | `0` | `BigDecimal montoEjecutado` | Monto ejecutado (escala 4 decimales) |
| `estado` | `estado_partida` (ENUM) | - | `NO` | `'BORRADOR'` | `EstadoPartida estado` (Enum) | Estado de la partida (BORRADOR, APROBADA, CERRADA) |
| `version` | `BIGINT` | - | `NO` | `0` | `Long version` | Optimistic Locking (Hibernate @Version) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime updatedAt` | Auditoría: Fecha de actualización |

**Índices:**
- `idx_partida_presupuesto (presupuesto_id)` - Búsqueda por presupuesto
- `idx_partida_proyecto (proyecto_id)` - Búsqueda por proyecto
- `idx_partida_codigo (codigo)` - Búsqueda por código
- `idx_partida_estado (estado)` - Filtrado por estado
- `idx_partida_tipo (tipo)` - Filtrado por tipo

**Constraints:**
- `PRIMARY KEY (id)`
- `UNIQUE (presupuesto_id, codigo)` - Garantiza código único por presupuesto
- `FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id)` - Integridad referencial
- `FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)` - Integridad referencial
- `CHECK (monto_presupuestado >= 0)` - Invariante: Monto presupuestado nunca negativo
- `CHECK (monto_reservado >= 0)` - Invariante: Monto reservado nunca negativo
- `CHECK (monto_ejecutado >= 0)` - Invariante: Monto ejecutado nunca negativo
- `CHECK (monto_presupuestado - (monto_reservado + monto_ejecutado) >= 0)` - Invariante: Saldo disponible nunca negativo

**Relaciones JPA:**
- `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "presupuesto_id")` → `PresupuestoEntity presupuesto` (bidireccional)

**Mapeo de Enums:**
- `tipo`: `@JdbcTypeCode(SqlTypes.VARCHAR)` + `@Enumerated(EnumType.STRING)` → `TipoRecurso` (dominio)
- `estado`: `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` + `@Enumerated(EnumType.STRING)` → `EstadoPartida` (dominio)

**Nota:** La tabla tiene un campo `proyecto_id` denormalizado para facilitar consultas por proyecto sin necesidad de hacer JOIN con `presupuesto`.

---

### 4. `recurso`

**Descripción:** Tabla del Shared Kernel — Catálogo de Recursos.

**Migración:** `V1__create_recurso_types_and_table.sql`

**Mapeo JPA:** `com.budgetpro.infrastructure.persistence.entity.RecursoEntity`

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `nombre` | `TEXT` | - | `NO` | - | `String nombre` | Nombre del recurso |
| `nombre_normalizado` | `TEXT` | `UNIQUE` | `NO` | - | `String nombreNormalizado` | Nombre normalizado (deduplicación semántica) |
| `tipo` | `recurso_tipo` (ENUM) | - | `NO` | - | `TipoRecurso tipo` (Enum) | Tipo de recurso (MATERIAL, MANO_OBRA, SUBCONTRATO, ACTIVO) |
| `unidad_base` | `VARCHAR(20)` | - | `NO` | - | `String unidadBase` | Unidad de medida base |
| `atributos` | `JSONB` | - | `NO` | `'{}'` | `Map<String, Object> atributos` | Atributos adicionales (JSON) |
| `estado` | `recurso_estado` (ENUM) | - | `NO` | `'ACTIVO'` | `EstadoRecurso estado` (Enum) | Estado del recurso (ACTIVO, EN_REVISION, DEPRECADO) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime updatedAt` | Auditoría: Fecha de actualización |
| `created_by` | `UUID` | - | `NO` | - | `UUID createdBy` | Auditoría: Usuario creador |

**Índices:**
- `idx_recurso_tipo (tipo)` - Búsqueda por tipo
- `idx_recurso_estado (estado)` - Filtrado por estado

**Constraints:**
- `PRIMARY KEY (id)`
- `UNIQUE (nombre_normalizado)` - Garantiza nombre único normalizado

**Mapeo de Enums:**
- `tipo`: `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` + `@Enumerated(EnumType.STRING)` → `TipoRecurso` (dominio)
- `estado`: `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` + `@Enumerated(EnumType.STRING)` → `EstadoRecurso` (dominio)

**Mapeo JSONB:**
- `atributos`: `@JdbcTypeCode(SqlTypes.JSON)` → `Map<String, Object>`

---

### 5. `billetera`

**Descripción:** Billetera financiera de un proyecto. Relación 1:1 con proyecto (UNIQUE constraint).

**Migración:** `V2__create_finanzas_schema.sql`

**Mapeo JPA:** ⚠️ **NOTA:** Basado en `MODULO_FINANZAS_DOMINIO.md`, la migración `V2__create_finanzas_schema.sql` debería existir, pero no se encontró en el sistema de archivos actual. Esta sección se documenta basándose en la referencia.

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `proyecto_id` | `UUID` | `UNIQUE`, `FK → proyecto(id)` | `NO` | - | `UUID proyectoId` | Relación 1:1 con proyecto |
| `saldo_actual` | `NUMERIC(19,4)` | `CHECK (saldo_actual >= 0)` | `NO` | `0` | `BigDecimal saldoActual` | Saldo actual (escala 4 decimales) |
| `version` | `BIGINT` | - | `NO` | `0` | `Long version` | Optimistic Locking (Hibernate @Version) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |
| `updated_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime updatedAt` | Auditoría: Fecha de actualización |

**Índices:**
- `idx_billetera_proyecto_id (proyecto_id)` - Búsqueda rápida por proyecto

**Constraints:**
- `PRIMARY KEY (id)`
- `UNIQUE (proyecto_id)` - Garantiza relación 1:1 con proyecto
- `FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)` - Integridad referencial
- `CHECK (saldo_actual >= 0)` - Invariante: Saldo nunca negativo

**Nota:** Esta tabla fue referenciada en documentación pero la migración `V2__create_finanzas_schema.sql` no existe en el sistema de archivos actual.

---

### 6. `movimiento_caja`

**Descripción:** Movimientos de caja (ingresos y egresos) de una billetera.

**Migración:** `V2__create_finanzas_schema.sql`

**Mapeo JPA:** ⚠️ **NOTA:** Basado en `MODULO_FINANZAS_DOMINIO.md`, esta tabla debería existir, pero la migración no se encontró. Esta sección se documenta basándose en la referencia.

| Columna | Tipo SQL | Constraints | Nullable | Default | Mapeo JPA | Notas |
|---------|----------|-------------|----------|---------|-----------|-------|
| `id` | `UUID` | `PRIMARY KEY` | `NO` | - | `UUID id` | Identificador único |
| `billetera_id` | `UUID` | `FK → billetera(id) ON DELETE CASCADE` | `NO` | - | `BilleteraEntity billetera` (bidireccional) | Relación N:1 con billetera |
| `monto` | `NUMERIC(19,4)` | - | `NO` | - | `BigDecimal monto` | Monto del movimiento (escala 4 decimales) |
| `tipo` | `VARCHAR(20)` | `CHECK (tipo IN ('INGRESO', 'EGRESO'))` | `NO` | - | `TipoMovimiento tipo` (Enum) | Tipo de movimiento (INGRESO, EGRESO) |
| `fecha` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime fecha` | Fecha del movimiento |
| `referencia` | `VARCHAR(255)` | - | `NO` | - | `String referencia` | Descripción/referencia del movimiento |
| `evidencia_url` | `VARCHAR(500)` | - | `YES` | - | `String evidenciaUrl` | URL opcional de evidencia documental |
| `estado` | `VARCHAR(20)` | - | `NO` | `'ACTIVO'` | `String estado` | Estado del movimiento (solo persistencia, no dominio) |
| `created_at` | `TIMESTAMP` | - | `NO` | `now()` | `LocalDateTime createdAt` | Auditoría: Fecha de creación |

**Índices:**
- `idx_movimiento_caja_billetera (billetera_id)` - Búsqueda por billetera
- `idx_movimiento_caja_fecha (fecha DESC)` - Ordenamiento por fecha
- `idx_movimiento_caja_tipo (tipo)` - Filtrado por tipo
- `idx_movimiento_caja_estado (estado)` - Filtrado por estado

**Constraints:**
- `PRIMARY KEY (id)`
- `FOREIGN KEY (billetera_id) REFERENCES billetera(id) ON DELETE CASCADE` - Integridad referencial con cascade delete
- `CHECK (tipo IN ('INGRESO', 'EGRESO'))` - Validación de tipo

**Relaciones JPA:**
- `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "billetera_id")` → `BilleteraEntity billetera` (bidireccional)

**Nota:** Esta tabla fue referenciada en documentación pero la migración `V2__create_finanzas_schema.sql` no existe en el sistema de archivos actual.

---

## 🔗 RELACIONES ENTRE TABLAS

### Diagrama de Relaciones

```
proyecto (1) ──< (N) presupuesto
proyecto (1) ──< (N) partida [proyecto_id denormalizado]
presupuesto (1) ──< (N) partida
partida (N) ──< (1) presupuesto [FK principal]
```

**Relaciones Documentadas:**

1. **`proyecto` → `presupuesto`** (1:N)
   - **FK:** `presupuesto.proyecto_id → proyecto.id`
   - **JPA:** `PresupuestoEntity.proyectoId` (UUID, sin relación bidireccional)

2. **`proyecto` → `partida`** (1:N) [Denormalizado]
   - **FK:** `partida.proyecto_id → proyecto.id`
   - **JPA:** `PartidaEntity.proyectoId` (UUID, denormalizado para consultas)

3. **`presupuesto` → `partida`** (1:N)
   - **FK:** `partida.presupuesto_id → presupuesto.id`
   - **JPA:** `PartidaEntity.presupuesto` (bidireccional: `@ManyToOne` ↔ `@OneToMany`)
   - **Constraint UNIQUE:** `UNIQUE (presupuesto_id, codigo)` - Código único por presupuesto

---

## 📊 TIPOS ENUMERADOS (PostgreSQL ENUM)

### 1. `recurso_tipo`

**Valores:**
- `MATERIAL`
- `MANO_OBRA`
- `SUBCONTRATO`
- `ACTIVO`

**Migración:** `V1__create_recurso_types_and_table.sql`

**Mapeo Java:** `com.budgetpro.domain.recurso.model.TipoRecurso` (Enum)

**Uso:** Campo `recurso.tipo`, `partida.tipo` (VARCHAR en BD, enum en dominio)

---

### 2. `recurso_estado`

**Valores:**
- `ACTIVO`
- `EN_REVISION`
- `DEPRECADO`

**Migración:** `V1__create_recurso_types_and_table.sql`

**Mapeo Java:** `com.budgetpro.domain.recurso.model.EstadoRecurso` (Enum)

**Uso:** Campo `recurso.estado`

---

### 3. `estado_partida`

**Valores:**
- `BORRADOR`
- `APROBADA`
- `CERRADA`

**Migración:** `V3__create_presupuesto_partida_schema.sql`

**Mapeo Java:** `com.budgetpro.domain.finanzas.partida.EstadoPartida` (Enum)

**Uso:** Campo `partida.estado`

---

## 🔍 CONSTRAINTS Y VALIDACIONES

### Check Constraints

| Tabla | Constraint | Descripción | Validación |
|-------|-----------|-------------|------------|
| `partida` | `chk_partida_monto_presupuestado_no_negativo` | Monto presupuestado >= 0 | `monto_presupuestado >= 0` |
| `partida` | `chk_partida_monto_reservado_no_negativo` | Monto reservado >= 0 | `monto_reservado >= 0` |
| `partida` | `chk_partida_monto_ejecutado_no_negativo` | Monto ejecutado >= 0 | `monto_ejecutado >= 0` |
| `partida` | `chk_partida_saldo_disponible_no_negativo` | Saldo disponible >= 0 | `monto_presupuestado - (monto_reservado + monto_ejecutado) >= 0` |

**Nota:** Los check constraints son validaciones de respaldo en BD. La validación principal está en el dominio mediante `PresupuestoExcedidoException`.

---

### Unique Constraints

| Tabla | Constraint | Columnas | Descripción |
|-------|-----------|----------|-------------|
| `recurso` | `uq_recurso_nombre` | `nombre_normalizado` | Nombre único normalizado |
| `partida` | `uq_partida_presupuesto_codigo` | `presupuesto_id`, `codigo` | Código único por presupuesto |

**Nota:** La constraint `uq_partida_presupuesto_codigo` permite que el mismo código exista en diferentes presupuestos, pero no en el mismo presupuesto.

---

### Foreign Keys

| Tabla | FK | Referencia | On Delete | Descripción |
|-------|----|-----------|-----------|-------------|
| `presupuesto` | `fk_presupuesto_proyecto` | `proyecto_id → proyecto(id)` | - | Cada presupuesto pertenece a un proyecto |
| `partida` | `fk_partida_presupuesto` | `presupuesto_id → presupuesto(id)` | - | Cada partida pertenece a un presupuesto |
| `partida` | `fk_partida_proyecto` | `proyecto_id → proyecto(id)` | - | Cada partida pertenece a un proyecto (denormalizado) |

---

## 🗂️ ÍNDICES

### Índices por Tabla

#### `proyecto`
- `idx_proyecto_estado (estado)` - Búsqueda por estado

#### `presupuesto`
- `idx_presupuesto_proyecto (proyecto_id)` - Búsqueda por proyecto

#### `partida`
- `idx_partida_presupuesto (presupuesto_id)` - Búsqueda por presupuesto
- `idx_partida_proyecto (proyecto_id)` - Búsqueda por proyecto (denormalizado)
- `idx_partida_codigo (codigo)` - Búsqueda por código
- `idx_partida_estado (estado)` - Filtrado por estado
- `idx_partida_tipo (tipo)` - Filtrado por tipo

#### `recurso`
- `idx_recurso_tipo (tipo)` - Búsqueda por tipo
- `idx_recurso_estado (estado)` - Filtrado por estado

---

## 📝 NOTAS DE IMPLEMENTACIÓN

### 1. Precisión Numérica (NUMERIC 19,4)

**Tablas afectadas:** `presupuesto`, `partida`

**Regla:**
- Todos los montos monetarios usan `NUMERIC(19,4)` (escala 4 decimales)
- Alineado con el ERD físico y el Value Object `Monto` del dominio (escala 4, redondeo HALF_EVEN)

**Validación:**
- El mapper (`PartidaMapper`) normaliza `BigDecimal` a escala 4 con redondeo HALF_EVEN
- El Value Object `Monto` del dominio garantiza escala 4

---

### 2. Optimistic Locking

**Tabla afectada:** `partida`

**Implementación:**
- Campo `version BIGINT` en BD
- Campo `version Long` en JPA Entity con anotación `@Version`
- Hibernate incrementa automáticamente `version` tras cada UPDATE
- El adapter verifica `version` antes de actualizar (lanza `OptimisticLockingFailureException` si no coincide)

---

### 3. Normalización de Códigos

**Tabla afectada:** `partida`

**Regla:**
- El campo `codigo` se normaliza en el dominio (Trim + UpperCase) mediante `CodigoPartida`
- El repositorio verifica duplicados usando el código normalizado
- La BD almacena el código normalizado (ej: "MAT-01")

---

### 4. Estados y Enums

**Mapeo:**
- `partida.estado`: PostgreSQL ENUM (`estado_partida`) → Java Enum (`EstadoPartida`) usando `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`
- `partida.tipo`: VARCHAR(20) → Java Enum (`TipoRecurso`) usando `@JdbcTypeCode(SqlTypes.VARCHAR)` + `@Enumerated(EnumType.STRING)`
- `recurso.tipo`: PostgreSQL ENUM (`recurso_tipo`) → Java Enum (`TipoRecurso`) usando `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`
- `recurso.estado`: PostgreSQL ENUM (`recurso_estado`) → Java Enum (`EstadoRecurso`) usando `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`

---

### 5. Campos Denormalizados

**Tabla `partida`:**
- `proyecto_id`: Campo denormalizado para facilitar consultas por proyecto sin necesidad de JOIN con `presupuesto`
- Permite `findByProyectoId()` eficiente sin JOINs

---

### 6. Relación Bidireccional Presupuesto ↔ Partida

**Implementación JPA:**
- `PresupuestoEntity`: `@OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)` → `List<PartidaEntity> partidas`
- `PartidaEntity`: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "presupuesto_id")` → `PresupuestoEntity presupuesto`
- `CascadeType.ALL`: Todas las operaciones se propagan a partidas hijos
- `orphanRemoval = true`: Elimina partidas huérfanas al removerlas de la colección

---

## 🚨 ELEMENTOS FALTANTES O PENDIENTES

### ⚠️ Tablas Referenciadas pero Sin Migración Visible

1. **`billetera`** y **`movimiento_caja`**: 
   - Referenciadas en `MODULO_FINANZAS_DOMINIO.md`
   - Migración esperada: `V2__create_finanzas_schema.sql`
   - **Estado:** ⚠️ Migración no encontrada en sistema de archivos actual
   - **Nota:** Puede haber sido eliminada en el refactoring o no haberse creado aún

---

## 📋 MIGRACIONES FLYWAY

| Versión | Archivo | Descripción | Estado |
|---------|---------|-------------|--------|
| `V1` | `V1__create_recurso_types_and_table.sql` | Tipos enumerados y tabla `recurso` | ✅ Existe |
| `V2` | `V2__create_finanzas_schema.sql` | Tablas `proyecto`, `billetera`, `movimiento_caja` | ⚠️ No encontrada |
| `V3` | `V3__create_presupuesto_partida_schema.sql` | Tablas `proyecto` (IF NOT EXISTS), `presupuesto`, `partida`, tipos enumerados | ✅ Existe |

**Nota:** La migración `V3` crea la tabla `proyecto` con `CREATE TABLE IF NOT EXISTS` para evitar conflictos si `V2` ya la creó.

---

**FIN DEL DOCUMENTO**
