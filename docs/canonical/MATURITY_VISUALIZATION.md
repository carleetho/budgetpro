# Maturity Visualization Templates

> **Goal**: Communicate status at a glance.

## 1. Maturity Matrix (Markdown Table)

Good for detailed reports.

| Module          | Invariants (30%) | Use Cases (35%) | Tech Debt (20%) | Obs (15%) | **Overall** | **Level**  |
| --------------- | ---------------- | --------------- | --------------- | --------- | ----------- | ---------- |
| **Presupuesto** | 90%              | 85%             | 70%             | 50%       | **79%**     | Complete   |
| **EVM**         | 40%              | 30%             | 80%             | 25%       | **38%**     | Functional |
| **RRHH**        | 20%              | 10%             | 90%             | 0%        | **27%**     | Skeletal   |

## 2. Mermaid Chart (Visual)

Good for presentations.

```mermaid
%%{init: {'theme':'base'}}%%
graph LR
    subgraph "Level 3: Complete"
        P[Presupuesto: 79%]
        X[Cross-Cutting: 90%]
    end

    subgraph "Level 2: Functional"
        E[EVM: 38%]
        C[Cronograma: 45%]
        I[Inventario: 50%]
    end

    subgraph "Level 1: Skeletal"
        R[RRHH: 27%]
    end

    style P fill:#4caf50,stroke:#333,color:white
    style X fill:#4caf50,stroke:#333,color:white
    style E fill:#ffeb3b,stroke:#333
    style R fill:#ff5252,stroke:#333,color:white
```

## 3. Progression Tracker

Track delta since last quarter.

### Q2 2026 Snapshot

- **Presupuesto**: 79% (↑ 5%)
- **EVM**: 38% (↑ 12%) - _Fast Mover_
- **RRHH**: 27% (—) - _Stagnant_
