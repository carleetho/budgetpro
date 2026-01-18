# PLAN DE RESOLUCIÓN DE DOMINIO — BUDGETPRO

## CRÍTICO

| ID | Regla(s) involucradas | Dominio afectado | Decisión requerida | Impacto si no se resuelve |
| --- | --- | --- | --- | --- |
| CD-01 | Estados de Proyecto incompatibles entre docs y migraciones: BORRADOR/ACTIVO/SUSPENDIDO/CERRADO vs BORRADOR/PAUSADO/EJECUCION/FINALIZADO | PROYECTO | definir | Incompatibilidad de estados entre documentación y esquema |
| CD-02 | Estados de Presupuesto incompatibles: BORRADOR/CONGELADO/INVALIDADO vs EN_EDICION/APROBADO vs BORRADOR/APROBADO/ANULADO | PRESUPUESTO | definir | Incompatibilidad de estados entre documentación y esquema |
| CD-04 | Regla “No hay cobro sin evidencia” contradice ingreso por aprobación de estimación con evidencia nula | CONTRATOS / COBROS, FINANZAS / BILLETERA | definir | Pérdida financiera / ruptura contractual |
| RS-01 | Cobro sin evidencia documental en ingresos de billetera | FINANZAS / BILLETERA | definir | Cobro sin evidencia documental |
| RH-CRIT-01 | Billetera: saldo no negativo y todo cambio exige movimiento (regla sin módulo canónico) | FINANZAS / BILLETERA | definir | Regla crítica sin documentación canónica |
| RH-CRIT-02 | Aprobar estimación genera ingreso en billetera (regla sin módulo canónico) | CONTRATOS / COBROS | definir | Regla crítica sin documentación canónica |

## ALTO

| ID | Regla(s) involucradas | Dominio afectado | Decisión requerida | Impacto si no se resuelve |
| --- | --- | --- | --- | --- |
| VD-01 | Amortización de anticipo sin regla explícita de origen del saldo pendiente (placeholder) | ESTIMACION | definir | Falta de trazabilidad del saldo de anticipo |
| VD-02 | Validación de saldo por partida/APU en compras marcada como opcional y no implementada | COMPRAS | definir | Sobreconsumo no gobernado |
| CD-03 | “Compra sin Partida es válida” vs detalle de compra requiere partidaId y valida existencia | COMPRAS | definir | Contradicción semántica de compra |
| RS-02 | Compra bloqueada por ausencia de billetera aunque exista proyecto | COMPRAS | definir | Bloqueo operativo por dependencia de billetera |
| RS-03 | Sobreconsumo no gobernado por ausencia de validación de saldo de partida/APU | COMPRAS | definir | Sobreconsumo no gobernado |
| FG-01 | Billetera/flujo de caja real sin módulo canónico de reglas | FINANZAS / BILLETERA | definir | Vacío de gobierno del flujo de caja |
| FG-02 | Anticipos y amortizaciones sin regla explícita de saldo pendiente (placeholder) | ESTIMACION | definir | Falta de trazabilidad del saldo de anticipo |
| FG-03 | Disponibilidad real vs planes de compra sin gobierno explícito | COMPRAS | definir | Desalineación entre plan y ejecución |

## MEDIO

| ID | Regla(s) involucradas | Dominio afectado | Decisión requerida | Impacto si no se resuelve |
| --- | --- | --- | --- | --- |
| RH-01 | Reglas de billetera existen en código sin módulo canónico en docs/modules | FINANZAS / BILLETERA | definir | Reglas sin documentación canónica |
| RH-02 | Reglas paramétricas de alertas existen en código sin módulo canónico en docs/modules | FINANZAS / ALERTAS | definir | Reglas sin documentación canónica |

