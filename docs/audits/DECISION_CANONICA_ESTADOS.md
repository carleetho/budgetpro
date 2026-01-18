# DECISION_CANONICA_ESTADOS.md

## 1. Propósito del documento
- Congelar una decisión de dominio sobre estados canónicos.
- Evitar ambigüedad futura entre documentación, migraciones y nomenclaturas técnicas.

---

## 2. Dominio PROYECTO

### Estados canónicos válidos
- **BORRADOR**: Proyecto sin contrato digital; no habilita ejecución.
- **ACTIVO**: Proyecto con Línea Base congelada (Presupuesto + Cronograma); habilita ejecución.
- **SUSPENDIDO**: Proyecto con ejecución detenida por causas formales; no permite nuevas operaciones.
- **CERRADO**: Proyecto finalizado; solo admite consulta y auditoría.

### Estados legacy detectados
- Migración V2: `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.
- Migración V16: `BORRADOR`, `PAUSADO`, `EJECUCION`, `FINALIZADO` (con mapeo explícito desde ACTIVO/SUSPENDIDO/CERRADO).

### Declaración de no-uso de legacy
Los estados legacy (`PAUSADO`, `EJECUCION`, `FINALIZADO`) se consideran **alias técnicos** sin semántica contractual.  
Las reglas de negocio y la documentación deben usar **exclusivamente** los estados canónicos.

---

## 3. Dominio PRESUPUESTO

### Estados canónicos válidos
- **BORRADOR**: Presupuesto editable; no habilita ejecución.
- **CONGELADO**: Presupuesto aprobado formalmente; genera Snapshot inmutable y habilita ejecución.
- **INVALIDADO**: Presupuesto reemplazado; solo auditoría.

### Estados legacy detectados
- Migración V2: `EN_EDICION`, `APROBADO`.
- Migración V16: `BORRADOR`, `APROBADO`, `ANULADO` (con mapeo explícito desde EN_EDICION → BORRADOR).

### Declaración de no-uso de legacy
Los estados legacy (`EN_EDICION`, `APROBADO`, `ANULADO`) se consideran **alias técnicos** sin semántica contractual.  
Las reglas de negocio y la documentación deben usar **exclusivamente** los estados canónicos.

---

## 4. Tabla oficial de equivalencias

| Estado legacy | Dominio | Estado canónico | Acción |
| --- | --- | --- | --- |
| BORRADOR | PROYECTO | BORRADOR | mapear |
| ACTIVO | PROYECTO | ACTIVO | mapear |
| SUSPENDIDO | PROYECTO | SUSPENDIDO | mapear |
| CERRADO | PROYECTO | CERRADO | mapear |
| EJECUCION | PROYECTO | ACTIVO | renombrar (mapeo explícito en V16) |
| PAUSADO | PROYECTO | SUSPENDIDO | renombrar (mapeo explícito en V16) |
| FINALIZADO | PROYECTO | CERRADO | renombrar (mapeo explícito en V16) |
| EN_EDICION | PRESUPUESTO | BORRADOR | renombrar (mapeo explícito en V16) |
| APROBADO | PRESUPUESTO | CONGELADO | mapear (CONGELADO = aprobado formalmente) |
| ANULADO | PRESUPUESTO | INVALIDADO | **ambigüedad**: ANULADO no está definido en fuentes canónicas; requiere validación explícita |

---

## 5. Regla de obligatoriedad
Este documento prevalece sobre:
- migraciones históricas,
- implementaciones previas,
- nomenclaturas técnicas alternativas.

---

## 6. Alcance y exclusiones
- No modifica código.
- No ejecuta migraciones.
- No redefine flujos.
