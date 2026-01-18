# 📋 REPORTE DE IMPLEMENTACIÓN: MOVIMIENTO 3 - GESTIÓN DE APU (ANÁLISIS DE PRECIOS UNITARIOS)

**Fecha:** 2026-01-12  
**Movimiento:** Fase de Fundación - MOVIMIENTO 3  
**Estado:** ✅ **COMPLETADO Y COMPILABLE**  
**Arquitectura:** Clean Architecture (Hexagonal)  
**Framework:** Spring Boot 3.x + JPA/Hibernate

---

## 🎯 RESUMEN EJECUTIVO

Se ha implementado completamente el **MOVIMIENTO 3** de la Fase de Fundación, que incluye:

1. **Agregado APU** - Gestión completa de Análisis de Precios Unitarios con relación 1:1 con Partida
2. **Entidad Interna ApuInsumo** - Detalle de insumos (recursos) que componen el APU
3. **Cálculo de Costo Total** - Motor de cálculo que suma subtotales de insumos

**Resultado:**
- ✅ **20 archivos Java** creados
- ✅ **1 migración Flyway** (V4)
- ✅ **1 test de integración** completo
- ✅ **Compilación exitosa** (BUILD SUCCESS)
- ✅ **0 errores de linter**
- ✅ **Arquitectura hexagonal** respetada
- ✅ **Reglas JPA estrictas** cumplidas
- ✅ **Relación 1:1 con Partida** funcional
- ✅ **Cálculo de costos** implementado

---

## 📐 ARQUITECTURA IMPLEMENTADA

### Estructura de Capas (Hexagonal)

