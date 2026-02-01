# PRESUPUESTO Module - Canonical Specification

> **Status**: Complete (80%)
> **Owner**: Finanzas Team
> **Last Updated**: 2026-01-31

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State      | Deliverables                                    |
| ----------- | --------- | ----------------- | ----------------------------------------------- |
| **Current** | Now       | 80% (Core Stable) | CRUD, WBS, Freeze Logic, Snapshots              |
| **Next**    | +1 Month  | 85%               | Advanced Analytics, Export to Excel/PDF         |
| **Target**  | +3 Months | 95%               | Versioning v2 (History), Multi-currency Support |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                                                        | Status         |
| ---- | ----------------------------------------------------------------------------------------------------------- | -------------- |
| P-01 | **No Modification Frozen**: A budget cannot be modified (add/remove items) once it is in `CONGELADO` state. | ✅ Implemented |
| P-02 | **WBS Hierarchy**: Partidas must form a strict hierarchical tree structure (Parent-Child).                  | ✅ Implemented |
| P-03 | **Leaf Node APU**: Only leaf partidas (lowest level) can have an associated APU or APUSnapshot.             | ✅ Implemented |
| P-04 | **Snapshot Immutability**: APUSnapshots are immutable upon creation, except for `rendimientoVigente`.       | ✅ Implemented |
| P-05 | **Unique Item Code**: Each partida must have a unique WBS item code within the project.                     | ✅ Implemented |
| P-06 | **Indirect Costs**: Overhead calculations must be based on standard formulas (percentage of direct costs).  | ✅ Implemented |

## 3. Domain Events

| Event Name                 | Trigger             | Content (Payload)                          | Status             |
| -------------------------- | ------------------- | ------------------------------------------ | ------------------ |
| `PresupuestoCreadoEvent`   | New budget creation | `presupuestoId`, `proyectoId`              | ✅                 |
| `PresupuestoAprobadoEvent` | Freeze action       | `presupuestoId`, `totalMonto`, `timestamp` | ✅                 |
| `PartidaCreadaEvent`       | Adding a partida    | `partidaId`, `presupuestoId`               | 🟡 (Internal only) |

## 4. State Constraints

```mermaid
graph TD
    BORRADOR -->|Aprobar| CONGELADO
    CONGELADO -->|Reabrir (Admin)| BORRADOR
```

- **Constraint**: Transitions to CONGELADO trigger `ProgramaObra` freezing.

## 5. Data Contracts

### Entity: Presupuesto

- `id`: UUID (Immutable)
- `proyectoId`: UUID (Immutable)
- `nombre`: String
- `esContractual`: Boolean

### JSON Schema (Evolution)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Presupuesto",
  "properties": {
    "moneda": {
      "type": "string",
      "description": "Status: 🔴 Missing (Planned for v2)"
    }
  }
}
```

## 6. Use Cases

| ID     | Use Case              | Priority | Status |
| ------ | --------------------- | -------- | ------ |
| UC-P01 | Create Budget         | P0       | ✅     |
| UC-P02 | Add Partidas (WBS)    | P0       | ✅     |
| UC-P03 | Assign APU/Snapshot   | P0       | ✅     |
| UC-P04 | Approve/Freeze Budget | P0       | ✅     |
| UC-P05 | Clone Budget          | P2       | 🔴     |
| UC-P06 | Export to Excel       | P1       | 🔴     |

## 7. Domain Services

- **Service**: `PresupuestoService`
- **Responsibility**: Coordinator of invariants for budget aggregate.
- **Methods**:
  - `crear(command)`: Initializes root.
  - `aprobar(id)`: Validates completeness and freezes.

## 8. REST Endpoints

| Method | Path                                   | Description      | Status |
| ------ | -------------------------------------- | ---------------- | ------ |
| POST   | `/api/v1/presupuestos`                 | Create budget    | ✅     |
| POST   | `/api/v1/partidas`                     | Add partida      | ✅     |
| POST   | `/api/v1/presupuestos/{id}/aprobar`    | Freeze budget    | ✅     |
| PUT    | `/api/v1/presupuestos/{id}/sobrecosto` | Config overheads | ✅     |

## 9. Observability

- **Metrics**: `budget.created.count`, `budget.value.total`
- **Logs**: Audit log on `aprobar` (Critical Action)

## 10. Integration Points

- **Consumes**: `CatalogoService` (for Snapshots)
- **Exposes**: `PresupuestoAprobadoEvent` to `Cronograma` and `EVM`

## 11. Technical Debt & Risks

- [ ] **Legacy APUs**: Support for legacy non-snapshot APUs complicates validation logic. (Medium)
- [ ] **Recursion Performance**: Recursive WBS loading needs optimization for deep trees. (Low)
