# FASE 3 — INVENTARIO CANÓNICO DE REGLAS EXISTENTES

## 1. Reglas Extraídas del Código

### RC-01
- Descripción breve: La billetera no puede tener saldo negativo y todo cambio de saldo genera movimiento.
- Ubicación en código: `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java`
- Dominio canónico sugerido: FINANZAS / BILLETERA
- Estado:
  - explícita
- Impacto:
  - dinero

---

### RC-02
- Descripción breve: Un egreso con saldo resultante negativo lanza excepción.
- Ubicación en código: `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java` (`egresar`)
- Dominio canónico sugerido: FINANZAS / BILLETERA
- Estado:
  - explícita
- Impacto:
  - dinero

---

### RC-03
- Descripción breve: Aprobar estimación registra ingreso en billetera sin evidencia.
- Ubicación en código: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`
- Dominio canónico sugerido: CONTRATOS / COBROS
- Estado:
  - explícita
- Impacto:
  - dinero / contrato

---

### RC-04
- Descripción breve: Amortización de anticipo usa saldo pendiente placeholder.
- Ubicación en código: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java`
- Dominio canónico sugerido: CONTRATOS / COBROS
- Estado:
  - placeholder
- Impacto:
  - dinero / contrato

---

### RC-05
- Descripción breve: Validación de saldo por partida/APU en compras está marcada como opcional y no bloquea.
- Ubicación en código: `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`
- Dominio canónico sugerido: COMPRAS
- Estado:
  - explícita
- Impacto:
  - dinero / contrato

---

### RC-06
- Descripción breve: Registrar compra exige billetera existente del proyecto.
- Ubicación en código: `backend/src/main/java/com/budgetpro/application/compra/usecase/RegistrarCompraUseCaseImpl.java`
- Dominio canónico sugerido: FINANZAS / BILLETERA
- Estado:
  - explícita
- Impacto:
  - dinero

---

### RC-07
- Descripción breve: Cada detalle de compra requiere partidaId.
- Ubicación en código: `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/CompraDetalle.java`
- Dominio canónico sugerido: COMPRAS
- Estado:
  - explícita
- Impacto:
  - dinero / contrato

---

### RC-08
- Descripción breve: Alertas paramétricas: costo_horario=0, ratio acero/concreto fuera de rango, agregado > 1/5 del ancho.
- Ubicación en código: `backend/src/main/java/com/budgetpro/domain/finanzas/alertas/service/AnalizadorParametricoService.java`
- Dominio canónico sugerido: PRESUPUESTO
- Estado:
  - explícita
- Impacto:
  - dinero / métricas

---

## 2. Reglas Documentadas sin Implementación Clara

### RD-01
- Documento fuente: `docs/modules/COMPRAS_SPECS.md`
- Regla descrita: “Compra sin Partida es válida. Compra sin clasificación es ilegal.”
- Evidencia en código:
  - no
- Riesgo asociado:
  - ruptura contractual

---

### RD-02
- Documento fuente: `docs/context/01_mapa_dominio.md`, `docs/context/03_definicion_mvp.md`, `docs/context/09_erd_fisico_mermaid.md`
- Regla descrita: “No hay cobro sin evidencia.”
- Evidencia en código:
  - no
- Riesgo asociado:
  - pérdida financiera / ruptura contractual

---

## 3. Reglas Huérfanas Críticas

### RH-CRIT-01
- Regla: La billetera no permite saldo negativo y todo cambio exige movimiento.
- Dónde existe: `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java`
- Dónde debería documentarse: FINANZAS / BILLETERA
- Severidad: CRÍTICO

---

### RH-CRIT-02
- Regla: Aprobar estimación genera ingreso en billetera (cobro).
- Dónde existe: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`
- Dónde debería documentarse: CONTRATOS / COBROS
- Severidad: CRÍTICO

---

## 4. Reglas Contradichas (SIN RESOLVER)

### RCON-01
- Regla A: Estados Proyecto BORRADOR/ACTIVO/SUSPENDIDO/CERRADO.
- Regla B: Estados Proyecto BORRADOR/PAUSADO/EJECUCION/FINALIZADO.
- Dominios involucrados: PROYECTO
- Tipo de contradicción:
  - estado

---

### RCON-02
- Regla A: Estados Presupuesto BORRADOR/CONGELADO/INVALIDADO.
- Regla B: Estados Presupuesto EN_EDICION/APROBADO y BORRADOR/APROBADO/ANULADO.
- Dominios involucrados: PRESUPUESTO
- Tipo de contradicción:
  - estado

---

### RCON-03
- Regla A: Compra sin Partida es válida.
- Regla B: Detalle de compra requiere partidaId y se valida existencia.
- Dominios involucrados: COMPRAS
- Tipo de contradicción:
  - semántica

---

### RCON-04
- Regla A: No hay cobro sin evidencia.
- Regla B: Ingreso por aprobación de estimación se registra con evidencia null.
- Dominios involucrados: CONTRATOS / COBROS, FINANZAS / BILLETERA
- Tipo de contradicción:
  - flujo

---

## 5. Resumen Ejecutivo de Riesgo

- Total reglas identificadas: 16
- Reglas sin documento canónico: 2
- Reglas financieras críticas: 5
- Reglas temporales críticas: 0
- Reglas contractuales críticas: 4