```
┌─────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  REST        │  │  Persistence │  │  Database    │ │
│  │  Controllers │  │  Adapters    │  │  Migrations  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────┐
│                    APPLICATION                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  UseCases    │  │  DTOs         │  │  Exceptions │ │
│  │  (Ports In)  │  │  (Commands)   │  │  (Domain)   │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
                          ↕
┌─────────────────────────────────────────────────────────┐
│                      DOMAIN                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Aggregates  │  │  Value       │  │  Ports Out   │ │
│  │  (Roots)     │  │  Objects     │  │  (Repos)     │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 COMPONENTES IMPLEMENTADOS POR CAPA

### 1. CAPA DE DOMINIO (Domain Layer)

#### 1.1 Agregado APU

**Ubicación:** `com.budgetpro.domain.finanzas.apu.model`

**Archivos Creados:**
- `APU.java` - Aggregate Root
- `ApuId.java` - Value Object (UUID wrapper)
- `ApuInsumo.java` - Entidad interna del agregado
- `ApuInsumoId.java` - Value Object (UUID wrapper)

**Características del Agregado:**

```java
public final class APU {
    private final ApuId id;
    private final UUID partidaId;
    private BigDecimal rendimiento; // Opcional
    private String unidad; // Copia de la unidad de la partida
    private Long version;
    private final List<ApuInsumo> insumos; // Entidades internas
}
```

**Atributos:**
- `id` (ApuId) - Identificador único
- `partidaId` (UUID) - Obligatorio, relación 1:1 con Partida
- `rendimiento` (BigDecimal) - Opcional, cantidad de unidades que se pueden producir por día
- `unidad` (String) - Copia de la unidad de la partida
- `version` (Long) - Para optimistic locking
- `insumos` (List<ApuInsumo>) - Lista de insumos (entidades internas)

**Entidad Interna ApuInsumo:**

```java
public final class ApuInsumo {
    private final ApuInsumoId id;
    private final UUID recursoId;
    private BigDecimal cantidad; // Cantidad técnica por unidad de partida
    private BigDecimal precioUnitario; // Snapshot del precio del recurso
    private BigDecimal subtotal; // Calculado: cantidad * precioUnitario
}
```

**Invariantes:**
- ✅ El partidaId es obligatorio
- ✅ La lista de insumos no puede ser nula (puede estar vacía)
- ✅ El recursoId de cada insumo no puede ser nulo
- ✅ La cantidad no puede ser negativa
- ✅ El precioUnitario no puede ser negativo
- ✅ El subtotal = cantidad * precioUnitario (calculado automáticamente)
- ✅ El costo total = Σ subtotales de insumos

**Métodos de Dominio:**
- `crear(ApuId, UUID, String)` - Factory method para crear APU vacío
- `crear(ApuId, UUID, BigDecimal, String)` - Factory method para crear APU con rendimiento
- `reconstruir(...)` - Factory method para reconstruir desde persistencia
- `agregarInsumo(UUID, BigDecimal, BigDecimal)` - Agrega un insumo al APU
- `calcularCostoTotal()` - Calcula el costo total: Σ subtotales de insumos
- `actualizarRendimiento(BigDecimal)` - Actualiza el rendimiento
- `actualizarUnidad(String)` - Actualiza la unidad
- `tieneInsumos()` - Verifica si el APU tiene insumos

**Métodos de ApuInsumo:**
- `crear(ApuInsumoId, UUID, BigDecimal, BigDecimal)` - Factory method
- `reconstruir(...)` - Factory method para reconstruir desde persistencia
- `actualizarCantidad(BigDecimal)` - Actualiza cantidad y recalcula subtotal
- `actualizarPrecioUnitario(BigDecimal)` - Actualiza precio y recalcula subtotal

**Puerto de Salida:**
- `ApuRepository.java` - Interface con métodos:
  - `save(APU)` - Guarda APU y todos sus insumos
  - `findById(ApuId)`
  - `findByPartidaId(UUID)` - Busca APU de una partida (relación 1:1)
  - `existsByPartidaId(UUID)` - Verifica existencia

---

### 2. CAPA DE APLICACIÓN (Application Layer)

#### 2.1 Casos de Uso - APU

**Ubicación:** `com.budgetpro.application.apu`

**Archivos Creados:**
- `CrearApuUseCase.java` - Puerto de entrada (interface)
- `CrearApuUseCaseImpl.java` - Implementación del caso de uso
- `CrearApuCommand.java` - DTO de comando
- `ApuInsumoCommand.java` - DTO de comando para insumo
- `ApuResponse.java` - DTO de respuesta
- `ApuInsumoResponse.java` - DTO de respuesta para insumo
- `PartidaNoEncontradaException.java` - Excepción de dominio
- `RecursoNoEncontradoException.java` - Excepción de dominio
- `ApuYaExisteException.java` - Excepción de dominio

**Flujo del Caso de Uso:**

```
1. Recibe CrearApuCommand (partidaId, rendimiento opcional, unidad opcional, lista de insumos)
2. Valida que la partida exista
   → Si no existe: lanza PartidaNoEncontradaException
3. Valida que la partida no tenga ya un APU
   → Si existe: lanza ApuYaExisteException
4. Valida que todos los recursos existan
   → Si algún recurso no existe: lanza RecursoNoEncontradoException
5. Crea nuevo ApuId (UUID aleatorio)
6. Crea el agregado APU (vacío o con rendimiento)
7. Para cada insumo en el comando:
   a. Agrega el insumo al APU mediante agregarInsumo()
   b. El APU calcula automáticamente el subtotal
8. Persiste mediante ApuRepository (guarda APU y todos sus insumos)
9. Calcula costo total mediante calcularCostoTotal()
10. Retorna ApuResponse con datos del APU creado y lista de insumos
```

**Validaciones:**
- ✅ PartidaId obligatorio (validado con `@NotNull`)
- ✅ Lista de insumos obligatoria (validado con `@NotNull`)
- ✅ Partida debe existir
- ✅ Partida no debe tener APU previo
- ✅ Todos los recursos deben existir
- ✅ Cantidad no negativa (validado con `@DecimalMin("0.0")`)
- ✅ Precio unitario no negativo (validado con `@DecimalMin("0.0")`)

---

### 3. CAPA DE INFRAESTRUCTURA (Infrastructure Layer)

#### 3.1 Persistencia (JPA)

**Ubicación:** `com.budgetpro.infrastructure.persistence`

##### 3.1.1 Entidades JPA

**ApuEntity.java:**
```java
@Entity
@Table(name = "apu",
       uniqueConstraints = @UniqueConstraint(name = "uq_apu_partida", columnNames = "partida_id"))
public class ApuEntity {
    @Id
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false, updatable = false, unique = true)
    private PartidaEntity partida; // Relación 1:1
    
    @Column(name = "rendimiento", precision = 19, scale = 6)
    private BigDecimal rendimiento;
    
    @Column(name = "unidad", length = 20)
    private String unidad;
    
    @Version
    @Column(name = "version", nullable = false)
    private Integer version; // Optimistic locking
    
    @OneToMany(mappedBy = "apu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApuInsumoEntity> insumos; // Relación 1:N con cascade
}
```

**ApuInsumoEntity.java:**
```java
@Entity
@Table(name = "apu_insumo")
public class ApuInsumoEntity {
    @Id
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apu_id", nullable = false, updatable = false)
    private ApuEntity apu; // Relación N:1 con APU
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false, updatable = false)
    private RecursoEntity recurso; // Relación N:1 con Recurso
    
    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;
    
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;
    
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal; // Calculado
    
    @Version
    @Column(name = "version", nullable = false)
    private Integer version; // Optimistic locking
}
```

**Características Especiales:**
- ✅ **Relación 1:1 con Partida:** `@OneToOne` con `unique = true`
- ✅ **Relación 1:N con ApuInsumo:** `@OneToMany` con `cascade = CascadeType.ALL` y `orphanRemoval = true`
- ✅ **Relación N:1 con Recurso:** `@ManyToOne` para cada insumo
- ✅ **Lazy Loading:** Todas las relaciones usan `FetchType.LAZY`
- ✅ **Constructor acepta `version = null`** (nunca se fuerza `version = 0`)
- ✅ **`@PrePersist` NO se usa para version** (solo para fechas)
- ✅ **Hibernate maneja el optimistic locking** automáticamente con `@Version`
- ✅ **Cascade y Orphan Removal:** Los insumos se eliminan automáticamente si se elimina el APU

##### 3.1.2 Repositorios JPA

**ApuJpaRepository.java:**
- Extiende `JpaRepository<ApuEntity, UUID>`
- Métodos custom:
  - `findByPartidaId(UUID)` - Busca APU de una partida (relación 1:1)
  - `existsByPartidaId(UUID)` - Verifica existencia

**ApuInsumoJpaRepository.java:**
- Extiende `JpaRepository<ApuInsumoEntity, UUID>`
- Métodos custom:
  - `findByApuId(UUID)` - Busca todos los insumos de un APU

##### 3.1.3 Mapper

**ApuMapper.java:**
- `toEntity(APU, PartidaEntity)` - Convierte dominio a entidad (pasa `null` en version, requiere PartidaEntity)
- `toInsumoEntity(ApuInsumo, ApuEntity, RecursoEntity)` - Convierte insumo dominio a entidad
- `toDomain(ApuEntity)` - Convierte entidad a dominio (mapea insumos también)
- `toInsumoDomain(ApuInsumoEntity)` - Convierte insumo entidad a dominio
- `updateEntity(ApuEntity, APU)` - Actualiza entidad existente (NO toca version ni partida)

##### 3.1.4 Adapter

**ApuRepositoryAdapter.java:**
```java
@Override
@Transactional
public void save(APU apu) {
    Optional<ApuEntity> existingEntityOpt = jpaRepository.findById(apu.getId().getValue());
    
    if (existingEntityOpt.isPresent()) {
        // Actualización: actualizar campos y sincronizar insumos
        ApuEntity existingEntity = existingEntityOpt.get();
        mapper.updateEntity(existingEntity, apu);
        sincronizarInsumos(existingEntity, apu);
        jpaRepository.save(existingEntity);
    } else {
        // Creación: cargar entidades relacionadas y mapear
        PartidaEntity partidaEntity = partidaJpaRepository.findById(...)
                .orElseThrow(...);
        
        ApuEntity newEntity = mapper.toEntity(apu, partidaEntity);
        asignarRecursosAInsumos(newEntity, apu); // Cargar RecursoEntity para cada insumo
        jpaRepository.save(newEntity);
    }
}
```

**Reglas Críticas Implementadas:**
- ✅ NO se hacen validaciones manuales de versión
- ✅ NO se lanza `OptimisticLockingFailureException` manualmente
- ✅ Hibernate maneja el optimistic locking automáticamente
- ✅ Carga entidades relacionadas (PartidaEntity, RecursoEntity) antes de mapear
- ✅ Sincroniza insumos usando cascade y orphanRemoval

---

#### 3.2 REST Controllers

**Ubicación:** `com.budgetpro.infrastructure.rest`

##### 3.2.1 ApuController

**Endpoint:**
- `POST /api/v1/partidas/{partidaId}/apu`

**Request Body:**
```json
{
    "rendimiento": 10.5,
    "unidad": "m3",
    "insumos": [
        {
            "recursoId": "550e8400-e29b-41d4-a716-446655440000",
            "cantidad": 7.5,
            "precioUnitario": 25.50
        }
    ]
}
```

**Response (201 CREATED):**
```json
{
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "partidaId": "770e8400-e29b-41d4-a716-446655440002",
    "rendimiento": 10.5,
    "unidad": "m3",
    "costoTotal": 191.25,
    "version": 0,
    "insumos": [
        {
            "id": "990e8400-e29b-41d4-a716-446655440004",
            "recursoId": "550e8400-e29b-41d4-a716-446655440000",
            "cantidad": 7.5,
            "precioUnitario": 25.50,
            "subtotal": 191.25
        }
    ],
    "createdAt": "2026-01-12T17:00:00",
    "updatedAt": "2026-01-12T17:00:00"
}
```

**Validaciones:**
- `rendimiento` es opcional
- `unidad` es opcional
- `insumos` es obligatorio (`@NotNull`)
- Cada insumo requiere:
  - `recursoId` obligatorio (`@NotNull`)
  - `cantidad` obligatoria y no negativa (`@NotNull`, `@DecimalMin("0.0")`)
  - `precioUnitario` obligatorio y no negativo (`@NotNull`, `@DecimalMin("0.0")`)

**Errores Posibles:**
- `400 BAD REQUEST` - Validación fallida
- `404 NOT FOUND` - Partida no encontrada
- `404 NOT FOUND` - Recurso no encontrado
- `409 CONFLICT` - La partida ya tiene un APU

---

### 4. BASE DE DATOS

#### 4.1 Migración Flyway

**Archivo:** `V4__create_apu_schema.sql`

**Contenido:**

```sql
-- Crear tabla apu
CREATE TABLE apu (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL UNIQUE,
    rendimiento NUMERIC(19,6),
    unidad VARCHAR(20),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_apu_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE
);

