# Guía de seguridad para asistentes de IA (Frontend) — REQ-47

> **Alcance:** desarrollo UI/FE en BudgetPro.  
> **No sustituye:** `docs/canonical/AI_AGENT_PROTOCOL.md` (protocolo general / backend).  
> **Madurez oficial:** `docs/canonical/radiography/MODULE_SPECS_CURRENT.md` y `SCOREBOARD_17.md` (code-first). Revisar esos archivos antes de confiar en % de este resumen.

## 1. Objetivo

Reducir alucinaciones al generar código React/Next (pantallas, hooks, clientes HTTP, Zod) cuando el módulo aún no tiene contrato estable. La UI **no inventa** REGLA-XXX ni máquinas de estado: las consume del API y del notebook canónico.

Relacionado: `docs/frontend/VALIDATION_STRATEGY.md` (quién valida qué).

## 2. Umbrales de madurez → uso de IA

| Tier | Umbral | Uso de IA en FE | Revisión humana |
|------|--------|-----------------|-----------------|
| **Alto** | ≥ 80% | Generación de UI/cliente permitida con spot-check | Diff + smoke del flujo |
| **Medio** | 50–79% | Generación permitida **solo con revisión antes de commit** | Checklist §5 obligatorio |
| **Bajo / riesgo elevado** | &lt; 50% **o** gaps P0 abiertos / API inestable | **Solo preguntas**; código a mano o muy acotado | No mergear UI “completa” del módulo |

### 2.1 Clasificación as-built (radiografía)

Fuente: `MODULE_SPECS_CURRENT.md` (última sync documentada en radiografía).

**Alto (≥80%)** — OK para generar UI con spot-check:

| Módulo | Madurez | Notebook |
|--------|---------|----------|
| EVM | 95% | `EVM_MODULE_CANONICAL.md` |
| APU | 90% | `APU_MODULE_CANONICAL.md` |
| Alertas | 90% | `ALERTAS_MODULE_CANONICAL.md` |
| Cross-Cutting | 90% | `CROSS_CUTTING_MODULE_CANONICAL.md` |
| Presupuesto | 80% | `PRESUPUESTO_MODULE_CANONICAL.md` |

**Medio (50–79%)** — generar con revisión obligatoria:

| Módulo | Madurez | Notebook |
|--------|---------|----------|
| Estimación | 75% | `ESTIMACION_MODULE_CANONICAL.md` |
| Compras | 75% | `COMPRAS_MODULE_CANONICAL.md` |
| Seguridad | 75% | `SEGURIDAD_MODULE_CANONICAL.md` |
| Billetera | 70% | `BILLETERA_MODULE_CANONICAL.md` |
| Inventario | 70% | `INVENTARIO_MODULE_CANONICAL.md` |
| Recursos | 70% | `RECURSOS_MODULE_CANONICAL.md` |
| Auditoría | 70% | `AUDITORIA_MODULE_CANONICAL.md` |
| Partidas | 65% | `PARTIDAS_MODULE_CANONICAL.md` |
| Cronograma | 60% | `CRONOGRAMA_MODULE_CANONICAL.md` |
| Producción | 55% | `PRODUCCION_MODULE_CANONICAL.md` |
| Marketing | 55% | `MARKETING_MODULE_CANONICAL.md` |
| RRHH | 50% | `RRHH_MODULE_CANONICAL.md` + `radiography/gaps/RRHH_GAP_STUDY.md` |

**Nota RRHH:** aunque el % as-built es ~50% (borde medio/bajo), el índice de alineación y el gap study marcan **riesgo alto de alucinación** (nómina incompleta, FSR dual, superficie REST aún en evolución). Para FE: tratar RRHH como **preguntas + implementación manual acotada**, no como “pantalla completa con IA”.

> **Discrepancia histórica:** el texto original de REQ-47 Task 6 citaba RRHH ~20% / grounding 1.5. **No usar esos números**; prevalece `MODULE_SPECS_CURRENT.md`.

## 3. Directrices de uso por tier

### Alto

- Permitido: páginas, tablas, formularios Zod, llamadas a endpoints **ya existentes** (verificar en OpenAPI/`/v3/api-docs` o controllers).
- Obligatorio: `@` notebook del módulo + `VALIDATION_STRATEGY.md` si hay forms.
- Prohibido: inventar paths REST o campos JSON no presentes en DTO/OpenAPI.

### Medio

- Igual que alto, más: **no commit** hasta checklist §5 en verde.
- Preferir wrappers sobre `apiClient` / `BudgetProApiError` existentes.
- Si el canónico tiene `[AMBIGUITY_DETECTED]` o gap P0: **parar** y preguntar (protocolo).

