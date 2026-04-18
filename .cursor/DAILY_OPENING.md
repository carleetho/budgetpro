# Ritual de apertura — master prompt del día

1. Sustituye los marcadores `«…»` (o rellena las líneas de contexto humano).
2. Sustituye `<MODULO>` por el nombre del archivo de notebook (ej. `RRHH`, `PRESUPUESTO`) sin la ruta completa.
3. Copia **todo el bloque** desde **--- INICIO MASTER PROMPT** hasta **--- FIN** en el primer mensaje del Agent.

---

## --- INICIO MASTER PROMPT ---

**BudgetPro — inicio de jornada (contexto para el Agent)**

**Humano — contexto operativo**

- Rama actual: «rama o N/A»
- Objetivo macro del día (una frase): «…»
- Módulo canónico principal (si aplica): «RRHH | PRESUPUESTO | EVM | CRONOGRAMA | ESTIMACION | COMPRAS | BILLETERA | INVENTARIO | CROSS_CUTTING | N/A»
- Riesgo percibido por el humano (opcional): «LOW | MID | HIGH | N/A»

**Adjuntos obligatorios de contexto (léelos antes de actuar)**

@docs/canonical/AI_AGENT_PROTOCOL.md  
@.cursor/canonical_context.md  
@docs/canonical/modules/<MODULO>_MODULE_CANONICAL.md  
@docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md

**Instrucciones al Agent**

1. **Git — estado inicial (obligatorio antes del primer parche):** desde la raíz del repo ejecuta `git branch --show-current`, `git status -sb` y, si hay cambios locales, `git diff --stat` (respecto de `HEAD` o del índice, según aplique). Compara la rama efectiva con la **rama declarada por el humano** arriba; si no coinciden, **detente** y aclara antes de editar. Resume en el chat: rama actual, resumen corto de working tree (M/modificados, A/añadidos, D/eliminados, ?? sin seguimiento) y si hay riesgo de pisar trabajo no commiteado.
2. Declara en una sola línea inicial: **Modo A** (as-built / evidencia código+git) o **Modo B** (implementación / negocio / contratos) según la petición de abajo, y justifica en una frase.
3. Si la petición es **ambigua o escueta**, **no implementes** hasta ejecutar clarificación tipo BrainGrid: preguntas con opciones **a / b / c / d / otro** (máx. 5–7), luego resume **criterios de aceptación** y pide confirmación. Puedes usar el comando del proyecto **`/bp-clarify`** o el skill `budgetpro-request-clarification`.
4. En **Modo B**, cumple la lectura canónica ya cubierta por los adjuntos; escanea `[AMBIGUITY_DETECTED]` y respeta paradas del protocolo.
5. **Blast radius:** no crees ni elimines **más de 10 archivos** en una sola tanda sin **confirmación explícita** del humano en el mismo hilo; si hace falta más alcance, propón **batches** temáticos (lista de paths prevista por lote) alineados al handbook / gobernanza del repo. Antes de un lote grande, vuelve a mostrar `git status -sb` o el conteo de paths afectados.
6. Si vas a tocar AXIOM o validadores, declara **MODE_0 / MODE_1 / MODE_2** según `.budgetpro/handbook/AXIOM_SAFE_OPERATIONS.md` y, si aplica, ejecuta `./axiom.sh --dry-run` desde la raíz del repo antes de proponer cambios masivos.

**Petición concreta de hoy**

«Describe aquí en 2–6 frases qué quieres: resultado, restricciones y cómo sabremos que está listo.»

**--- FIN MASTER PROMPT ---**

---

## Atajos

- Reglas y comandos del repo: `@.cursor/README.md`
- Solo clarificación: `/bp-clarify`
- Implementación bajo canónicos: `/bp-canonical-implement`
