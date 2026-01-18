# FASE 2 — DIAGNÓSTICO DE DOMINIO BUDGETPRO

## 1. Vacíos de Dominio Detectados
- VD-01: Existe cálculo de amortización de anticipo, pero no existe regla explícita de origen del saldo de anticipo pendiente (se usa placeholder). Fuente: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java` (`saldoAnticipoPendiente = new BigDecimal("999999999"); // Placeholder`).
- VD-02: Existe ejecución de compras y consumos, pero la validación de saldo disponible por partida/APU está marcada como opcional y no implementada. Fuente: `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java` (comentario “Opcional MVP... no bloquea”).

## 2. Contradicciones Identificadas
- CD-01: Estados de Proyecto incompatibles entre documentos y migraciones: `PROYECTO_SPECS` define BORRADOR/ACTIVO/SUSPENDIDO/CERRADO, V2 usa BORRADOR/ACTIVO/SUSPENDIDO/CERRADO, V16 usa BORRADOR/PAUSADO/EJECUCION/FINALIZADO. Fuentes: `docs/modules/PROYECTO_SPECS.md`, `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`, `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`.
- CD-02: Estados de Presupuesto incompatibles: `PRESUPUESTO_SPECS` define BORRADOR/CONGELADO/INVALIDADO, V2 usa EN_EDICION/APROBADO, V16 usa BORRADOR/APROBADO/ANULADO. Fuentes: `docs/modules/PRESUPUESTO_SPECS.md`, `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`, `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`.
- CD-03: En COMPRAS se declara “Compra sin Partida es válida”, pero el dominio exige `partidaId` y falla si la partida no existe. Fuentes: `docs/modules/COMPRAS_SPECS.md`, `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/CompraDetalle.java`, `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`.
- CD-04: “No hay cobro sin evidencia” en docs, pero el ingreso a billetera por aprobación de estimación registra evidencia nula. Fuentes: `docs/context/01_mapa_dominio.md`, `docs/context/03_definicion_mvp.md`, `docs/context/09_erd_fisico_mermaid.md`, `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`.

## 3. Riesgos Sistémicos
- RS-01: Cobro sin evidencia documental en ingresos de billetera (evidencia null). Fuente: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`.
- RS-02: Compra bloqueada por ausencia de billetera aunque exista proyecto (billetera es obligatoria y no se crea en compras). Fuente: `backend/src/main/java/com/budgetpro/application/compra/usecase/RegistrarCompraUseCaseImpl.java`.
- RS-03: Sobreconsumo no gobernado por ausencia de validación de saldo de partida/APU en compras. Fuente: `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`.

## 4. Reglas Huérfanas
- RH-01: Reglas de billetera (saldo no negativo, no existe dinero sin movimiento) existen en código sin módulo canónico en `docs/modules`. Fuentes: `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java`, búsqueda sin coincidencias en `docs/modules`.
- RH-02: Reglas paramétricas de alertas (costo_horario=0, ratio acero/concreto, tamaño de agregado) existen en código sin módulo canónico en `docs/modules`. Fuentes: `backend/src/main/java/com/budgetpro/domain/finanzas/alertas/service/AnalizadorParametricoService.java`, búsqueda sin coincidencias en `docs/modules`.

## 5. Flujos Críticos sin Gobierno Explícito
- FG-01: Billetera/flujo de caja real no tiene módulo canónico de reglas en `docs/modules`. Fuentes: `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java`, búsqueda sin coincidencias en `docs/modules`.
- FG-02: Anticipos y amortizaciones operan sin regla explícita de saldo pendiente (placeholder). Fuente: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java`.
- FG-03: Disponibilidad real vs planes de compra (saldo de partida/APU) carece de gobierno explícito en compras. Fuente: `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`.

## 6. Severidad
Clasifica cada hallazgo como:
- VD-01: ALTO
- VD-02: ALTO
- CD-01: CRÍTICO
- CD-02: CRÍTICO
- CD-03: ALTO
- CD-04: CRÍTICO
- RS-01: CRÍTICO
- RS-02: ALTO
- RS-03: ALTO
- RH-01: MEDIO
- RH-02: MEDIO
- FG-01: ALTO
- FG-02: ALTO
- FG-03: ALTO
