# Implementation of REQ-3: Inventory & Warehouse Business Rules

## 🔗 Related Requirement
**REQ-3: REGLAS DE NEGOCIO — MÓDULO INVENTARIOS (BODEGA)**

## 📝 Description
This Pull Request implements the core business logic for the Inventory and Warehouse module (`Bodega`), serving as the "Physical Truth" of the project. It bridges the gap between Procurement ("Comprado") and Consumption ("Consumido").

## ✨ Key Features Implemented

### 1. Core Inventory Architecture
- **Multi-Warehouse Support**: `Bodega` entity with unique constraints per project.
- **Resource Snapshot**: Implementation of "Source of Truth" principle. `InventarioItem` snapshots critical data (External ID, Name, Unit, Classification) to ensure historical integrity even if the Master Catalog changes.
- **Stock Tracking**: Uniqueness enforced by `(project, resource, unit, warehouse)`.

### 2. Valuation & Costing
- **Weighted Average Cost (PMP)**: Automated PMP calculation on every stock entry (`ingresar()`).
- **Precision**: 4 decimal places for internal calculations, 2 for persistence.

### 3. Verification & Movements (Kardex)
- **Immutable History**: `MovimientoInventario` entity tracks all stock changes (Kardex) in an append-only manner.
- **Movement Types**: Support for Purchase Entry, Consumption, Transfers, and Adjustments.
- **Domain Events**: `MaterialConsumed`, `MaterialTransferredBetweenProjects`.

### 4. Requisition Workflow
- **State Machine**: Full lifecycle management:
  - `BORRADOR` -> `SOLICITADA` -> `APROBADA` -> `DESPACHADA_PARCIAL` -> `DESPACHADA_TOTAL` / `RECHAZADA`.
- **Validation**:
  - Stock availability checks.
  - Budget/Item validation (`PartidaValidator`).
  - Tolerance handling (e.g., 5% over-request limits).

### 5. Technical Improvements
- **Strategy Pattern**: `ValidationRuleExecutor` for extensible business rules.
- **Domain-Driven Design**: Clear separation of Aggregates (Requisicion), Entities, and Value Objects.
- **Stubbed Adapters**: `PartidaValidatorStub` and others to allow independent testing.

## ⚠️ Notes for Reviewers
- This PR replaces the previous description which incorrectly referenced REQ-4.
- Includes database migrations for inventory schema (Note: Check for potential version conflicts in V10/V11).
- Requires `POSTGRES_PASSWORD` in environment variables.

## ✅ Checklist
- [x] Entity implementation (InventarioItem, Bodega, Requisicion)
- [x] Business logic for PMP calculation
- [x] State machine for Requisitions
- [x] Database Migrations
- [x] Unit/Integration Tests
