# Domain Audit Report: Technical Debt & Architectural Purity
*Fecha de Auditoría: 2026-01-25 01:15:57*

## Executive Summary
Este informe consolida las violaciones de la arquitectura de cebolla (Onion Architecture) detectadas en la capa de dominio.

## 1. Violation Matrix
| Módulo | Archivo | Tipo Violación | Detalle Técnico | Severidad | Acción Correctiva |
| :--- | :--- | :--- | :--- | :--- | :--- |
| catalogo | SnapshotService.java | INFRASTRUCTURE_IMPORT | `import com.budgetpro.infrastructure.catalogo.observability.CatalogEventLogger;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| catalogo | SnapshotService.java | INFRASTRUCTURE_IMPORT | `import com.budgetpro.infrastructure.catalogo.observability.CatalogMetrics;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| catalogo | SnapshotService.java | SPRING_IMPORT | `import org.springframework.stereotype.Service;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| catalogo | SnapshotService.java | SPRING_IMPORT | `import org.springframework.transaction.annotation.Transactional;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| catalogo | CalculoApuDinamicoService.java | SPRING_IMPORT | `import org.springframework.stereotype.Service;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | IntegrityAuditLog.java | SPRING_IMPORT | `import org.springframework.stereotype.Service;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | IntegrityHashServiceImpl.java | INFRASTRUCTURE_IMPORT | `import com.budgetpro.infrastructure.observability.IntegrityEventLogger;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | IntegrityHashServiceImpl.java | INFRASTRUCTURE_IMPORT | `import com.budgetpro.infrastructure.observability.IntegrityMetrics;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | IntegrityHashServiceImpl.java | SPRING_IMPORT | `import org.springframework.stereotype.Service;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | SnapshotGeneratorService.java | HEAVY_LIBRARY_IMPORT | `import com.fasterxml.jackson.core.JsonProcessingException;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | SnapshotGeneratorService.java | HEAVY_LIBRARY_IMPORT | `import com.fasterxml.jackson.databind.ObjectMapper;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| logistica | ProcesarCompraService.java | INFRASTRUCTURE_IMPORT | `import com.budgetpro.infrastructure.observability.IntegrityEventLogger;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| logistica | ProcesarCompraService.java | INFRASTRUCTURE_IMPORT | `import com.budgetpro.infrastructure.observability.IntegrityMetrics;` | 🔴 CRITICAL | Extraer a Infrastructure/Port o eliminar framework. |
| finanzas | IntegrityHashServiceImpl.java | Ubicación Incorrecta | `Misplaced Impl in domain.` | 🟠 HIGH | Relocate to infrastructure. |
| catalogo | RecursoSnapshot.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | RecursoSearchCriteria.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | RecursoProxy.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | APUInsumoSnapshot.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | SnapshotService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | SnapshotService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | CalculoApuDinamicoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | CalculoApuDinamicoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| catalogo | CalculoApuDinamicoService.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | ControlAvanceService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | ControlAvanceService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.partida.model.Partida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | PresupuestoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | PresupuestoService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityAuditLog.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityHashServiceImpl.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityHashServiceImpl.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.catalogo.model.APUSnapshot;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityHashServiceImpl.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.partida.model.Partida;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | IntegrityHashServiceImpl.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
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
| finanzas | InteligenciaMaquinariaService.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalculadoraPrecioVentaService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalcularSalarioRealService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalcularSalarioRealService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.recurso.model.Recurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | CalcularSalarioRealService.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
| finanzas | AnalizadorParametricoService.java | CROSS_CONTEXT_IMPORT | `import com.budgetpro.domain.recurso.model.TipoRecurso;` | 🟠 HIGH | Relacionar via ID-Reference. |
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
| logistica | ProcesarCompraService.java | AGGREGATE_COUPLING | `import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;` | 🟠 HIGH | Relacionar via ID-Reference. |
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
| **finanzas** | 🔴 CRITICAL | Total: 37 (🔴6, 🟠31) | HIGH |
| **proyecto** | ✅ CLEAN | Total: 0 (🔴0, 🟠0) | LOW |
| **catalogo** | 🔴 CRITICAL | Total: 14 (🔴5, 🟠9) | HIGH |
| **recurso** | ✅ CLEAN | Total: 0 (🔴0, 🟠0) | LOW |
| **rrhh** | 🟠 STABLE | Total: 2 (🔴0, 🟠2) | MEDIUM |
| **logistica** | 🔴 CRITICAL | Total: 19 (🔴2, 🟠17) | HIGH |
| **shared** | ✅ CLEAN | Total: 0 (🔴0, 🟠0) | LOW |

## 3. Refactoring Action Plan
### 3.1 File Relocations (Structural Fixes)
Las siguientes clases concretas deben moverse a la capa de infraestructura:
```bash
# Violación: IntegrityHashServiceImpl.java en dominio
mkdir -p backend/src/main/java/com/budgetpro/infrastructure/service/finanzas && mv backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashServiceImpl.java backend/src/main/java/com/budgetpro/infrastructure/service/finanzas/IntegrityHashServiceImpl.java && \
sed -i 's/package com.budgetpro.domain.finanzas.presupuesto.service;/package com.budgetpro.infrastructure.service.finanzas;/' backend/src/main/java/com/budgetpro/infrastructure/service/finanzas/IntegrityHashServiceImpl.java

```
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