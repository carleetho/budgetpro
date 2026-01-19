# 🎨 ARQUITECTURA VISUAL DEL SISTEMA - BUDGETPRO

**Fecha:** 2026-01-13  
**Versión:** 1.0  
**Propósito:** Documentación visual para guiar el desarrollo del Frontend

---

## 📋 TABLA DE CONTENIDOS

1. [Mapa de Endpoints REST](#1-mapa-de-endpoints-rest)
2. [Diagrama ER (Entity Relationship)](#2-diagrama-er-entity-relationship)
3. [Diagrama de Flujo Principal (Happy Path)](#3-diagrama-de-flujo-principal-happy-path)
4. [Diagrama de Secuencia: Proceso de Estimación](#4-diagrama-de-secuencia-proceso-de-estimación)

---

## 1. MAPA DE ENDPOINTS REST

### Tabla Completa de Endpoints

| Método | Endpoint | Controller | Request DTO | Response DTO | Descripción |
|--------|----------|------------|-------------|--------------|-------------|
| **PROYECTO** |
| `POST` | `/api/v1/proyectos` | `ProyectoController` | `CrearProyectoRequest` | `ProyectoResponse` | Crear nuevo proyecto |
| **PRESUPUESTO** |
| `POST` | `/api/v1/presupuestos` | `PresupuestoController` | `CrearPresupuestoRequest` | `PresupuestoResponse` | Crear presupuesto |
| `GET` | `/api/v1/presupuestos/{id}` | `PresupuestoController` | - | `PresupuestoResponse` | Consultar presupuesto |
| `POST` | `/api/v1/presupuestos/{id}/aprobar` | `PresupuestoController` | - | `Void` (204) | Aprobar presupuesto |
| `GET` | `/api/v1/presupuestos/{id}/control-costos` | `PresupuestoController` | - | `ReporteControlCostosResponse` | Reporte Plan vs Real |
| `PUT` | `/api/v1/presupuestos/{id}/sobrecosto` | `SobrecostoController` | `ConfigurarSobrecostoRequest` | `AnalisisSobrecostoResponse` | Configurar sobrecosto |
| **PARTIDA** |
| `POST` | `/api/v1/partidas` | `PartidaController` | `CrearPartidaRequest` | `PartidaResponse` | Crear partida |
| `POST` | `/api/v1/partidas/{id}/apu` | `ApuController` | `CrearApuRequest` | `ApuResponse` | Crear APU para partida |
| `POST` | `/api/v1/partidas/{id}/avances` | `AvanceController` | `RegistrarAvanceRequest` | `AvanceFisicoResponse` | Registrar avance físico |
| **RECURSO** |
| `POST` | `/api/v1/recursos` | `RecursoController` | `CrearRecursoRequest` | `RecursoResponse` | Crear recurso |
| `GET` | `/api/v1/recursos?search={term}` | `RecursoController` | - | `List<RecursoResponse>` | Buscar recursos (autocomplete) |
| **COMPRA** |
| `POST` | `/api/v1/compras` | `CompraController` | `RegistrarCompraRequest` | `RegistrarCompraResponse` | Registrar compra |
| **INVENTARIO** |
| `GET` | `/api/v1/proyectos/{id}/inventario` | `InventarioController` | - | `List<InventarioItemResponse>` | Consultar inventario |
| **CRONOGRAMA** |
| `POST` | `/api/v1/proyectos/{id}/cronograma/actividades` | `CronogramaController` | `ProgramarActividadRequest` | `ActividadProgramadaResponse` | Programar actividad |
| `GET` | `/api/v1/proyectos/{id}/cronograma` | `CronogramaController` | - | `CronogramaResponse` | Consultar cronograma (Gantt) |
| **ESTIMACIÓN** |
| `POST` | `/api/v1/proyectos/{id}/estimaciones` | `EstimacionController` | `GenerarEstimacionRequest` | `EstimacionResponse` | Generar estimación |
| `PUT` | `/api/v1/proyectos/estimaciones/{id}/aprobar` | `EstimacionController` | - | `Void` (204) | Aprobar estimación (cobro) |
| **ALMACÉN** |
| `POST` | `/api/v1/almacen/movimientos` | `AlmacenController` | `RegistrarMovimientoAlmacenRequest` | `MovimientoAlmacenResponse` | Registrar movimiento |
| **ALERTAS** |
| `GET` | `/api/v1/presupuestos/{id}/analisis` | `AnalisisController` | - | `AnalisisPresupuestoResponse` | Analizar presupuesto |
| **REAJUSTE** |
| `POST` | `/api/v1/presupuestos/{id}/reajuste` | `ReajusteController` | `CalcularReajusteRequest` | `EstimacionReajusteResponse` | Calcular reajuste |
| **LABORAL** |
| `PUT` | `/api/v1/configuracion-laboral` | `LaboralController` | `ConfigurarLaboralRequest` | `ConfiguracionLaboralResponse` | Configuración global |
| `PUT` | `/api/v1/proyectos/{id}/configuracion-laboral` | `LaboralController` | `ConfigurarLaboralRequest` | `ConfiguracionLaboralResponse` | Configuración por proyecto |

---

## 2. DIAGRAMA ER (ENTITY RELATIONSHIP)

### Diagrama Principal de Relaciones

```mermaid
erDiagram
    PROYECTO ||--o{ PRESUPUESTO : "tiene"
    PROYECTO ||--o{ COMPRA : "realiza"
    PROYECTO ||--o{ INVENTARIO_ITEM : "contiene"
    PROYECTO ||--|| PROGRAMA_OBRA : "tiene"
    PROYECTO ||--|| BILLETERA : "tiene"
    
    PRESUPUESTO ||--o{ PARTIDA : "contiene"
    PRESUPUESTO ||--|| ANALISIS_SOBRECOSTO : "tiene"
    PRESUPUESTO ||--o{ ESTIMACION_REAJUSTE : "tiene"
    
    PARTIDA ||--|| APU : "tiene"
    PARTIDA ||--o| PARTIDA : "padre (WBS)"
    PARTIDA ||--o{ CONSUMO_PARTIDA : "consume"
    PARTIDA ||--o{ AVANCE_FISICO : "registra"
    PARTIDA ||--o{ ACTIVIDAD_PROGRAMADA : "programa"
    
    APU ||--o{ APU_INSUMO : "contiene"
    APU_INSUMO }o--|| RECURSO : "usa"
    
    PARTIDA ||--|| APU_SNAPSHOT : "tiene_snapshot"
    APU_SNAPSHOT ||--o{ APU_INSUMO_SNAPSHOT : "contiene"
    
    COMPRA ||--o{ COMPRA_DETALLE : "tiene"
    COMPRA_DETALLE }o--|| PARTIDA : "imputa"
    COMPRA_DETALLE ||--|| CONSUMO_PARTIDA : "genera"
    
    RECURSO ||--|| RECURSO_PROXY : "tiene_proxy"
    
    INVENTARIO_ITEM }o--|| RECURSO : "almacena"
    INVENTARIO_ITEM ||--o{ MOVIMIENTO_INVENTARIO : "registra"
    
    ESTIMACION ||--o{ DETALLE_ESTIMACION : "tiene"
    DETALLE_ESTIMACION }o--|| PARTIDA : "estima"
    
    PROGRAMA_OBRA ||--o{ ACTIVIDAD_PROGRAMADA : "contiene"
    ACTIVIDAD_PROGRAMADA ||--o{ DEPENDENCIA_ACTIVIDAD : "depende"
    
    AVANCE_FISICO ||--o{ VALUACION : "incluye"
    
    RECURSO ||--o{ APU_INSUMO : "usado_en"
    RECURSO ||--o{ INVENTARIO_ITEM : "almacenado_en"
    RECURSO ||--|| RECURSO_PROXY : "referenciado_por"
    
    PROYECTO {
        UUID id PK
        string nombre
        string ubicacion
        EstadoProyecto estado
    }
    
    PRESUPUESTO {
        UUID id PK
        UUID proyecto_id FK
        string nombre
        EstadoPresupuesto estado
        boolean es_contractual
    }
    
    PARTIDA {
        UUID id PK
        UUID presupuesto_id FK
        UUID padre_id FK "nullable"
        string item "WBS"
        string descripcion
        decimal metrado
        integer nivel
    }
    
    APU {
        UUID id PK
        UUID partida_id FK "unique"
        decimal rendimiento
        string unidad
    }
    
    APU_INSUMO {
        UUID id PK
        UUID apu_id FK
        UUID recurso_id FK
        decimal cantidad
        decimal precio_unitario
    }
    
    APU_SNAPSHOT {
        UUID id PK
        UUID partida_id FK "unique"
        string external_apu_id
        string catalog_source
        decimal rendimiento_original
        decimal rendimiento_vigente
        boolean rendimiento_modificado
        UUID rendimiento_modificado_por
        timestamp rendimiento_modificado_en
        string unidad_snapshot
        timestamp snapshot_date
    }
    
    APU_INSUMO_SNAPSHOT {
        UUID id PK
        UUID apu_snapshot_id FK
        string recurso_external_id
        string recurso_nombre
        decimal cantidad
        decimal precio_unitario
        decimal subtotal
    }
    
    RECURSO {
        UUID id PK
        string nombre
        TipoRecurso tipo
        string unidad_base
        EstadoRecurso estado
    }
    
    RECURSO_PROXY {
        UUID id PK
        string external_id
        string catalog_source
        string nombre_snapshot
        TipoRecurso tipo_snapshot
        string unidad_snapshot
        decimal precio_referencial_snapshot
        timestamp snapshot_date
        boolean activo
    }
    
    COMPRA {
        UUID id PK
        UUID proyecto_id FK
        date fecha
        string proveedor
        EstadoCompra estado
        decimal total
    }
    
    COMPRA_DETALLE {
        UUID id PK
        UUID compra_id FK
        string recurso_external_id
        string recurso_nombre
        UUID partida_id FK
        decimal cantidad
        decimal precio_unitario
        decimal subtotal
    }
    
    CONSUMO_PARTIDA {
        UUID id PK
        UUID partida_id FK
        UUID compra_detalle_id FK "unique"
        decimal monto
        TipoConsumo tipo
    }
    
    INVENTARIO_ITEM {
        UUID id PK
        UUID proyecto_id FK
        UUID recurso_id FK
        decimal cantidad_fisica
        decimal costo_promedio
        string ubicacion
    }
    
    ESTIMACION {
        UUID id PK
        UUID proyecto_id FK
        integer numero_estimacion
        date fecha_corte
        decimal monto_bruto
        decimal amortizacion_anticipo
        decimal retencion_fondo_garantia
        decimal monto_neto_pagar
        EstadoEstimacion estado
    }
    
    DETALLE_ESTIMACION {
        UUID id PK
        UUID estimacion_id FK
        UUID partida_id FK
        decimal cantidad_avance
        decimal precio_unitario
        decimal importe
    }
    
    PROGRAMA_OBRA {
        UUID id PK
        UUID proyecto_id FK "unique"
        date fecha_inicio
        date fecha_fin_estimada
        integer duracion_total_dias
    }
    
    ACTIVIDAD_PROGRAMADA {
        UUID id PK
        UUID programa_obra_id FK
        UUID partida_id FK "unique"
        date fecha_inicio
        date fecha_fin
        integer duracion_dias
    }
    
    AVANCE_FISICO {
        UUID id PK
        UUID partida_id FK
        date fecha
        decimal metrado_ejecutado
    }
    
    BILLETERA {
        UUID id PK
        UUID proyecto_id FK "unique"
        decimal saldo_actual
    }
```

### Relaciones Clave Explicadas

1. **Proyecto → Presupuesto** (1:N): Un proyecto puede tener múltiples presupuestos (aunque en MVP solo uno activo).
2. **Presupuesto → Partida** (1:N): Un presupuesto contiene múltiples partidas en estructura jerárquica (WBS).
3. **Partida → Partida** (Auto-referencia): Relación padre-hijo para estructura jerárquica.
4. **Partida → APU** (1:1): Cada partida tiene un único APU legacy (Análisis de Precios Unitarios).
5. **Partida → APU_Snapshot** (1:1): Cada partida puede tener un snapshot de APU desde catálogo externo.
6. **APU → APU_Insumo** (1:N): Un APU legacy contiene múltiples insumos (Material, Mano de Obra, Equipo).
7. **APU_Snapshot → APU_Insumo_Snapshot** (1:N): Un snapshot contiene insumos con referencias externas.
8. **APU_Insumo → Recurso** (N:1): Los insumos legacy referencian recursos locales.
9. **APU_Insumo_Snapshot**: Usa `recurso_external_id` (string) en lugar de FK a Recurso.
10. **Recurso → RecursoProxy** (1:1): Cada recurso local puede tener un proxy a catálogo externo.
11. **Compra → Compra_Detalle** (1:N): Una compra tiene múltiples detalles.
12. **Compra_Detalle**: Usa `recurso_external_id` (string) y `recurso_nombre` (snapshot) en lugar de FK a Recurso.
13. **Compra_Detalle → Partida** (N:1): Cada detalle se imputa a una partida.
14. **Compra_Detalle → Consumo_Partida** (1:1): Cada detalle genera un consumo presupuestal.
15. **Estimación → Detalle_Estimacion** (1:N): Una estimación contiene múltiples detalles por partida.

---

## 3. DIAGRAMA DE FLUJO PRINCIPAL (HAPPY PATH)

### Flujo Completo: Crear Presupuesto → Aprobar → Programar → Estimar → Cobrar

```mermaid
flowchart TD
    Start([Inicio: Nuevo Proyecto]) --> CreateProyecto[POST /api/v1/proyectos<br/>Crear Proyecto]
    CreateProyecto --> CreatePresupuesto[POST /api/v1/presupuestos<br/>Crear Presupuesto]
    
    CreatePresupuesto --> CreatePartidas[POST /api/v1/partidas<br/>Crear Partidas WBS]
    CreatePartidas --> CreateAPU[POST /api/v1/partidas/{id}/apu<br/>Crear APU con Insumos]
    
    CreateAPU --> ConfigSobrecosto[PUT /api/v1/presupuestos/{id}/sobrecosto<br/>Configurar Sobrecosto]
    ConfigSobrecosto --> AprobarPresupuesto[POST /api/v1/presupuestos/{id}/aprobar<br/>Aprobar Presupuesto]
    
    AprobarPresupuesto --> ProgramarCronograma[POST /api/v1/proyectos/{id}/cronograma/actividades<br/>Programar Actividades]
    ProgramarCronograma --> ConsultarCronograma[GET /api/v1/proyectos/{id}/cronograma<br/>Consultar Cronograma]
    
    ConsultarCronograma --> RegistrarCompras[POST /api/v1/compras<br/>Registrar Compras]
    RegistrarCompras --> RegistrarAvances[POST /api/v1/partidas/{id}/avances<br/>Registrar Avances Físicos]
    
    RegistrarAvances --> GenerarEstimacion[POST /api/v1/proyectos/{id}/estimaciones<br/>Generar Estimación]
    GenerarEstimacion --> AprobarEstimacion[PUT /api/v1/proyectos/estimaciones/{id}/aprobar<br/>Aprobar Estimación]
    
    AprobarEstimacion --> CobroRealizado([Cobro Realizado<br/>Ingreso en Billetera])
    
    style Start fill:#e1f5ff
    style CobroRealizado fill:#c8e6c9
    style AprobarPresupuesto fill:#fff9c4
    style AprobarEstimacion fill:#fff9c4
```

### Descripción del Flujo

1. **Crear Proyecto** → Se crea un nuevo proyecto con nombre y ubicación.
2. **Crear Presupuesto** → Se crea un presupuesto asociado al proyecto.
3. **Crear Partidas** → Se crean partidas en estructura jerárquica (WBS).
4. **Crear APU** → Para cada partida, se crea un APU con sus insumos (Material, Mano de Obra, Equipo).
5. **Configurar Sobrecosto** → Se configuran porcentajes de indirectos, financiamiento, utilidad, etc.
6. **Aprobar Presupuesto** → El presupuesto se aprueba y congela (esContractual = true).
7. **Programar Cronograma** → Se programan actividades por partida con fechas y dependencias.
8. **Registrar Compras** → Durante la ejecución, se registran compras que consumen el presupuesto.
9. **Registrar Avances** → Se registran avances físicos (metrados ejecutados) por partida.
10. **Generar Estimación** → Se genera una estimación de avance con cálculos automáticos (amortización, retención).
11. **Aprobar Estimación** → Se aprueba la estimación y se registra el ingreso en la billetera (cobro).

---

## 4. DIAGRAMA DE SECUENCIA: PROCESO DE ESTIMACIÓN

### Flujo Detallado: Generar y Aprobar Estimación

```mermaid
sequenceDiagram
    participant Frontend
    participant EstimacionController
    participant GenerarEstimacionUseCase
    participant GeneradorEstimacionService
    participant EstimacionRepository
    participant PartidaRepository
    participant PresupuestoRepository
    participant AprobarEstimacionUseCase
    participant BilleteraRepository
    participant Database

    Frontend->>EstimacionController: POST /api/v1/proyectos/{id}/estimaciones
    EstimacionController->>GenerarEstimacionUseCase: generar(command)
    
    GenerarEstimacionUseCase->>PresupuestoRepository: findByProyectoId()
    PresupuestoRepository->>Database: SELECT presupuesto
    Database-->>PresupuestoRepository: Presupuesto
    PresupuestoRepository-->>GenerarEstimacionUseCase: Presupuesto
    
    GenerarEstimacionUseCase->>EstimacionRepository: obtenerSiguienteNumeroEstimacion()
    EstimacionRepository->>Database: SELECT MAX(numero_estimacion)
    Database-->>EstimacionRepository: numero
    EstimacionRepository-->>GenerarEstimacionUseCase: numeroEstimacion
    
    loop Para cada detalle
        GenerarEstimacionUseCase->>PartidaRepository: findById(partidaId)
        PartidaRepository->>Database: SELECT partida
        Database-->>PartidaRepository: Partida
        PartidaRepository-->>GenerarEstimacionUseCase: Partida
        
        GenerarEstimacionUseCase->>GeneradorEstimacionService: calcularAcumuladoAnterior()
        GeneradorEstimacionService->>EstimacionRepository: findAprobadasByProyectoId()
        EstimacionRepository->>Database: SELECT estimaciones aprobadas
        Database-->>EstimacionRepository: List<Estimacion>
        EstimacionRepository-->>GeneradorEstimacionService: List<Estimacion>
        GeneradorEstimacionService-->>GenerarEstimacionUseCase: acumuladoAnterior
        
        GenerarEstimacionUseCase->>GeneradorEstimacionService: validarVolumenEstimado()
        GeneradorEstimacionService-->>GenerarEstimacionUseCase: validado
        
        GenerarEstimacionUseCase->>GeneradorEstimacionService: calcularAmortizacionAnticipo()
        GeneradorEstimacionService-->>GenerarEstimacionUseCase: amortizacion
        
        GenerarEstimacionUseCase->>GeneradorEstimacionService: calcularRetencionFondoGarantia()
        GeneradorEstimacionService-->>GenerarEstimacionUseCase: retencion
    end
    
    GenerarEstimacionUseCase->>EstimacionRepository: save(estimacion)
    EstimacionRepository->>Database: INSERT estimacion + detalles
    Database-->>EstimacionRepository: OK
    EstimacionRepository-->>GenerarEstimacionUseCase: OK
    
    GenerarEstimacionUseCase-->>EstimacionController: EstimacionResponse
    EstimacionController-->>Frontend: 201 CREATED + EstimacionResponse
    
    Note over Frontend: Usuario revisa y aprueba
    
    Frontend->>EstimacionController: PUT /api/v1/proyectos/estimaciones/{id}/aprobar
    EstimacionController->>AprobarEstimacionUseCase: aprobar(estimacionId)
    
    AprobarEstimacionUseCase->>EstimacionRepository: findById(estimacionId)
    EstimacionRepository->>Database: SELECT estimacion
    Database-->>EstimacionRepository: Estimacion
    EstimacionRepository-->>AprobarEstimacionUseCase: Estimacion
    
    AprobarEstimacionUseCase->>Estimacion: aprobar()
    Estimacion-->>AprobarEstimacionUseCase: estado = APROBADA
    
    AprobarEstimacionUseCase->>EstimacionRepository: save(estimacion)
    EstimacionRepository->>Database: UPDATE estimacion
    Database-->>EstimacionRepository: OK
    
    AprobarEstimacionUseCase->>BilleteraRepository: findByProyectoId()
    BilleteraRepository->>Database: SELECT billetera
    Database-->>BilleteraRepository: Billetera
    BilleteraRepository-->>AprobarEstimacionUseCase: Billetera
    
    AprobarEstimacionUseCase->>Billetera: ingresar(montoNetoPagar, referencia)
    Billetera-->>AprobarEstimacionUseCase: MovimientoCaja creado
    
    AprobarEstimacionUseCase->>BilleteraRepository: save(billetera)
    BilleteraRepository->>Database: UPDATE billetera + INSERT movimiento_caja
    Database-->>BilleteraRepository: OK
    
    AprobarEstimacionUseCase-->>EstimacionController: OK
    EstimacionController-->>Frontend: 204 NO CONTENT
```

---

## 5. DIAGRAMA DE CLASES: ESTRUCTURA DE DOMINIO

### Agregados Principales y sus Relaciones

```mermaid
classDiagram
    class Proyecto {
        +ProyectoId id
        +String nombre
        +String ubicacion
        +EstadoProyecto estado
    }
    
    class Presupuesto {
        +PresupuestoId id
        +UUID proyectoId
        +String nombre
        +EstadoPresupuesto estado
        +Boolean esContractual
        +aprobar()
    }
    
    class Partida {
        +PartidaId id
        +UUID presupuestoId
        +UUID padreId
        +String item
        +BigDecimal metrado
        +Integer nivel
    }
    
    class APU {
        +ApuId id
        +UUID partidaId
        +BigDecimal rendimiento
        +calcularCostoTotal()
    }
    
    class ApuInsumo {
        +UUID recursoId
        +BigDecimal cantidad
        +BigDecimal precioUnitario
        +calcularSubtotal()
    }
    
    class Recurso {
        +RecursoId id
        +String nombre
        +TipoRecurso tipo
        +String unidadBase
    }
    
    class Compra {
        +CompraId id
        +UUID proyectoId
        +LocalDate fecha
        +EstadoCompra estado
        +aprobar()
    }
    
    class CompraDetalle {
        +UUID recursoId
        +UUID partidaId
        +BigDecimal cantidad
        +BigDecimal precioUnitario
    }
    
    class Estimacion {
        +EstimacionId id
        +UUID proyectoId
        +Integer numeroEstimacion
        +BigDecimal montoBruto
        +BigDecimal montoNetoPagar
        +EstadoEstimacion estado
        +aprobar()
    }
    
    class Billetera {
        +BilleteraId id
        +UUID proyectoId
        +BigDecimal saldoActual
        +ingresar(monto, referencia)
        +egresar(monto, referencia)
    }
    
    Proyecto "1" --> "*" Presupuesto
    Presupuesto "1" --> "*" Partida
    Partida "1" --> "0..1" Partida : padre
    Partida "1" --> "1" APU
    APU "1" --> "*" ApuInsumo
    ApuInsumo "*" --> "1" Recurso
    Compra "1" --> "*" CompraDetalle
    CompraDetalle "*" --> "1" Recurso
    CompraDetalle "*" --> "1" Partida
    Proyecto "1" --> "*" Estimacion
    Estimacion "1" --> "*" DetalleEstimacion
    DetalleEstimacion "*" --> "1" Partida
    Proyecto "1" --> "1" Billetera
```

---

## 6. DIAGRAMA DE ESTADOS: CICLO DE VIDA DE PRESUPUESTO

### Estados y Transiciones del Presupuesto

```mermaid
stateDiagram-v2
    [*] --> EN_EDICION: Crear Presupuesto
    
    EN_EDICION --> EN_EDICION: Agregar Partidas
    EN_EDICION --> EN_EDICION: Crear APUs
    EN_EDICION --> EN_EDICION: Configurar Sobrecosto
    
    EN_EDICION --> APROBADO: Aprobar Presupuesto
    APROBADO --> APROBADO: Consultar Control Costos
    APROBADO --> APROBADO: Registrar Compras
    APROBADO --> APROBADO: Registrar Avances
    APROBADO --> APROBADO: Generar Estimaciones
    
    note right of APROBADO
        Una vez aprobado, el presupuesto
        se congela (esContractual = true)
        y no puede modificarse
    end note
```

---

## 7. DIAGRAMA DE ESTADOS: CICLO DE VIDA DE ESTIMACIÓN

### Estados y Transiciones de la Estimación

```mermaid
stateDiagram-v2
    [*] --> BORRADOR: Generar Estimación
    
    BORRADOR --> BORRADOR: Recalcular Montos
    BORRADOR --> BORRADOR: Modificar Detalles
    
    BORRADOR --> APROBADA: Aprobar Estimación
    APROBADA --> APROBADA: Consultar Detalles
    
    APROBADA --> PAGADA: Marcar como Pagada
    
    note right of APROBADA
        Al aprobar, se registra
        automáticamente el ingreso
        en la Billetera
    end note
    
    note right of PAGADA
        Estado final.
        La estimación ya fue cobrada.
    end note
```

---

## 8. DIAGRAMA DE COMPONENTES: ARQUITECTURA HEXAGONAL

### Separación de Capas y Dependencias

```mermaid
graph TB
    subgraph "Infrastructure Layer"
        REST[REST Controllers]
        JPA[JPA Entities]
        Adapters[Repository Adapters]
        CatalogAdapters[Catalog Adapters]
        Cache[Catalog Cache]
        Config[Spring Config]
    end
    
    subgraph "Application Layer"
        UseCases[Use Cases]
        DTOs[DTOs Commands/Responses]
        PortsIn[Inbound Ports]
    end
    
    subgraph "Domain Layer"
        Aggregates[Aggregates]
        Services[Domain Services]
        SnapshotService[Snapshot Service]
        PortsOut[Outbound Ports]
        CatalogPort[Catalog Port]
        VOs[Value Objects]
    end
    
    REST --> UseCases
    UseCases --> PortsIn
    UseCases --> Aggregates
    UseCases --> Services
    UseCases --> SnapshotService
    Adapters --> PortsOut
    Adapters --> JPA
    CatalogAdapters --> CatalogPort
    CatalogAdapters --> Cache
    Services --> Aggregates
    Services --> PortsOut
    SnapshotService --> CatalogPort
    Aggregates --> VOs
    Config --> Services
    Config --> CatalogAdapters
    
    style REST fill:#ffcccc
    style JPA fill:#ffcccc
    style Adapters fill:#ffcccc
    style CatalogAdapters fill:#ffcccc
    style Cache fill:#ffcccc
    style UseCases fill:#ffffcc
    style DTOs fill:#ffffcc
    style Aggregates fill:#ccffcc
    style Services fill:#ccffcc
    style SnapshotService fill:#ccffcc
    style VOs fill:#ccffcc
```

**Leyenda:**
- 🔴 **Rojo (Infrastructure):** Depende de frameworks (Spring, JPA)
- 🟡 **Amarillo (Application):** Orquesta el dominio
- 🟢 **Verde (Domain):** Puro, sin dependencias externas

### Integración con Catálogos Externos

```mermaid
graph LR
    subgraph "BudgetPro"
        SnapshotService[SnapshotService]
        CatalogPort[CatalogPort Interface]
        CapecoAdapter[CapecoApiAdapter]
        MockAdapter[MockCatalogAdapter]
        Cache[CatalogCache]
    end
    
    subgraph "External Catalogs"
        CapecoAPI[CAPECO API]
        MockData[Mock Data]
    end
    
    SnapshotService --> CatalogPort
    CatalogPort --> CapecoAdapter
    CatalogPort --> MockAdapter
    CapecoAdapter --> Cache
    CapecoAdapter --> CapecoAPI
    MockAdapter --> MockData
    
    style SnapshotService fill:#ccffcc
    style CatalogPort fill:#ccffcc
    style CapecoAdapter fill:#ffcccc
    style MockAdapter fill:#ffcccc
    style Cache fill:#ffcccc
```

---

## 9. DIAGRAMA DE SECUENCIA: CREACIÓN DE APU SNAPSHOT DESDE CATÁLOGO

### Flujo Completo: Crear Snapshot desde Catálogo Externo

```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant SnapshotService
    participant CatalogPort
    participant CapecoAdapter
    participant CatalogCache
    participant CatalogAPI
    participant ApuSnapshotRepository
    participant Database

    User->>Controller: POST /api/v1/partidas/{id}/apu-snapshot
    Controller->>SnapshotService: createAPUSnapshot(externalApuId, catalogSource)
    
    Note over SnapshotService: Genera correlationId para rastreo
    
    SnapshotService->>CatalogPort: fetchAPU(externalApuId, catalogSource)
    CatalogPort->>CapecoAdapter: fetchAPU(externalApuId, catalogSource)
    
    CapecoAdapter->>CatalogCache: getApuL2(cacheKey)
    CatalogCache-->>CapecoAdapter: Optional<APUSnapshot>
    
    alt Cache Miss
        CapecoAdapter->>CatalogAPI: GET /apus/{externalApuId}
        CatalogAPI-->>CapecoAdapter: CapecoApuResponse
        
        Note over CapecoAdapter: Registra métricas: API call, latency
        
        CapecoAdapter->>CatalogCache: putApuL2(cacheKey, snapshot)
        CapecoAdapter-->>CatalogPort: APUSnapshot
    else Cache Hit
        CapecoAdapter-->>CatalogPort: APUSnapshot (from cache)
        Note over CapecoAdapter: Registra métrica: cache hit
    end
    
    CatalogPort-->>SnapshotService: APUSnapshot (from catalog)
    
    loop Para cada insumo del APU
        SnapshotService->>CatalogPort: fetchRecurso(recursoExternalId, catalogSource)
        CatalogPort->>CapecoAdapter: fetchRecurso(recursoExternalId, catalogSource)
        
        CapecoAdapter->>CatalogCache: getRecursoL2(cacheKey)
        alt Cache Miss
            CapecoAdapter->>CatalogAPI: GET /recursos/{externalId}
            CatalogAPI-->>CapecoAdapter: CapecoRecursoResponse
            CapecoAdapter->>CatalogCache: putRecursoL2(cacheKey, snapshot)
        end
        
        CapecoAdapter-->>CatalogPort: RecursoSnapshot
        CatalogPort-->>SnapshotService: RecursoSnapshot
        
        Note over SnapshotService: Crea APUInsumoSnapshot con<br/>recursoExternalId y recursoNombre
    end
    
    Note over SnapshotService: Registra métricas:<br/>snapshot creation, duration
    
    SnapshotService->>ApuSnapshotRepository: save(APUSnapshot)
    ApuSnapshotRepository->>Database: INSERT apu_snapshot + insumos
    Database-->>ApuSnapshotRepository: OK
    ApuSnapshotRepository-->>SnapshotService: APUSnapshot saved
    
    SnapshotService-->>Controller: APUSnapshot
    Controller-->>User: 201 CREATED + APUSnapshotResponse
```

---

## 10. DIAGRAMA DE SECUENCIA: MODIFICACIÓN DE RENDIMIENTO

### Flujo: Usuario Modifica Rendimiento de APU Snapshot

```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant SnapshotService
    participant ApuSnapshotRepository
    participant CatalogEventLogger
    participant CatalogMetrics
    participant Database

    User->>Controller: PUT /api/v1/apu-snapshots/{id}/rendimiento
    Controller->>SnapshotService: actualizarRendimiento(snapshot, nuevoRendimiento, usuarioId)
    
    Note over SnapshotService: Genera correlationId
    
    SnapshotService->>ApuSnapshotRepository: findById(snapshotId)
    ApuSnapshotRepository->>Database: SELECT apu_snapshot
    Database-->>ApuSnapshotRepository: APUSnapshot
    ApuSnapshotRepository-->>SnapshotService: APUSnapshot
    
    Note over SnapshotService: rendimientoAnterior = snapshot.rendimientoVigente
    
    SnapshotService->>SnapshotService: snapshot.actualizarRendimiento(nuevoRendimiento, usuarioId)
    
    Note over SnapshotService: Actualiza:<br/>- rendimientoVigente = nuevoRendimiento<br/>- rendimientoModificado = true<br/>- rendimientoModificadoPor = usuarioId<br/>- rendimientoModificadoEn = now()
    
    SnapshotService->>CatalogMetrics: recordRendimientoOverride(catalogSource)
    SnapshotService->>CatalogEventLogger: logRendimientoModification(correlationId, ...)
    
    Note over CatalogEventLogger: Log estructurado con:<br/>- rendimientoOriginal<br/>- rendimientoAnterior<br/>- rendimientoNuevo<br/>- desviacionOriginal<br/>- cambioAbsoluto
    
    SnapshotService->>ApuSnapshotRepository: save(snapshot)
    ApuSnapshotRepository->>Database: UPDATE apu_snapshot
    Database-->>ApuSnapshotRepository: OK
    ApuSnapshotRepository-->>SnapshotService: APUSnapshot updated
    
    SnapshotService-->>Controller: OK
    Controller-->>User: 200 OK
```

---

## 11. DIAGRAMA DE ARQUITECTURA: INTEGRACIÓN CON CATÁLOGOS

### Flujo Completo de Integración

```mermaid
graph TB
    subgraph "BudgetPro Application"
        subgraph "Domain Layer"
            SnapshotService[SnapshotService]
            CatalogPort[CatalogPort Interface]
        end
        
        subgraph "Infrastructure Layer"
            CapecoAdapter[CapecoApiAdapter]
            MockAdapter[MockCatalogAdapter]
            Cache[CatalogCache L1/L2]
            Metrics[CatalogMetrics]
            Logger[CatalogEventLogger]
        end
        
        subgraph "Persistence"
            SnapshotRepo[ApuSnapshotRepository]
            ProxyRepo[RecursoProxyRepository]
            Database[(PostgreSQL)]
        end
    end
    
    subgraph "External Systems"
        CapecoAPI[CAPECO API]
    end
    
    SnapshotService --> CatalogPort
    CatalogPort --> CapecoAdapter
    CatalogPort --> MockAdapter
    CapecoAdapter --> Cache
    CapecoAdapter --> CapecoAPI
    CapecoAdapter --> Metrics
    CapecoAdapter --> Logger
    SnapshotService --> Metrics
    SnapshotService --> Logger
    SnapshotService --> SnapshotRepo
    SnapshotService --> ProxyRepo
    SnapshotRepo --> Database
    ProxyRepo --> Database
    
    style SnapshotService fill:#ccffcc
    style CatalogPort fill:#ccffcc
    style CapecoAdapter fill:#ffcccc
    style MockAdapter fill:#ffcccc
    style Cache fill:#ffcccc
    style Metrics fill:#ffcccc
    style Logger fill:#ffcccc
```

---

## 12. NOTAS PARA EL FRONTEND

### Endpoints Críticos para el Desarrollo

#### 1. Flujo de Creación de Presupuesto
```
POST /api/v1/proyectos
POST /api/v1/presupuestos
POST /api/v1/partidas (múltiples)
POST /api/v1/partidas/{id}/apu
PUT /api/v1/presupuestos/{id}/sobrecosto
POST /api/v1/presupuestos/{id}/aprobar
```

#### 2. Flujo de Consulta (Read-Only)
```
GET /api/v1/presupuestos/{id}
GET /api/v1/presupuestos/{id}/control-costos
GET /api/v1/proyectos/{id}/cronograma
GET /api/v1/proyectos/{id}/inventario
GET /api/v1/recursos?search={term}
```

#### 3. Flujo de Ejecución
```
POST /api/v1/compras
POST /api/v1/partidas/{id}/avances
POST /api/v1/proyectos/{id}/estimaciones
PUT /api/v1/proyectos/estimaciones/{id}/aprobar
```

### Validaciones Importantes

1. **Presupuesto:** Solo se puede aprobar si todas las partidas hoja tienen APU.
2. **Estimación:** No se puede estimar más del 100% del volumen contratado.
3. **Compra:** Requiere saldo suficiente en la billetera.
4. **Partida:** El `padreId` debe pertenecer al mismo presupuesto.

### Códigos de Respuesta HTTP

- `201 CREATED`: Recurso creado exitosamente
- `200 OK`: Operación exitosa (GET, PUT)
- `204 NO CONTENT`: Operación exitosa sin contenido (DELETE, aprobar)
- `400 BAD REQUEST`: Validación fallida
- `404 NOT FOUND`: Recurso no encontrado
- `409 CONFLICT`: Conflicto de versión (Optimistic Locking)

---

## 10. ESTRATEGIA DE MIGRACIÓN: LEGACY → SNAPSHOT

### Fases de Migración

```mermaid
flowchart TD
    Start[Estado Inicial] --> Phase1[Fase 1: Migración de Datos]
    Phase1 --> Phase2[Fase 2: Dual Write]
    Phase2 --> Phase3[Fase 3: Solo Snapshots]
    
    Phase1 --> LegacyData[(APU Legacy)]
    Phase1 --> SnapshotData[(APU_SNAPSHOT)]
    
    Phase2 --> NewPresupuestos[Nuevos Presupuestos<br/>usan Snapshots]
    Phase2 --> LegacyPresupuestos[Presupuestos Legacy<br/>usan APU Legacy]
    
    Phase3 --> AllSnapshots[Todos usan Snapshots]
    Phase3 --> Deprecate[Deprecar APU Legacy]
    
    style Phase1 fill:#fff9c4
    style Phase2 fill:#c8e6c9
    style Phase3 fill:#ffcccc
```

### Fase 1: Migración de Datos (Completada)

**Objetivo:** Migrar datos legacy a formato snapshot

**Migraciones:**
- `V5__migrate_recurso_to_proxy.sql`: Migra recursos a RecursoProxy
- `V6__migrate_apu_to_snapshot.sql`: Migra APUs a APUSnapshot

**Resultado:**
- Todos los datos legacy tienen snapshots equivalentes
- `catalog_source = 'BUDGETPRO_LEGACY'`
- `external_apu_id = 'LEGACY_APU_{uuid}'`

**Ver:** `docs/migration/V6_MIGRATION_GUIDE.md`

### Fase 2: Dual Write (Actual)

**Objetivo:** Nuevos presupuestos usan snapshots, legacy mantiene APU

**Características:**
- Nuevos presupuestos crean solo snapshots
- Presupuestos legacy mantienen sus APUs originales
- Ambos modelos coexisten

### Fase 3: Solo Snapshots (Futuro)

**Objetivo:** Deprecar completamente modelo legacy

**Acciones:**
- Todos los presupuestos usan snapshots
- Tablas `apu` y `apu_insumo` se marcan como deprecated
- Migración final de cualquier dato restante

---

## 11. REFERENCIAS

- **Base URL:** `http://localhost:8080/api/v1`
- **Content-Type:** `application/json`
- **Autenticación:** (Por implementar en el futuro)

### Documentación Relacionada

- **Integración de Catálogos:** `docs/CATALOG_INTEGRATION.md`
- **Semántica de Snapshots:** `docs/SNAPSHOT_SEMANTICS.md`
- **Modificación de Rendimiento:** `docs/RENDIMIENTO_OVERRIDE.md`
- **Modelo de Dominio:** `docs/DOMAIN_MODEL.md`
- **Observabilidad:** `docs/OBSERVABILITY_CATALOG.md`
- **Guía de Migración:** `docs/migration/V6_MIGRATION_GUIDE.md`

---

**Fin del Documento**

*Este documento se actualiza automáticamente basándose en el código fuente del backend.*
