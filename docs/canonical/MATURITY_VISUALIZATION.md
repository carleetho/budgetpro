# Maturity Visualization Templates

> **Goal**: Communicate status at a glance.

## 1. Maturity Matrix (Markdown Table)

Good for detailed reports.

| Module          | Invariants (30%) | Use Cases (35%) | Tech Debt (20%) | Obs (15%) | **Overall** | **Level**  |
| --------------- | ---------------- | --------------- | --------------- | --------- | ----------- | ---------- |
| **Presupuesto** | 90%              | 85%             | 70%             | 50%       | **79%**     | Complete   |
| **EVM**         | 95%              | 95%             | 90%             | 40%       | **87%**     | Complete   |
| **RRHH**        | 20%              | 10%             | 90%             | 0%        | **27%**     | Skeletal   |

## 2. Mermaid Chart (Visual)

Good for presentations.

```mermaid
%%{init: {'theme':'base'}}%%
graph LR
    subgraph "Level 3: Complete"
        P[Presupuesto: 79%]
        X[Cross-Cutting: 90%]
        E[EVM: 87%]
    end

    subgraph "Level 2: Functional"
        C[Cronograma: 45%]
        I[Inventario: 50%]
    end

    subgraph "Level 1: Skeletal"
        R[RRHH: 27%]
    end

    style P fill:#4caf50,stroke:#333,color:white
    style X fill:#4caf50,stroke:#333,color:white
    style E fill:#4caf50,stroke:#333,color:white
    style R fill:#ff5252,stroke:#333,color:white
```

## 3. Progression Tracker

Track delta since last quarter.

### Q1 2026 Snapshot

- **Presupuesto**: 79% (—)
- **EVM**: 87% (↑ 49%) - _Epic Complete (REQ-61/62/63/64)_
- **RRHH**: 27% (—) - _Stagnant_
