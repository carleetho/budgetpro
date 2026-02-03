# Construction Standards Reference

> **Purpose**: Authoritative sources to resolve domain ambiguities.

## 1. Earned Value Management (EVM)

_Standard: ANSI/EIA-748_

- **Planned Value (PV)**: Budgeted cost of work scheduled.
- **Earned Value (EV)**: Budgeted cost of work performed.
- **Actual Cost (AC)**: Actual cost incurred.
- **Variance**:
  - `SV = EV - PV` (Schedule Variance)
  - `CV = EV - AC` (Cost Variance)
- **Indices**:
  - `SPI = EV / PV`
  - `CPI = EV / AC`

**Rule**: All EVM calculations in BudgetPro MUST follow these formulas. No custom variations.

## 2. Project Management Institute (PMI)

_Standard: PMBOK Guide_

- **WBS**: Hierarchical decomposition of total scope.
- **Change Control**: Approved changes must modify the Baseline.

## 3. Local Regulations (Peru/LatAm)

_Context: Public & Private Works_

- **IGV/VAT**: 18% standard.
- **Detracciones**: Specialized withholding tax for services.
- **Fondo de Garantía**: typically 5-10% retention until project completion.
- **Civil Construction Regime**: Weekly payments, specific hierarchal categories (Peón, Oficial, Operario).

## 4. Best Practices

- **Snapshot Immutability**: Historical financial data should never change.
- **FIFO Inventory**: Stock consumption usually assumes First-In-First-Out for cost averaging (though Weighted Avg is acceptable if consistent).
