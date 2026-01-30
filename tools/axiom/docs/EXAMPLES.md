# AXIOM Usage Examples

## Scenario Based Configurations

### 1. Minimal Configuration

For a small project where you just want to limit massive PRs.

```yaml
validators:
  blast_radius:
    threshold: 50
    strictness: "warning"

protection_zones:
  green:
    - path: "src"
      max_files: 50
```

### 2. Strict Core Protection

The default for **BudgetPro**. Protects the domain model aggressively.

```yaml
protection_zones:
  red:
    - path: "domain/presupuesto"
      max_files: 1
    - path: "domain/estimacion"
      max_files: 1
validators:
  blast_radius:
    threshold: 10
    strictness: "blocking"
```

## Using Overrides

When you need to make a valid large change (e.g., "Big Bang" refactor or module update), use override keywords in your **commit message**.

| Keyword                | Effect                                                      |
| ---------------------- | ----------------------------------------------------------- |
| `BIGBANG_APPROVED`     | Bypasses **all** blast radius limits globally.              |
| `OVERRIDE_ESTIMACION`  | Bypasses file limits for `domain/estimacion`.               |
| `OVERRIDE_PRESUPUESTO` | Bypasses file limits for `domain/presupuesto`.              |
| `OVERRIDE_DOMAIN_CORE` | Bypasses file limits for all domain entities/value objects. |

**Example Commit Message:**

```text
Refactor entire estimation module to use new calculation engine.

BIGBANG_APPROVED
OVERRIDE_ESTIMACION
```

## Python API Usage

Developers building tools on top of AXIOM can use the configuration API.

### Loading Configuration

```python
from tools.axiom.config_loader import load_axiom_config, ConfigurationError

try:
    config = load_axiom_config()
    print(f"Blast Radius Threshold: {config.validators['blast_radius']['threshold']}")
except ConfigurationError as e:
    print(f"Invalid Configuration: {e}")
```

### Detecting Overrides

```python
from tools.axiom.override_detector import detect_overrides

commit_msg = "fix: critical bug OVERRIDE_ESTIMACION"
overrides = detect_overrides(commit_msg)

if "domain/estimacion" in overrides.bypass_zones:
    print("Bypassing checks for Estimacion module")
```

## Troubleshooting

### "Configuration file not found"

- **Cause**: `axiom.config.yaml` is missing from the project root.
- **Fix**: Create the file or allowing AXIOM to run with system defaults (it will log a warning).

### "Overlapping protection zones"

- **Cause**: You defined `domain` and `domain/submodule` in the config.
- **Fix**: Remove the parent zone or make zones disjoint. AXIOM requires unambiguous path matching.

### "At least one reporter must be enabled"

- **Cause**: You explicitly set `enabled: false` for console, log_file, and metrics.
- **Fix**: Enable at least one to ensure you can see the validation results.

## AI Assistant Integration Examples

### 1. Spanish Language Configuration

BudgetPro uses Spanish for domain concepts. The AI configuration supports this natively to keep instructions consistent with the codebase.

```yaml
system:
  role: "Arquitecto de Software Senior"
  priorities:
    - "La integridad de los datos es la prioridad número uno."
    - "Usar nombres en español para el dominio (ej. Presupuesto, Partida)."

axioms:
  prohibitions:
    - rule: "No usar 'utils' como nombre de paquete"
      reason: "Agrupar por funcionalidad, no por tipo."
      severity: "WARNING"
```

### 2. Hexagonal Architecture Strictness

Enforcing the dependency rule explicitly in the `.cursorrules`.

```yaml
axioms:
  hexagonal_boundaries:
    permitted:
      - "Adapter -> Port"
      - "Service -> Entity"
    forbidden:
      - "Entity -> Repository (Implementation)"
      - "Domain -> Spring Framework"
```

This generates clear "✅ Permitted" and "❌ Forbidden" sections in the AI instructions, helping Copilots avoid suggesting bad imports.

### 3. Generated .cursorrules Preview

When you run `tools/axiom/sync_cursorrules.py`, the output looks like this:

```markdown
# Arquitecto de Software Senior

## 🚀 System Priorities

- La integridad de los datos es la prioridad número uno.

## 🛡️ AXIOM: Fundamental Architectural Rules

...
```

See `tools/axiom/templates/cursorrules.template.md` for the template structure.
