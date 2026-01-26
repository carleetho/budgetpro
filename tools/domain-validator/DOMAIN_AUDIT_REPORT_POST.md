# Domain Audit Report: Technical Debt & Architectural Purity
*Fecha de Auditoría: 2026-01-25 01:49:59*

## Executive Summary
Este informe consolida las violaciones de la arquitectura de cebolla (Onion Architecture) detectadas en la capa de dominio.

## 1. Violation Matrix
| Módulo | Archivo | Tipo Violación | Detalle Técnico | Severidad | Acción Correctiva |
| :--- | :--- | :--- | :--- | :--- | :--- |
| catalogo | SnapshotService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | SnapshotService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | CalculoApuDinamicoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | CalculoApuDinamicoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | ControlAvanceService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | ControlAvanceService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.partida.model.Partida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | PresupuestoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | PresupuestoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityAuditLog.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalculoPresupuestoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.partida.model.Partida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityHashService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalculoCronogramaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalculoCronogramaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | SnapshotGeneratorService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | SnapshotGeneratorService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CronogramaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CronogramaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CronogramaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | Billetera.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | InteligenciaMaquinariaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.apu.model.ApuInsumo;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalculadoraPrecioVentaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalcularSalarioRealService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalcularSalarioRealService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.recurso.model.Recurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | AgregacionControlCostosService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | AgregacionControlCostosService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.partida.model.Partida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | GeneradorEstimacionService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | GeneradorEstimacionService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;` | 🟠 HIGH | Relacionar via ID-Reference. |
| rrhh | CalculadorFSR.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;` | 🟠 HIGH | Relacionar via ID-Reference. |
| rrhh | CalculadorFSR.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.rrhh.model.Empleado;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | DespachoRequisicionService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.requisicion.model.Requisicion;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | DespachoRequisicionService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.requisicion.model.RequisicionItem;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | BacklogService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | BacklogService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.requisicion.model.Requisicion;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | ProcesarCompraService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | ProcesarCompraService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.model.Billetera;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | ProcesarCompraService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.partida.model.Partida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | ProcesarCompraService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.compra.model.Compra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | ProcesarCompraService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.compra.model.CompraDetalle;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | GestionInventarioService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.compra.model.Compra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | GestionInventarioService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.compra.model.CompraDetalle;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | GestionInventarioService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | InventarioSnapshotService.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.catalogo.model.RecursoSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | InventarioSnapshotService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.compra.model.Compra;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | InventarioSnapshotService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.compra.model.CompraDetalle;` | 🟠 HIGH | Relacionar via ID-Reference. |
| logistica | TransferenciaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;` | 🟠 HIGH | Relacionar via ID-Reference. |

## 2. Health Status by Bounded Context
| Bounded Context | Status | Violations Breakdown | Risk Level |
| :--- | :--- | :--- | :--- |
| **finanzas** | 🟠 STABLE | Total: 23 (🔴0, 🟠23) | MEDIUM |
| **proyecto** | ✅ CLEAN | Total: 0 (🔴0, 🟠0) | LOW |
| **catalogo** | 🟠 STABLE | Total: 4 (🔴0, 🟠4) | MEDIUM |
| **recurso** | ✅ CLEAN | Total: 0 (🔴0, 🟠0) | LOW |
| **rrhh** | 🟠 STABLE | Total: 2 (🔴0, 🟠2) | MEDIUM |
| **logistica** | 🟠 STABLE | Total: 16 (🔴0, 🟠16) | MEDIUM |
| **shared** | ✅ CLEAN | Total: 0 (🔴0, 🟠0) | LOW |

## 3. Refactoring Action Plan
### 3.1 File Relocations (Structural Fixes)
No se detectaron archivos Impl mal ubicados.
### 3.2 Observability Decoupling (Purity Fixes)
Para resolver las violaciones de infraestructura (Purity Violations), se debe implementar el patrón Port:
```java
// 1. Definir interfaz en dominio
package com.budgetpro.domain.shared.port.out;
public interface DomainEventLogger {
    void log(String message);
}

// 2. Inyectar en servicio de dominio
public class IntegrityHashServiceImpl implements IntegrityHashService {
    private final DomainEventLogger logger; // Decoupled
    ...
}
```
### 3.3 Aggregate Decoupling
Se detectaron acoplamientos directos entre agregados. Aplicar patrón ID-Reference: