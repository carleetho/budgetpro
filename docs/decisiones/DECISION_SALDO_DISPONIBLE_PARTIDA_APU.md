# DECISIÓN CANÓNICA — SALDO DISPONIBLE PARTIDA/APU

## 1. Propósito
Cerrar VD-02 / RS-03 / FG-03 sin introducir reglas nuevas ni fórmulas contractuales.

## 2. Hallazgos confirmados (con citas)
- No existe definición canónica explícita de “saldo disponible” por Partida/APU en las fuentes revisadas. Las fuentes solo describen topes, consumos esperados y validaciones generales sin fórmula ni fuente de cálculo. (Fuentes: `docs/modules/PRESUPUESTO_SPECS.md`, `docs/modules/COMPRAS_SPECS.md`, `docs/modules/INVENTARIOS_SPECS.md`)
- Existe exceso de consumo como realidad operativa documentada, pero no se define su cuantificación contractual; se exige visibilidad y registro como excepción u orden de cambio. (Fuente: `docs/modules/PRESUPUESTO_SPECS.md`)
- El diagnóstico vigente confirma el vacío: la validación de saldo disponible por partida/APU está marcada como opcional y no implementada. (Fuente: `docs/audits/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md`)
- El inventario canónico reconoce el vacío: no hay mecanismo definido para saldo disponible ni sobreconsumo, solo alertas mencionadas. (Fuente: `docs/audits/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md`)

## 3. Decisiones de dominio (SIN inventar)

### 3.1 Fuente del saldo disponible
No existe fuente canónica definida actualmente.

### 3.2 Existencia de sobreconsumo
El concepto está definido documentalmente como realidad operativa, pero no existe definición contractual vigente de su cuantificación.

### 3.3 Naturaleza del control
Alerta (no bloqueo) según documentación existente; no se define bloqueo automático.

## 4. Consecuencia operativa inmediata
- El sistema no puede implementar cálculo de “saldo disponible” ni cuantificación de sobreconsumo sin una definición canónica explícita.
- COMPRAS, INVENTARIOS y ALERTAS tienen prohibido inferir comportamiento o fórmulas de saldo disponible/sobreconsumo.

## 5. Estado del pendiente
- VD-02: PENDIENTE POR AUSENCIA DE DEFINICIÓN CANÓNICA.
- RS-03: PENDIENTE POR AUSENCIA DE DEFINICIÓN CANÓNICA.
- FG-03: PENDIENTE POR AUSENCIA DE DEFINICIÓN CANÓNICA.