### Bajo / RRHH elevado

- Solo: “¿existe el endpoint X?”, “¿qué dice el invariante Y?”.
- No pedir a la IA: CRUD completo, nómina, asistencia “canónica” sin citar notebook + gap.
- Cualquier pantalla: stub + copy “En desarrollo”, sin fingir reglas de negocio.

## 4. Dónde mirar (canónicos)

| Recurso | Ruta |
|---------|------|
| Protocolo agentes | `docs/canonical/AI_AGENT_PROTOCOL.md` |
| Contratos hexagonales | `docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md` |
| Notebooks | `docs/canonical/modules/{MODULO}_MODULE_CANONICAL.md` |
| Madurez / gaps | `MODULE_SPECS_CURRENT.md`, `MODULE_CODE_ALIGNMENT_INDEX.md`, `gaps/` |
| Validación UI↔API | `docs/frontend/VALIDATION_STRATEGY.md` |

Cómo verificar una sugerencia de IA:

1. Abrir el notebook del módulo → sección Invariantes / REGLA-XXX.
2. Contrastar path HTTP y DTO en `infrastructure/rest/...`.
3. Confirmar manejo de error con `BudgetProApiError` (no inventar `ApiError` genérico).
4. Si notebook y código discrepan: **notebook manda** para negocio; declarar el conflicto en el PR/chat.

## 5. Checklist de verificación (antes de commit)

- [ ] El código FE no define REGLA-XXX nuevas; solo muestra mensajes del backend.
- [ ] Paths y payloads coinciden con controller/DTO o OpenAPI actual.
- [ ] Validación Zod ≤ validación Bean del DTO (no más estricta en negocio).
- [ ] Estados de UI (tabs, badges) no inventan transiciones de dominio.
- [ ] Errores usan `BudgetProApiError` / `fieldErrors`; se preserva el form.
- [ ] Módulo medio/bajo: leído el gap study si existe.
- [ ] Sin secretos ni JWT hardcodeados; auth vía flujo existente.

## 6. Patrones de alucinación frecuentes (FE)

| Patrón | Ejemplo | Mitigación |
|--------|---------|------------|
| Inventar reglas | “UI bloquea aprobación si CPI &lt; 1” sin notebook | No enforzar; solo mostrar datos EVM del API |
| State machine falsa | Botones de estados que el backend no expone | Listar solo acciones con endpoint real |
| Validación inventada | Zod exige campos que el DTO no tiene | Copiar constraints del DTO |
| Relaciones erróneas | Partida sin `presupuestoId` / WBS inventado | Seguir `CrearPartidaRequest` + canónico Partidas |
| Cálculos financieros en UI | Recalcular BAC/EAC en el browser como verdad | Mostrar cifras del API; cálculo solo UX aproximado |
| Endpoints fantasma | `POST /api/v1/rrhh/nomina/calcular` sin existir | Grep controllers / OpenAPI primero |
| Contratos de error inventados | Asumir siempre `error: "BUSINESS_RULE"` | Usar `businessCode` real (`BAD_REQUEST`, `CONFLICT`, …) |

## 7. Enfoque por fases (documentación / UI)

Objetivo: construir FE sobre APIs estables primero.

| Fase | Módulos FE prioritarios | Documentación / AI |
|------|-------------------------|--------------------|
| **1** | Presupuesto, Partidas, APU, Recursos, Cross-Cutting/Auth | Documentación completa; IA en tier alto/medio con checklist |
| **2** | Estimación, Billetera, EVM, Compras, Inventario | Docs con warnings de deuda; IA solo con revisión |
| **3** | RRHH, Producción (superficie dual), Marketing incompleto | Estructura / “Under Development”; IA = preguntas |

Racional: evita pantallas que congelan contratos REST aún en cambio y reduce rework.

## 8. Prueba rápida de usabilidad

Pregunta: *“¿Puedo usar IA para generar el módulo RRHH completo?”*  
Respuesta esperada: **No.** Solo preguntas y stubs; riesgo elevado; leer `RRHH_MODULE_CANONICAL.md` + `RRHH_GAP_STUDY.md`; no inventar nómina/asistencia.

Pregunta: *“¿Puedo generar UI del workspace de Presupuesto?”*  
Respuesta esperada: **Sí, con spot-check** (madurez ~80%, patrones ya en `frontend/src/modules/presupuestos`).

## 9. Fuera de alcance

- Cambiar `AI_AGENT_PROTOCOL.md` o reglas AXIOM del backend.
- Configurar Cursor/MCP (solo uso).
- Process de code review organizacional (más allá del checklist §5).
