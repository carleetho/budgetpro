# DOMAIN_INVARIANTS_CURRENT.md - Complete Rule Catalog

> **Scope**: Cross-cutting
> **Last Updated**: 2026-02-09
> **Total Rules**: 161
> **Source**: Canonical Notebooks

## 1. Overview
This document catalogs all 161 business rules across the BudgetPro domain. 
For detailed implementation specifications, technical traces, and code evidence, refer to the [Canonical Notebooks](../modules/).

## 2. Invariants by Module

### 2.1. Producción (RPC)
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-001 | Reporte aprobado es inmutable | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-002 | Fecha obligatoria, no futura | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-003 | Responsable usuario válido | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-004 | Cantidad reportada obligatoria <= metrado vigente | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-005 | Al menos un detalle requerido | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-006 | Estado inicial BORRADOR | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-007 | Solo APROBADO impacta costos | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-008 | Eliminación lógica solo en BORRADOR | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-009 | Reversión devuelve a BORRADOR | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-058 | Estado en {BORRADOR, APROBADO, RECHAZADO, ELIMINADO} | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-059 | Cantidad reportada non-negative per detail | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-076 | ResponsableID mandatory in Entity | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-077 | PartidaID mandatory in Detail | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-081 | Creation Request validation | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-082 | Detail Request validation | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-083 | Approval Request validation | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-084 | Rejection Request validation | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-139 | Legacy Creation Request | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-140 | Legacy Detail Request | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |
| REGLA-141 | Legacy Rejection Request | ✅ | [PRODUCCION](../modules/PRODUCCION_MODULE_CANONICAL.md) |

### 2.2. Estimaciones
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-010 | Transiciones BORRADOR→APROBADA→PAGADA | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-011 | Solo APROBADA genera deuda | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-012 | Solo PAGADA mueve billetera | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-013 | Periodo inicio <= fin | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-014 | Fecha certificación obligatoria | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-015 | Total positivo | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-016 | Volumen estimado <= contratado | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-017 | Amortización anticipo porcentual | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-066 | Estados válidos y montos positivos | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-087 | Validación Request generación | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-088 | Validación Request detalle | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-130 | Estimación única por número/proyecto | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |
| REGLA-131 | Detalle único por partida | ✅ | [ESTIMACION](../modules/ESTIMACION_MODULE_CANONICAL.md) |

### 2.3. Presupuesto
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-101 | Presupuesto aprobado es contrato inmutable | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-106 | Proyecto requires Presupuesto congelado | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-110 | Solo un presupuesto ACTIVO por Proyecto | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-111 | Estados: BORRADOR, CONGELADO, INVALIDADO | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-112 | Snapshot generation on Freeze | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-113 | Change Orders adjust BAC, preserve Original | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-114 | Change Order Cap ±20% | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-143 | Unique Baseline Budget | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-148 | Snapshot without Schedule is Invalid | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-152 | Frozen Budget Immutable | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-156 | Time-impacting Change Order needs Schedule | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-044 | Nombre/Proyecto/Estado obligatorios | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-045 | Aprobación marca esContractual | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-046 | APROBADO read-only | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-061 | Estado check constraints | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-071 | Moneda 3 chars | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-098 | Creation Request validation | ✅ | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-149 | Invalidation suspends Project | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-157 | Excess consumption handling | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-155 | CO adjustment visibility | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| REGLA-153 | Purchase links to Frozen Item | 🟡 | [PRESUPUESTO](../modules/PRESUPUESTO_MODULE_CANONICAL.md) |

### 2.4. Cronograma
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-018 | Fin >= Inicio | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-019 | No self-dependency | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-020 | Finish-to-Start constraint | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-021 | Program Fin >= Inicio | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-022 | Update prerequisites | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-067 | DB Temporal Constraints | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-089 | Activity Request Validation | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-107 | Baseline definition (Budget+Schedule) | 🟡 | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-132 | Unique Program per Project | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-133 | Unique Activity per Partida | ✅ | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-147 | Activation dependency | 🟡 | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-148 | Snapshot validity (Reciprocal) | 🟡 | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |
| REGLA-156 | CO Impact (Reciprocal) | 🟡 | [CRONOGRAMA](../modules/CRONOGRAMA_MODULE_CANONICAL.md) |

### 2.5. Inventarios
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-033 | Purchase process integration | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-049 | Salida requires Partida | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-050 | Default movement type | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-064 | Movement financial integrity | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-065 | Stock financial integrity | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-085 | Movement Request validation | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-117 | Purchase Generates Ingress | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-118 | Movement preconditions | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-119 | Illegal movements | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-120 | Egress reduces APU balance | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-121 | Auditors Exceptions | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-134 | Movement Types Enum | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-136 | Unique Warehouse Code | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-137 | Unique Stock Entry | ✅ | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-154 | Inventory without Partida illegal | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-102 | No actions outside budget | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-105 | Draft Project blocks operational | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |
| REGLA-150 | Active Project requirement | 🟡 | [INVENTARIO](../modules/INVENTARIO_MODULE_CANONICAL.md) |

### 2.6. Seguridad
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-051 | JWT Secret Length | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-052 | Public Routes | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-053 | CORS Policy | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-055 | Unique Email | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-056 | RBAC Roles | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-075 | Mandatory user fields | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-078 | Login Request | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-079 | Register Request | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |
| REGLA-138 | Token Expiration | ✅ | [SEGURIDAD](../modules/SEGURIDAD_MODULE_CANONICAL.md) |

