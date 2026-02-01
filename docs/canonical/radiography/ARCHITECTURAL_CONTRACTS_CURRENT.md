# ARCHITECTURAL_CONTRACTS_CURRENT.md - Current State Radiography

> **Scope**: Hexagonal Architecture
> **Last Updated**: 2026-01-31
> **Authors**: Antigravity

## 1. Overview

Maps the strict Hexagonal Architecture layers and boundaries currently enforced in the codebase.

## 2. Layer Definitions

### 2.1. Domain Layer (`com.budgetpro.domain`)

- **Role**: Pure business logic, Agnostic to frameworks.
- **Components**: Aggregates, Entities, Value Objects, Domain Services, Output Ports (Interfaces).
- **Dependencies**: None (Java Standard Library only).

### 2.2. Application Layer (`com.budgetpro.application`)

- **Role**: Orchestration, Use Cases.
- **Components**: Commands, Queries, UseCase Implementations, Input Ports.
- **Dependencies**: Domain.

### 2.3. Infrastructure Layer (`com.budgetpro.infrastructure`)

- **Role**: Adapters, Persistence, Web, Config.
- **Components**: REST Controllers, JPA Entities, Repository Implementations.
- **Dependencies**: Application, Domain, Spring Boot, Hibernate, 3rd Party Libs.

## 3. Current Architecture Diagram

```mermaid
graph TB
    subgraph "Infrastructure"
        REST[REST Controllers]
        DB[JPA Repositories]
    end

    subgraph "Application"
        UC[Use Cases]
        PortIn[Input Ports]
        PortOut[Output Ports]
    end

    subgraph "Domain"
        Agg[Aggregates]
        Svc[Domain Services]
    end

    REST --> PortIn
    PortIn <|.. UC
    UC --> Svc
    UC --> Agg
    UC --> PortOut

    PortOut <|.. DB
```

## 4. Contract Enforcements

| Contract       | Implementation                                              | Status                           |
| -------------- | ----------------------------------------------------------- | -------------------------------- |
| **Inbound**    | Use Cases implement Input Interfaces?                       | 🟡 (Direct UseCase usage common) |
| **Outbound**   | Repositories implement Domain Interfaces?                   | ✅                               |
| **Mapping**    | Infrastructure Entities mapped to Domain Entities manually? | ✅ (Mappers exist)               |
| **Validation** | Domain Invariants validated in Constructors/Methods?        | ✅                               |

## 5. Violations & Debt

- **UseCase strictness**: Some Controllers might be calling Repositories directly (need audit).
- **Lombok in Domain**: Usage of Lombok is restricted but exists in some Value Objects.