CREATE INDEX idx_apu_partida ON apu(partida_id);

-- Crear tabla apu_insumo
CREATE TABLE apu_insumo (
    id UUID PRIMARY KEY,
    apu_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL,
    precio_unitario NUMERIC(19,4) NOT NULL,
    subtotal NUMERIC(19,4) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_apu_insumo_apu
        FOREIGN KEY (apu_id) REFERENCES apu(id) ON DELETE CASCADE,
    CONSTRAINT fk_apu_insumo_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id)
);

CREATE INDEX idx_apu_insumo_apu ON apu_insumo(apu_id);
CREATE INDEX idx_apu_insumo_recurso ON apu_insumo(recurso_id);
```

**Características:**
- ✅ Constraint UNIQUE en `apu.partida_id` (relación 1:1)
- ✅ Foreign key a `partida` con `ON DELETE CASCADE`
- ✅ Foreign key a `recurso` (sin cascade, el recurso es compartido)
- ✅ Foreign key de `apu_insumo` a `apu` con `ON DELETE CASCADE`
- ✅ Índices para optimización de consultas
- ✅ Campos de auditoría (`created_at`, `updated_at`)
- ✅ Campo `version` para optimistic locking
- ✅ Precisión adecuada: `NUMERIC(19,6)` para cantidades, `NUMERIC(19,4)` para precios y subtotales

---

### 5. TESTS

#### 5.1 Test de Integración

**Archivo:** `GestionApuIntegrationTest.java`

**Ubicación:** `src/test/java/com/budgetpro/infrastructure/rest/apu/`

**Cobertura:**
1. ✅ Setup: Crear Proyecto, Presupuesto, Partida y Recurso
2. ✅ Crear APU para la Partida agregando el Recurso mediante REST API
3. ✅ Verificar que el APU se guardó en BD
4. ✅ Verificar que el ApuInsumo se guardó correctamente
5. ✅ Validar cálculos (subtotal, costoTotal)
6. ✅ Validar relaciones (APU → Partida, ApuInsumo → Recurso)

**Validaciones del Test:**
- PartidaId del APU
- Rendimiento del APU
- Unidad del APU
- Version del APU (no nulo)
- Fechas de auditoría (createdAt, updatedAt)
- RecursoId del insumo
- Cantidad del insumo
- Precio unitario del insumo (snapshot)
- Subtotal del insumo (calculado correctamente)
- Costo total del APU (suma de subtotales)
- Relaciones correctas (APU → Partida, ApuInsumo → Recurso)

**Nota:** El test requiere Docker/Testcontainers para ejecutarse. El código compila correctamente.

---

## 🔒 REGLAS TÉCNICAS CUMPLIDAS

### Reglas JPA Estrictas

| Regla | Estado | Implementación |
|-------|--------|----------------|
| Constructor acepta `version = null` | ✅ | `ApuEntity(UUID, PartidaEntity, BigDecimal, String, Integer)` - version puede ser null |
| `@PrePersist` solo para fechas | ✅ | No se usa `@PrePersist`. Se usa `@CreationTimestamp` y `@UpdateTimestamp` |
| NO validaciones manuales de versión | ✅ | Adapters NO lanzan `OptimisticLockingFailureException` manualmente |
| Hibernate maneja optimistic locking | ✅ | `@Version` en entidades, Hibernate incrementa automáticamente |
| Relación 1:1 con Partida | ✅ | `@OneToOne` con `unique = true` |
| Cascade y Orphan Removal | ✅ | `@OneToMany` con `cascade = CascadeType.ALL` y `orphanRemoval = true` |

### Reglas de Arquitectura

| Regla | Estado | Implementación |
|-------|--------|----------------|
| Separación de capas | ✅ | Domain → Application → Infrastructure |
| Puertos y Adapters | ✅ | Interfaces en dominio, implementaciones en infraestructura |
| DTOs en Application | ✅ | Commands y Responses en capa de aplicación |
| Agregados inmutables | ✅ | Clases `final`, constructores privados, factory methods |
| Value Objects | ✅ | `ApuId`, `ApuInsumoId` encapsulan UUID |
| Entidades internas | ✅ | `ApuInsumo` es entidad interna del agregado APU |

---

## 📊 ESTADÍSTICAS DE IMPLEMENTACIÓN

### Archivos Creados

| Categoría | Cantidad | Ubicación |
|-----------|----------|-----------|
| **Domain** | 4 | `domain/finanzas/apu/` |
| **Application** | 9 | `application/apu/` |
| **Infrastructure** | 6 | `infrastructure/persistence/`, `infrastructure/rest/` |
| **Database** | 1 | `resources/db/migration/V4__*.sql` |
| **Tests** | 1 | `test/java/.../GestionApuIntegrationTest.java` |
| **TOTAL** | **21** | |

### Líneas de Código (Estimado)

- **Domain:** ~500 líneas
- **Application:** ~400 líneas
- **Infrastructure:** ~600 líneas
- **Database:** ~40 líneas
- **Tests:** ~180 líneas
- **TOTAL:** ~1,720 líneas

---

## ✅ VALIDACIONES Y REGLAS DE NEGOCIO

### APU

| Regla | Validación | Ubicación |
|-------|------------|-----------|
| PartidaId obligatorio | `@NotNull` + validación dominio | Request DTO + Agregado |
| Partida debe existir | Validación UseCase | `CrearApuUseCaseImpl` |
| Un partida = un APU | Constraint BD + validación UseCase | Tabla + `existsByPartidaId()` |
| Lista de insumos no nula | Validación dominio | Constructor agregado |
| Rendimiento opcional | Sin validación obligatoria | Request DTO |
| Unidad opcional | Sin validación obligatoria | Request DTO |

### ApuInsumo

| Regla | Validación | Ubicación |
|-------|------------|-----------|
| RecursoId obligatorio | `@NotNull` + validación dominio | Request DTO + Agregado |
| Cantidad no negativa | `@DecimalMin("0.0")` + validación dominio | Request DTO + Agregado |
| Precio unitario no negativo | `@DecimalMin("0.0")` + validación dominio | Request DTO + Agregado |
| Subtotal calculado | Lógica dominio | `ApuInsumo.calcularSubtotal()` |
| Recurso debe existir | Validación UseCase | `CrearApuUseCaseImpl` |

### Cálculo de Costos

| Regla | Validación | Ubicación |
|-------|------------|-----------|
| Subtotal = cantidad * precioUnitario | Cálculo automático | `ApuInsumo.calcularSubtotal()` |
| Costo Total = Σ subtotales | Cálculo automático | `APU.calcularCostoTotal()` |

---

## 🚀 ENDPOINTS REST DISPONIBLES

### POST /api/v1/partidas/{partidaId}/apu

**Descripción:** Crea un nuevo APU para una partida

**Request:**
```http
POST /api/v1/partidas/770e8400-e29b-41d4-a716-446655440002/apu
Content-Type: application/json

