# INTEGRATION_PATTERNS_CURRENT.md - Current State Radiography

> **Scope**: API & External Systems
> **Last Updated**: 2026-01-31
> **Authors**: Antigravity

## 1. Overview

Documents the current integration surfaces: Internal REST APIs and External System Adapters.

## 2. Internal REST API (Inbound)

### 2.1. Resource Oriented Architecture

The API follows standard REST principles grouped by Aggregate Root.

| Module          | Base Path                             | Key Capabilities                | Auth |
| --------------- | ------------------------------------- | ------------------------------- | ---- |
| **Proyecto**    | `/api/v1/proyectos`                   | CRUD, Status Mgmt               | JWT  |
| **Presupuesto** | `/api/v1/presupuestos`                | WBS manipulation, Freezing      | JWT  |
| **Estimacion**  | `/api/v1/proyectos/{id}/estimaciones` | Generate sequential estimations | JWT  |
| **Cronograma**  | `/api/v1/proyectos/{id}/cronograma`   | Gantt data, Activity scheduling | JWT  |
| **Compras**     | `/api/v1/compras`                     | Purchase registration           | JWT  |
| **Recurso**     | `/api/v1/recursos`                    | Autocomplete search             | JWT  |

## 3. External Integrations (Outbound)

### 3.1. CAPECO Catalog Adapter

- **Purpose**: Fetch latest construction resource prices and APUs.
- **Pattern**: Port/Adapter with Fallback Validation.
- **Caching**: L1/L2 Cache strategy implemented to reduce external calls.
- **Resilience**: Circuit Breaker configured (via generic resilience patterns).

### 3.2. Email (Resend)

- **Purpose**: Send system notifications (welcome, alerts).
- **Pattern**: Async Event Listener -> Email Service.

## 4. Messaging Patterns

- **Domain Events**: Internal synchronous event bus (Spring ApplicationEventPublisher).
- **Event Types**:
  - `PresupuestoAprobadoEvent` -> Triggers Cronograma Freezing.
  - `EstimacionAprobadaEvent` -> Triggers Billetera Ingress.

## 5. Security & Auth

- **Protocol**: Bearer Token (JWT).
- **Gateway**: Spring Security Filter Chain.
- **Cors**: Global configuration for Frontend access.
