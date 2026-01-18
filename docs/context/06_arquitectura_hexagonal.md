# Arquitectura Hexagonal (Capas y Reglas)

---

> 🏷️ META-INFO
> 
> - **📅 Fecha:** 09/01/2026
> - **🧊 Estado:** CONGELADO (Auditado)
> - **📂 Clasificación:** Arquitectura de Sistema
> - **📎 Archivo Origen:** ARQUITECTURA CLEAN / HEXAGONAL — BUDGETPRO (v1.0)

---

# 🏰 BUDGETPRO — Arquitectura Clean / Hexagonal (v1.0)

## 1. 🎯 Resumen Ejecutivo

Este documento define la **arquitectura técnica oficial y congelada** del sistema **BUDGETPRO (MVP Real v1.0)**.

Su propósito es:

- Proteger el **Dominio** como núcleo del negocio
- Garantizar **consistencia transaccional** en operaciones financieras
- Permitir **lecturas de alto rendimiento** (CQRS-Lite)
- Evitar **Modelo de Dominio Anémico**
- Asegurar **testabilidad, desacoplamiento y extensibilidad**
- Soportar la **realidad operativa del rubro construcción / ingeniería**

> 🧊 Estado del documento:
> 
> 
> Aprobado por Arquitectura Senior y CTO Interino.
> 
> **Apto para implementación directa.**
> 

---

## 2. 🧭 Principios Rectores (Congelados)

| # | Principio | Descripción |
| --- | --- | --- |
| 1 | Regla de Dependencia | Las dependencias **siempre apuntan hacia adentro** |
| 2 | CQRS-Lite | Escritura con Agregados, Lectura con Proyecciones |
| 3 | Dominio Puro | Sin frameworks, sin I/O, sin serialización |
| 4 | UseCases Orquestan | Coordinan, no contienen reglas profundas |
| 5 | Infra Reemplazable | Cambiar REST/JPA/Storage no afecta Dominio |

⚠️ **Cualquier violación a estos principios se considera deuda técnica crítica.**

---

## 3. 🧱 Visión General de Capas

```mermaid
flowchart TB
    INFRA[Infraestructura<br/>(REST, JPA, SQL, Storage)]
    APP[Aplicación<br/>(UseCases WRITE / Queries READ)]
    DOM[Dominio<br/>(Agregados, Invariantes, Services)]

    INFRA --> APP
    APP --> DOM

```

---

## 4. 🧠 Capa de Dominio (Core)

### 4.1 Responsabilidad

- Modelar la **lógica de negocio pura**
- Proteger **invariantes**
- Definir **Agregados**
- Ejecutar **reglas inquebrantables**

### 4.2 Contenido Permitido

**✔ Incluye**

- Agregados (Roots)
- Entidades
- Value Objects
- Domain Services
- Interfaces de Repositorios (Puertos de Salida)
- Shared Kernel (`CatalogoRecurso`)

**❌ Prohibido**

- JPA / Hibernate
- REST / HTTP
- DTOs
- JSON
- Transacciones técnicas
- Almacenamiento de archivos

---

### 4.3 Agregados Principales (Resumen)

Agregado

---

Proyecto

---

Presupuesto

---

InventarioItem

---

Compra

---

ConsumoPartida

---

PlanillaSemanal

---

BilleteraProyecto

---

PrestamoInterProyecto

---

AvancePartida

---

EstimacionPago

---

AprobacionCliente

---

Evidencia

---

ℹ️ *La definición completa se encuentra en:*

📄 **Modelo de Agregados (DDD)**

---

### 4.4 Domain Services Críticos

### 🔹 ProcesarCompraDirectaService

- Orquesta:
    - Compra
    - Inventario (virtual)
    - ConsumoPartida
    - BilleteraProyecto
- **No abre transacciones**
- Recibe repositorios por constructor

### 🔹 CongelarLineaBaseService