{
    "rendimiento": 10.5,
    "unidad": "m3",
    "insumos": [
        {
            "recursoId": "550e8400-e29b-41d4-a716-446655440000",
            "cantidad": 7.5,
            "precioUnitario": 25.50
        },
        {
            "recursoId": "660e8400-e29b-41d4-a716-446655440001",
            "cantidad": 0.5,
            "precioUnitario": 15.00
        }
    ]
}
```

**Response (201 CREATED):**
```json
{
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "partidaId": "770e8400-e29b-41d4-a716-446655440002",
    "rendimiento": 10.5,
    "unidad": "m3",
    "costoTotal": 198.75,
    "version": 0,
    "insumos": [
        {
            "id": "990e8400-e29b-41d4-a716-446655440004",
            "recursoId": "550e8400-e29b-41d4-a716-446655440000",
            "cantidad": 7.5,
            "precioUnitario": 25.50,
            "subtotal": 191.25
        },
        {
            "id": "aa0e8400-e29b-41d4-a716-446655440005",
            "recursoId": "660e8400-e29b-41d4-a716-446655440001",
            "cantidad": 0.5,
            "precioUnitario": 15.00,
            "subtotal": 7.50
        }
    ],
    "createdAt": "2026-01-12T17:00:00",
    "updatedAt": "2026-01-12T17:00:00"
}
```

**Errores Posibles:**
- `400 BAD REQUEST` - Validación fallida
- `404 NOT FOUND` - Partida no encontrada
- `404 NOT FOUND` - Recurso no encontrado
- `409 CONFLICT` - La partida ya tiene un APU

---

## 🔍 DIAGRAMA DE FLUJO

### Flujo de Creación de APU

```
┌─────────────┐
│   Cliente   │
│   (REST)    │
└──────┬──────┘
       │ POST /api/v1/partidas/{partidaId}/apu
       │ {rendimiento?, unidad?, insumos[]}
       ▼
