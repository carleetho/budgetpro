# Maturity Scoring Rubric

> **Formula**: Weighted average of 4 key dimensions.

## 1. Dimensions & Weighting

| Dimension            | Weight | Metric Source                  |
| -------------------- | ------ | ------------------------------ |
| **1. Invariants**    | 30%    | `DOMAIN_INVARIANTS_CURRENT.md` |
| **2. Use Cases**     | 35%    | Module Notebook (Section 6)    |
| **3. Tech Debt**     | 20%    | Module Notebook (Section 11)   |
| **4. Observability** | 15%    | Module Notebook (Section 9)    |

## 2. Calculation Logic

### 2.1. Invariants Score (30%)

$$ Score = \frac{Implemented + (Partial \times 0.5)}{Total} \times 100 $$

### 2.2. Use Cases Score (35%)

Weighted by Priority:

- **P0**: 1.0 points
- **P1**: 0.7 points
- **P2**: 0.3 points

$$ Score = \frac{\sum (Weight \times Status)}{\sum TotalPossibleWeight} \times 100 $$

### 2.3. Technical Debt Score (20%)

Base: 100 Points. Deduct for debt severity.

- **Critical**: -30 pts
- **High**: -20 pts
- **Medium**: -10 pts
- **Low**: -5 pts

$$ Score = \max(0, 100 - TotalDeductions) $$

### 2.4. Observability Score (15%)

- **Level 0**: None (0%)
- **Level 1**: Logs Only (25%)
- **Level 2**: Structured Logs + Basic Metrics (50%)
- **Level 3**: Full Metrics + Tracing (75%)
- **Level 4**: SLOs + Alerting (100%)

## 3. Overall Score Example (Presupuesto)

- **Invariants**: 12/15 implemented, 3 partial. Score: 90%.
- **Use Cases**: 90% weighted completion.
- **Tech Debt**: 2 High issues (-40). Score: 60%.
- **Observability**: Level 2 (50%).

$$ Final = (90 \times 0.3) + (90 \times 0.35) + (60 \times 0.2) + (50 \times 0.15) $$
$$ Final = 27 + 31.5 + 12 + 7.5 = 78\% $$
**Result**: Complete (Level 3).
