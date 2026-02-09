# CRONOGRAMA Module - Canonical Specification

> **Status**: Functional (60%)
> **Owner**: Planificación Team
> **Last Updated**: 2026-01-31

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State           | Deliverables                                 |
| ----------- | --------- | ---------------------- | -------------------------------------------- |
| **Current** | Now       | 60% (Gantt/Scheduling) | Activities, Dependencies, Gantt View         |
| **Next**    | +1 Month  | 75%                    | Resource Histogram, Calendar Exceptions      |
| **Target**  | +3 Months | 85%                    | Critical Path Analysis v2, MS Project Import |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                                         | Status                |
| ---- | -------------------------------------------------------------------------------------------- | --------------------- |
| C-01 | **Program Frozen**: When Budget is approved, the Program must be frozen (Baseline creation). | ✅ Implemented        |
| C-02 | **Dependency Integrity**: Start-to-Finish dependencies cannot create circular references.    | 🟡 Partial            |
| C-03 | **One Activity Per Leaf**: Currently simplified to one activity per partida leaf.            | 🟡 Partial (Need 1:N) |
| C-04 | **Working Days**: Durations must calculate based on project calendar (skip weekends).        | 🔴 Missing            |
| C-05 | **Temporal Consistency**: Activity end date must be ≥ start date (zero duration allowed).    | ✅ Implemented        |


### 2.2 Extended Rule Inventory (Phase 1 Alignment)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-018 | **La fecha de fin de una actividad no puede ser menor que la fecha de inicio.** | ✅ Implemented |
| REGLA-019 | **Una actividad no puede ser predecesora de sí misma.** | ✅ Implemented |
| REGLA-020 | **En una dependencia Fin-Inicio, la fecha de inicio de B debe ser >= la fecha de fin de A.** | ✅ Implemented |
| REGLA-021 | **La fecha de fin estimada del programa no puede ser menor a la fecha de inicio.** | ✅ Implemented |
| REGLA-022 | **No se puede actualizar la fecha de fin desde actividades si no hay fecha de inicio.** | ✅ Implemented |
| REGLA-067 | **En cronograma: fecha_fin_estimada >= fecha_inicio; duracion_total_dias > 0 si existe; actividad con fecha_fin >= fecha_inicio y duracion_dias > 0 si existe; actividad_id != actividad_predecesora_id.** | ✅ Implemented |
| REGLA-089 | **Para programar actividad: partidaId, fechaInicio y fechaFin son obligatorios.** | ✅ Implemented |
| REGLA-107 | **La Línea Base requiere Presupuesto CONGELADO y Cronograma CONGELADO; la ausencia invalida ejecución.** | 🟡 Implemented |
| REGLA-132 | **Un programa de obra es único por proyecto (proyecto_id UNIQUE).** | ✅ Implemented |
| REGLA-133 | **Una actividad programada es única por partida (partida_id UNIQUE).** | ✅ Implemented |
| REGLA-147 | **Un Proyecto solo puede activarse si existe Cronograma congelado del mismo Proyecto; si no, se bloquea con el mensaje "Este proyecto no puede activarse sin un cronograma congelado."** | 🟡 Implemented |
| REGLA-148 | **Un Snapshot de Presupuesto sin Cronograma no constituye una Línea Base válida.** | 🟡 Implemented |
| REGLA-156 | **Toda Orden de Cambio que afecte plazo debe generar ajuste formal del Cronograma contractual.** | 🟡 Implemented |

## 3. Domain Events

| Event Name                   | Trigger         | Content (Payload)                   | Status |
| ---------------------------- | --------------- | ----------------------------------- | ------ |
| `CronogramaCongeladoEvent`   | Baseline frozen | `programaId`, `snapshotId`          | ✅     |
| `ActividadReprogramadaEvent` | Date change     | `actividadId`, `newStart`, `newEnd` | 🟡     |

## 4. State Constraints

```mermaid
graph TD
    NO_CONGELADO --> CONGELADO
    CONGELADO -->|Re-baseline| CONGELADO_V2
```

## 5. Data Contracts

### Entity: ProgramaObra

- `id`: UUID
- `fechaInicio`: Date
- `fechaFinEstimada`: Date

### JSON Schema (Evolution)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Dependencia",
  "properties": {
    "lag_days": { "type": "integer", "description": "Status: 🔴 Missing" },
    "type": {
      "type": "string",
      "enum": ["FS", "SS", "FF", "SF"],
      "description": "Status: 🟡 Only FS supported"
    }
  }
}
```

## 6. Use Cases

| ID     | Use Case               | Priority | Status |
| ------ | ---------------------- | -------- | ------ |
| UC-C01 | Create Activity        | P0       | ✅     |
| UC-C02 | Connect Dependencies   | P0       | ✅     |
| UC-C03 | Visualize Gantt        | P0       | ✅     |
| UC-C04 | Define Calendars       | P1       | 🔴     |
| UC-C05 | Import from MS Project | P2       | 🔴     |

## 7. Domain Services

- **Service**: `CronogramaService`
- **Responsibility**: Calculates dates based on dependencies and durations.
- **Methods**:
  - `recalcularFechas()`: Propagates changes through dependency chain.

## 8. REST Endpoints

| Method | Path                                            | Description      | Status |
| ------ | ----------------------------------------------- | ---------------- | ------ |
| POST   | `/api/v1/proyectos/{id}/cronograma/actividades` | Program activity | ✅     |
| GET    | `/api/v1/proyectos/{id}/cronograma`             | Get Gantt data   | ✅     |
| POST   | `/api/v1/proyectos/{id}/cronograma/baseline`    | Freeze baseline  | ✅     |

## 9. Observability

- **Metrics**: `schedule.activities.count`
- **Logs**: Circular dependency detection warnings.

## 10. Integration Points

- **Consumes**: `Presupuesto` (Partidas)
- **Exposes**: `PlannedProgress` to `EVM`

## 11. Technical Debt & Risks

- [ ] **Dependency Algorithm**: Current topological sort is naive and implemented in-memory. Needs robustness for large schedules. (High)