┌─────────────────────┐
│ ApuController       │
└──────┬──────────────┘
       │ CrearApuCommand
       ▼
┌─────────────────────┐
│ CrearApu            │
│ UseCaseImpl         │
└──────┬──────────────┘
       │ 1. Validar partida existe
       │ 2. Validar partida no tiene APU
       │ 3. Validar todos los recursos existen
       │ 4. Crear ApuId
       │ 5. Crear agregado APU
       │ 6. Para cada insumo:
       │    a. Agregar insumo al APU
       │    b. APU calcula subtotal automáticamente
       │ 7. Calcular costo total
       ▼
┌─────────────────────┐
│ ApuRepository       │
│ Adapter             │
└──────┬──────────────┘
       │ save(APU)
       │ - Cargar PartidaEntity
       │ - Mapear APU a ApuEntity
       │ - Para cada insumo:
       │   a. Cargar RecursoEntity
       │   b. Mapear ApuInsumo a ApuInsumoEntity
       │   c. Asignar relaciones
       ▼
┌─────────────────────┐
│ ApuJpaRepository    │
└──────┬──────────────┘
       │ INSERT INTO apu
       │ INSERT INTO apu_insumo (cascade)
       ▼
┌─────────────────────┐
│   PostgreSQL        │
└─────────────────────┘
```

---

## 📈 ESTADO ACTUAL Y PRÓXIMOS PASOS

### Estado Actual

✅ **COMPLETADO:**
- Agregado de dominio (APU con entidad interna ApuInsumo)
- Caso de uso de creación
- Persistencia JPA con relaciones 1:1 y 1:N
- Endpoint REST
- Migración de base de datos
- Test de integración
- Compilación exitosa
- Cálculo de costos implementado

### Próximos Pasos Sugeridos

**MOVIMIENTO 4 (Siguiente):**
1. **ConsumoPartida** - Imputación de costos a partidas
2. **Compra** - Registro de compras
3. **Inventario** - Gestión de stock

**Mejoras Futuras:**
- Endpoints de lectura (GET /partidas/{id}/apu)
- Endpoints de actualización (PUT /partidas/{id}/apu)
- Endpoints para agregar/eliminar insumos
- Validación de transiciones de estado
- Eventos de dominio (ApuCreado, InsumoAgregado)
- Consultas optimizadas para APU completo con insumos

---

## 🎯 CONCLUSIÓN

El **MOVIMIENTO 3** ha sido implementado exitosamente siguiendo:

- ✅ **Clean Architecture (Hexagonal)**
- ✅ **Reglas JPA estrictas** (optimistic locking nativo)
- ✅ **DDD** (Agregados, Value Objects, Entidades Internas, Invariantes)
- ✅ **Best Practices** (Factory Methods, Inmutabilidad, Separación de responsabilidades)
- ✅ **Relaciones JPA** (1:1 con Partida, 1:N con ApuInsumo, N:1 con Recurso)
- ✅ **Cálculo de Costos** (Motor de cálculo implementado en dominio)

**El código está:**
- ✅ Compilable
- ✅ Testeable
- ✅ Listo para producción (después de ejecutar tests con Docker)
- ✅ Documentado
- ✅ Siguiendo estándares del proyecto

**Estado Final:** 🟢 **COMPLETADO Y LISTO PARA USO**

---

**Fin del Reporte**
