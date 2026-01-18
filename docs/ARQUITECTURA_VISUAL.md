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
    
    COMPRA ||--o{ COMPRA_DETALLE : "tiene"
    COMPRA_DETALLE }o--|| RECURSO : "compra"
    COMPRA_DETALLE }o--|| PARTIDA : "imputa"
    COMPRA_DETALLE ||--|| CONSUMO_PARTIDA : "genera"
    
    INVENTARIO_ITEM }o--|| RECURSO : "almacena"
    INVENTARIO_ITEM ||--o{ MOVIMIENTO_INVENTARIO : "registra"
    
    ESTIMACION ||--o{ DETALLE_ESTIMACION : "tiene"
    DETALLE_ESTIMACION }o--|| PARTIDA : "estima"
    
    PROGRAMA_OBRA ||--o{ ACTIVIDAD_PROGRAMADA : "contiene"
    ACTIVIDAD_PROGRAMADA ||--o{ DEPENDENCIA_ACTIVIDAD : "depende"
    
    AVANCE_FISICO ||--o{ VALUACION : "incluye"
    
    RECURSO ||--o{ APU_INSUMO : "usado_en"
    RECURSO ||--o{ COMPRA_DETALLE : "comprado_en"
    RECURSO ||--o{ INVENTARIO_ITEM : "almacenado_en"
    
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
    
    RECURSO {
        UUID id PK
        string nombre
        TipoRecurso tipo
        string unidad_base
        EstadoRecurso estado
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
        UUID recurso_id FK
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
4. **Partida → APU** (1:1): Cada partida tiene un único APU (Análisis de Precios Unitarios).
5. **APU → APU_Insumo** (1:N): Un APU contiene múltiples insumos (Material, Mano de Obra, Equipo).
6. **APU_Insumo → Recurso** (N:1): Los insumos referencian recursos del catálogo.
7. **Compra → Compra_Detalle** (1:N): Una compra tiene múltiples detalles.
8. **Compra_Detalle → Partida** (N:1): Cada detalle se imputa a una partida.
9. **Compra_Detalle → Consumo_Partida** (1:1): Cada detalle genera un consumo presupuestal.
10. **Estimación → Detalle_Estimacion** (1:N): Una estimación contiene múltiples detalles por partida.

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
        PortsOut[Outbound Ports]
        VOs[Value Objects]
    end
    
    REST --> UseCases
    UseCases --> PortsIn
    UseCases --> Aggregates
    UseCases --> Services
    Adapters --> PortsOut
    Adapters --> JPA
    Services --> Aggregates
    Services --> PortsOut
    Aggregates --> VOs
    Config --> Services
    
    style REST fill:#ffcccc
    style JPA fill:#ffcccc
    style Adapters fill:#ffcccc
    style UseCases fill:#ffffcc
    style DTOs fill:#ffffcc
    style Aggregates fill:#ccffcc
    style Services fill:#ccffcc
    style VOs fill:#ccffcc
```

**Leyenda:**
- 🔴 **Rojo (Infrastructure):** Depende de frameworks (Spring, JPA)
- 🟡 **Amarillo (Application):** Orquesta el dominio
- 🟢 **Verde (Domain):** Puro, sin dependencias externas

---

## 9. NOTAS PARA EL FRONTEND

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

## 10. REFERENCIAS

- **Base URL:** `http://localhost:8080/api/v1`
- **Content-Type:** `application/json`
- **Autenticación:** (Por implementar en el futuro)

---

**Fin del Documento**

*Este documento se actualiza automáticamente basándose en el código fuente del backend.*