### 2.7. Recursos
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-037 | Recurso Invariants | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |
| REGLA-039 | Provisional creation | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |
| REGLA-112 | Valid Types/States | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |
| REGLA-116 | Name Normalization | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |
| REGLA-1759 | Creation Request | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |
| REGLA-2242 | Unique Name | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |
| REGLA-2509 | Attribute initialization | ✅ | [RECURSOS](../modules/RECURSOS_MODULE_CANONICAL.md) |

### 2.8. Partidas
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-064 | Report validation (Reciprocal) | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-278 | Estimation validation (Reciprocal) | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-653 | Partida Invariants | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-671 | Hierarchical consistency | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-834 | Standard Metrado Immutable | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-852 | Default Metrado Vigente | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-1105 | Non-negative values | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |
| REGLA-1708 | Creation Request validation | ✅ | [PARTIDAS](../modules/PARTIDAS_MODULE_CANONICAL.md) |

### 2.9. APU
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-035 | Integrity (Partida+Inputs) | ✅ | [APU](../modules/APU_MODULE_CANONICAL.md) |
| REGLA-036 | Financial Consistency | ✅ | [APU](../modules/APU_MODULE_CANONICAL.md) |
| REGLA-094 | Creation Input List | ✅ | [APU](../modules/APU_MODULE_CANONICAL.md) |
| REGLA-095 | Input Item Validation | ✅ | [APU](../modules/APU_MODULE_CANONICAL.md) |

### 2.10. Alertas Paramétricas
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-026 | Equipo Zero Cost | ✅ | [ALERTAS](../modules/ALERTAS_MODULE_CANONICAL.md) |
| REGLA-027 | Acero Ratio | ✅ | [ALERTAS](../modules/ALERTAS_MODULE_CANONICAL.md) |
| REGLA-028 | Agregado Size | ✅ | [ALERTAS](../modules/ALERTAS_MODULE_CANONICAL.md) |
| REGLA-116 | Budget Overrun Warn | ✅ | [ALERTAS](../modules/ALERTAS_MODULE_CANONICAL.md) |
| REGLA-029 | Zero Unit Price Warn | ✅ | [ALERTAS](../modules/ALERTAS_MODULE_CANONICAL.md) |

### 2.11. Marketing
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-108 | Lead States | ✅ | [MARKETING](../modules/MARKETING_MODULE_CANONICAL.md) |
| REGLA-123 | Mandatory Fields | ✅ | [MARKETING](../modules/MARKETING_MODULE_CANONICAL.md) |
| REGLA-124 | Public API Constraints | ✅ | [MARKETING](../modules/MARKETING_MODULE_CANONICAL.md) |

### 2.12. Auditoría
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-167 | CreatedBy Mandatory | ✅ | [AUDITORIA](../modules/AUDITORIA_MODULE_CANONICAL.md) |
| REGLA-168 | Non-retroactive truth | ✅ | [AUDITORIA](../modules/AUDITORIA_MODULE_CANONICAL.md) |
| REGLA-169 | State Change Audit | ✅ | [AUDITORIA](../modules/AUDITORIA_MODULE_CANONICAL.md) |

### 2.13. RRHH
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-024 | Laboral Config Constraints | ✅ | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-025 | Positive Base Salary | ✅ | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-069 | DB Config Constraints | ✅ | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-090 | Request Config Validation | ✅ | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-122 | Total Cost Tracking | 🟡 | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-123 | Costo Empresa Calculation | 🟡 | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-124 | No Double Allocation | 🟡 | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |
| REGLA-125 | Tareo Validation | 🟡 | [RRHH](../modules/RRHH_MODULE_CANONICAL.md) |

### 2.14. EVM
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-040 | CPI/SPI Calculation | ✅ | [EVM](../modules/EVM_MODULE_CANONICAL.md) |
| REGLA-158 | EV Calculation | ✅ | [EVM](../modules/EVM_MODULE_CANONICAL.md) |
| REGLA-159 | AC Calculation | ✅ | [EVM](../modules/EVM_MODULE_CANONICAL.md) |
| REGLA-160 | PV Calculation | ✅ | [EVM](../modules/EVM_MODULE_CANONICAL.md) |

### 2.15. Compras
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-030 | Comparison Validation | ✅ | [COMPRAS](../modules/COMPRAS_MODULE_CANONICAL.md) |
| REGLA-031 | Detail Calculation | ✅ | [COMPRAS](../modules/COMPRAS_MODULE_CANONICAL.md) |
| REGLA-032 | Approval Logic | ✅ | [COMPRAS](../modules/COMPRAS_MODULE_CANONICAL.md) |
| REGLA-153 | Link to Budget | 🟡 | [COMPRAS](../modules/COMPRAS_MODULE_CANONICAL.md) |
| REGLA-116 | Soft Block Handling | ✅ | [COMPRAS](../modules/COMPRAS_MODULE_CANONICAL.md) |
| REGLA-117 | Inventory Trigger | 🟡 | [COMPRAS](../modules/COMPRAS_MODULE_CANONICAL.md) |

### 2.16. Billetera
| ID | Rule | Status | Reference |
|---|---|---|---|
| REGLA-012 | Ingress from Estimation | ✅ | [BILLETERA](../modules/BILLETERA_MODULE_CANONICAL.md) |
| REGLA-161 | Balance Non-negative | ✅ | [BILLETERA](../modules/BILLETERA_MODULE_CANONICAL.md) |

## 3. Summary Statistics
- **Total Rules Documented**: 161
- **Implemented (✅)**: ~80%
- **Policy/Partial (🟡)**: ~20%
- **Missing Rules**: 0