- Garantiza inmutabilidad contractual
- Genera snapshot de lectura (Read Model)

---

## 5. 🧩 Capa de Aplicación

### 5.1 Responsabilidad

- Orquestar comportamiento del sistema
- Coordinar agregados
- Definir Casos de Uso (**WRITE**)
- Definir Queries (**READ**)
- Controlar transacciones
- Proteger la UX

---

### 5.2 WRITE — Casos de Uso

**Características**

- Ejecutan comandos
- Abren y cierran transacciones
- Invocan Domain Services
- ❌ Nunca retornan Agregados

**Ejemplos**

- `ProcesarCompraDirectaUseCase`
- `RegistrarPagoPlanillaUseCase`
- `DistribuirCostoPlanillaUseCase`
- `CongelarLineaBaseUseCase`
- `GenerarEstimacionPagoUseCase`

```java
public interface ProcesarCompraDirectaUseCase {
    void ejecutar(ProcesarCompraDirectaCommand command);
}

```

---

### 5.3 READ — CQRS-Lite (Obligatorio)

> 🛑 La UI nunca consulta Agregados ni Repositorios de Dominio
> 

**Características**

- DTOs planos (Projections)
- SQL / JPQL optimizado
- Sin lógica de negocio
- Sin hidratar Dominio

```java
public interface ObtenerPresupuestoQuery {
    PresupuestoView ejecutar(PresupuestoId id);
}

```

---

## 6. 🔌 Puertos (Interfaces)

### 6.1 Puertos de Entrada (Inbound)

Ubicación: `application.port.in`

- UseCases (WRITE)
- Queries (READ)

---

### 6.2 Puertos de Salida (Outbound)

- Repositorios de Dominio
- Gateways externos (Storage, APIs)

```java
public interface PresupuestoRepository {
    Presupuesto obtenerPorId(PresupuestoId id);
    void guardar(Presupuesto presupuesto);
}

```

---

## 7. 🧰 Infraestructura (Adaptadores)

### 7.1 Responsabilidad

- Implementar detalles técnicos
- Adaptar tecnología a puertos
- ❌ Nunca contener reglas de negocio

---

### 7.2 Adaptadores de Entrada

```java
@RestController
class CompraController {

  private final ProcesarCompraDirectaUseCase useCase;

  @PostMapping("/compras/directa")
  public void procesar(@RequestBody CompraDirectaRequest req) {
      useCase.ejecutar(map(req));
  }
}

```

---

### 7.3 Adaptadores de Salida

- Persistencia WRITE → JPA
- Persistencia READ → SQL / JPQL
- Storage → S3 / MinIO / FileSystem

---

## 8. 🔐 Manejo de Transacciones

🛑 **Regla Congelada**

- Transacciones **solo en Aplicación**
- Nunca en Dominio
- Nunca en Infraestructura

```java
@Transactional
public void ejecutar(Command cmd) {
    domainService.ejecutar(data);
}

```

---

## 9. 🗂️ Estructura Final de Paquetes

```
com.invco.budgetpro
├── domain
├── application
│   ├── usecase
│   ├── query
│   └── port
├── infrastructure
│   ├── persistence
│   ├── web
│   └── config
└── bootstrap

```

---

## 10. 🛡️ Riesgos Mitigados

| Riesgo | Estado |
| --- | --- |
| N+1 Queries | ✔ Mitigado |
| Lecturas lentas UI | ✔ Mitigado |
| Dominio serializado | ✔ Evitado |
| Uploads bloqueantes | ✔ Evitado |
| Lógica duplicada | ✔ Controlado |

---

## 🔒 Congelamiento Final

🧊 A partir de este documento:

- ❌ No usar Agregados para lectura
- ❌ No retornar Entidades en APIs
- ❌ No blobs en Dominio
- ✔ WRITE → UseCases + Dominio
- ✔ READ → Query Services
- ✔ Infraestructura solo implementa puertos

---