## 1. Inventario de estados detectados

### PROYECTO
- **Docs** (`docs/modules/PROYECTO_SPECS.md`): `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.
- **Migración V2** (`V2__create_proyecto_presupuesto_schema.sql`): `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.
- **Migración V16** (`V16__core_immutable_schema.sql`): `BORRADOR`, `PAUSADO`, `EJECUCION`, `FINALIZADO` (y mapeo desde `ACTIVO`, `SUSPENDIDO`, `CERRADO`).
- **Diagnóstico FASE 2** (`docs/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md`): contradicción CD-01 entre estados de docs/V2 y V16.
- **Inventario FASE 3** (`docs/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md`): contradicción RCON-01 entre conjuntos de estados.
- **Inventario FASE 1** (`docs/INVENTARIO_REGLAS_EXISTENTES_FASE1.md`): referencias a checks de estados en V2 y V16.

### PRESUPUESTO
- **Docs** (`docs/modules/PRESUPUESTO_SPECS.md`): `BORRADOR`, `CONGELADO`, `INVALIDADO`.
- **Migración V2** (`V2__create_proyecto_presupuesto_schema.sql`): `EN_EDICION`, `APROBADO`.
- **Migración V16** (`V16__core_immutable_schema.sql`): `BORRADOR`, `APROBADO`, `ANULADO` (y mapeo desde `EN_EDICION` → `BORRADOR`).
- **Diagnóstico FASE 2** (`docs/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md`): contradicción CD-02 entre estados docs/V2 y V16.
- **Inventario FASE 3** (`docs/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md`): contradicción RCON-02 entre conjuntos de estados.
- **Inventario FASE 1** (`docs/INVENTARIO_REGLAS_EXISTENTES_FASE1.md`): referencias a checks de estados en V2 y V16.

---

## 2. Análisis semántico por estado

### PROYECTO (según `docs/modules/PROYECTO_SPECS.md`)

| Estado | Significado contractual | Habilita ejecución | Terminal/Transitorio |
| --- | --- | --- | --- |
| BORRADOR | Proyecto sin contrato digital; preparación administrativa. | No | Transitorio |
| ACTIVO | Proyecto con Línea Base congelada (Presupuesto + Cronograma). | Sí | Transitorio |
| SUSPENDIDO | Ejecución detenida por causa formal. | No | Transitorio |
| CERRADO | Proyecto finalizado; solo consulta/auditoría. | No | Terminal |

### PROYECTO (solo en migración V16)

| Estado | Significado contractual | Habilita ejecución | Terminal/Transitorio | Observación |
| --- | --- | --- | --- | --- |
| PAUSADO | No definido en fuentes documentales listadas. | No definido | No definido | Solo aparece en `V16__core_immutable_schema.sql`. |
| EJECUCION | No definido en fuentes documentales listadas. | No definido | No definido | Solo aparece en `V16__core_immutable_schema.sql`. |
| FINALIZADO | No definido en fuentes documentales listadas. | No definido | No definido | Solo aparece en `V16__core_immutable_schema.sql`. |

### PRESUPUESTO (según `docs/modules/PRESUPUESTO_SPECS.md`)

| Estado | Significado contractual | Habilita ejecución | Terminal/Transitorio |
| --- | --- | --- | --- |
| BORRADOR | Presupuesto editable; no habilita ejecución. | No | Transitorio |
| CONGELADO | Presupuesto aprobado; genera Snapshot inmutable. | Sí | Transitorio |
| INVALIDADO | Presupuesto reemplazado; solo auditoría. | No | Terminal |

### PRESUPUESTO (solo en migraciones V2/V16)

| Estado | Significado contractual | Habilita ejecución | Terminal/Transitorio | Observación |
| --- | --- | --- | --- | --- |
| EN_EDICION | No definido en fuentes documentales listadas. | No definido | No definido | Solo aparece en `V2__create_proyecto_presupuesto_schema.sql`. |
| APROBADO | No definido en fuentes documentales listadas. | No definido | No definido | Aparece en `V2__create_proyecto_presupuesto_schema.sql` y `V16__core_immutable_schema.sql`. |
| ANULADO | No definido en fuentes documentales listadas. | No definido | No definido | Solo aparece en `V16__core_immutable_schema.sql`. |

---

## 3. Propuesta de conjunto canónico

### PROYECTO
- **Canónico propuesto**: `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.
- **Estados a eliminar/renombrar**:
  - `EJECUCION` → `ACTIVO`
  - `PAUSADO` → `SUSPENDIDO`
  - `FINALIZADO` → `CERRADO`
- **Justificación**:
  - `docs/modules/PROYECTO_SPECS.md` define semántica contractual explícita para estos cuatro estados.
  - `V2__create_proyecto_presupuesto_schema.sql` usa este mismo set.
  - `V16__core_immutable_schema.sql` introduce un set alterno sin semántica documental en las fuentes listadas.

### PRESUPUESTO
- **Canónico propuesto**: `BORRADOR`, `CONGELADO`, `INVALIDADO`.
- **Estados a eliminar/renombrar**:
  - `EN_EDICION` → `BORRADOR`
  - `APROBADO` → `CONGELADO`
  - `ANULADO` → `INVALIDADO`
- **Justificación**:
  - `docs/modules/PRESUPUESTO_SPECS.md` define semántica contractual explícita para estos tres estados.
  - `V2__create_proyecto_presupuesto_schema.sql` y `V16__core_immutable_schema.sql` usan nomenclaturas alternas sin semántica documental en las fuentes listadas.

---

## 4. Tabla de equivalencias (legacy → canónico)

| Estado legacy | Dominio | Estado canónico propuesto | Acción |
| --- | --- | --- | --- |
| EJECUCION | PROYECTO | ACTIVO | Renombrar |
| PAUSADO | PROYECTO | SUSPENDIDO | Renombrar |
| FINALIZADO | PROYECTO | CERRADO | Renombrar |
| EN_EDICION | PRESUPUESTO | BORRADOR | Renombrar |
| APROBADO | PRESUPUESTO | CONGELADO | Renombrar |
| ANULADO | PRESUPUESTO | INVALIDADO | Renombrar |

---

## 5. Riesgos si no se adopta el canónico

### Riesgos operativos
- Estados incompatibles entre documentación y migraciones impiden aplicar reglas de activación y bloqueo definidas en `PROYECTO_SPECS`.

### Riesgos contractuales
- La semántica contractual de `ACTIVO/CONGELADO/INVALIDADO` queda sin correspondencia unívoca en BD, generando ambigüedad en gobernanza contractual.

### Riesgos técnicos
- Checks de base de datos en V2 y V16 validan conjuntos distintos, creando inconsistencia de validación y migraciones incompatibles con la semántica documental.

