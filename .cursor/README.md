# Cursor — cadena de herramientas BudgetPro

Este directorio concentra **reglas**, **skills** y **comandos** del proyecto. La gobernanza base del Agent está en `.cursor/rules/budgetpro-agent-governance.mdc` (siempre activa).

## Ritual de apertura del día

Plantilla lista para copiar y pegar (contexto + `@` a protocolo, índice y notebook de módulo): **[`DAILY_OPENING.md`](DAILY_OPENING.md)**. El bloque incluye **apertura Git** (`branch`, `status`, `diff --stat`) antes del primer parche y tope de **blast radius** (10 archivos por tanda sin confirmación explícita).

Si la petición del día sigue siendo vaga, usa antes **`/bp-clarify`** (clarificación con opciones a/b/c/d/otro).

## Reglas (`.cursor/rules/`)

| Archivo | Aplicación |
|--------|------------|
| `budgetpro-agent-governance.mdc` | Siempre (`alwaysApply: true`) — Modo A vs B, anti-alucinación, canónicos. |
| `budgetpro-delegation.mdc` | Siempre — cuándo delegar en subagentes. |
| `budgetpro-axiom-handbook.mdc` | Por **globs** — AXIOM, handbook, Semgrep, domain-validator (ver tabla abajo). |
| `budgetpro-domain-layer.mdc` | Por **globs** — `com.budgetpro.domain` (main + test). |

### Tipos de aplicación (Cursor)

En **Cursor Settings → Rules, Commands** cada regla de proyecto tiene un **tipo** que corresponde al frontmatter:

| Tipo en UI | Frontmatter típico | Cuándo entra al contexto del Agent |
|------------|-------------------|-------------------------------------|
| **Always Apply** | `alwaysApply: true` | En conversaciones del **Agent (chat)** de forma habitual, sin depender del archivo abierto. |
| **Apply to Specific Files** | `alwaysApply: false` + **`globs`** | Cuando hay archivos que **coinciden con el patrón** en el **contexto** de la conversación (p. ej. adjuntos, archivos abiertos o referenciados). Si no hay coincidencias, la regla puede no inyectarse. |
| **Apply Intelligently** | `alwaysApply: false` y **sin** `globs` (o globs vacío) | El Agent decide por la **descripción** si la regla es relevante; conviene que la `description` sea muy concreta. |
| **Apply Manually** | (igual + uso en chat) | Solo si el usuario **@menciona** la regla o el archivo de regla. |

**Precedencia entre fuentes** (cuando chocan instrucciones): en planes Team, suele ser **Team Rules → Project Rules → User Rules**; dentro del proyecto, varias reglas **aplicables se fusionan** — evita mensajes contradictorios entre archivos.

### `globs` en este repo (sintaxis)

- Usa **lista YAML** (varias líneas con `-`), no una sola cadena separada por comas con espacios, para varios patrones.
- Los patrones son de estilo **minimatch** (p. ej. `**` recursivo). Rutas relativas al **raíz del workspace** salvo que Cursor indique lo contrario en tu versión.

**Patrones activos en `budgetpro-axiom-handbook.mdc`:**

| Patrón | Qué cubre |
|--------|-----------|
| `axiom.sh` | Script raíz del pipeline AXIOM. |
| `.budgetpro/**` | Config y handbook (`AXIOM_SAFE_OPERATIONS.md`, `axiom.config.yaml`, etc.). |
| `tools/axiom/**` | Sentinel y tooling Python AXIOM. |
| `tools/domain-validator/**` | Validador de dominio (incluye `boundary-rules.json` y `state-machine-rules.yml` bajo `src/main/resources`). |
| `.semgrep/**` | Reglas y pruebas Semgrep del repo. |

**Patrones en `budgetpro-domain-layer.mdc`:**

| Patrón | Qué cubre |
|--------|-----------|
| `backend/src/main/java/com/budgetpro/domain/**/*.java` | Dominio de producción. |
| `backend/src/test/java/com/budgetpro/domain/**/*.java` | Tests de dominio. |

**Si una regla con globs “no se aplica”:** abre o adjunta (`@`) al menos un archivo que coincida, o menciona la regla con `@` en el chat (véase FAQ oficial: tipo de regla y coincidencia de patrones).

## Skills (`.cursor/skills/`)

Invocación: en el chat, `/` + nombre del skill o `@` sobre `SKILL.md`.

| Skill | Uso |
|-------|-----|
| `budgetpro-canonical-implement` | Implementar negocio bajo notebooks + protocolo. |
| `budgetpro-as-built-audit` | Responder “qué hay hecho” con evidencia código/git. |
| `budgetpro-axiom-gate` | Sesión AXIOM: handbook, `./axiom.sh`, modos. |
| `budgetpro-request-clarification` | Petición ambigua → preguntas a/b/c/d/otro + criterios de aceptación. |

## Comandos (`.cursor/commands/`)

Atajos que **apuntan** al skill correspondiente (sin duplicar el procedimiento).

| Comando | Skill |
|---------|--------|
| `bp-canonical-implement` | `budgetpro-canonical-implement` |
| `bp-as-built-audit` | `budgetpro-as-built-audit` |
| `bp-axiom-gate` | `budgetpro-axiom-gate` |
| `bp-explore-delegate` | Exploración amplia vía subagente `explore` |
| `bp-clarify` | `budgetpro-request-clarification` |

## Skills globales de Cursor (creator / installer)

No copies esos skills al repo: viven en la instalación de Cursor.

- **Crear regla del proyecto**: en el chat, `/create-rule` (skill integrada).
- **Crear skill del proyecto**: `/create-skill` → guardar bajo `.cursor/skills/<nombre>/SKILL.md`.
- **Instalar skills de terceros** (entorno Codex / otra convención): usa el flujo de **skill-installer** en tu máquina (`~/.codex/skills/...`) solo para skills **personales o globales**; para BudgetPro, prioriza skills en **`.cursor/skills/`** versionados con git.

## Índice canónico

Para rutas de notebooks y radiografía: `@.cursor/canonical_context.md`.
