# AXIOM Configuration Guide for AI Integration

This guide details the configuration sections in `axiom.config.yaml` specifically designed for AI Assistant integration (REQ-27). These settings control how AXIOM generates the `.cursorrules` file to guide AI agents like Cursor, Windsurf, and Antigravity.

## Structure Overview

```yaml
system:
  # Identity and priorities of the AI agent
axioms:
  # Architectural rules, limits, and boundaries
history:
  # Institutional memory (lessons learned)
```

## 1. System Identity (`system`)

Defines the persona and high-level goals of the AI assistant.

```yaml
system:
  role: "Senior Java Architect"
  priorities:
    - "Maintain strict hexagonal architecture boundaries."
    - "Prioritize readability over brevity."
```

- **role**: The persona the AI should adopt.
- **priorities**: An ordered list of "North Star" principles the AI must follow.

## 2. Axioms (`axioms`)

The core architectural laws that the AI must respect.

### Protection Zones (`protection_zones`)

Defines file modification limits per commit to control blast radius.

```yaml
axioms:
  protection_zones:
    red:
      - path: "domain/core"
        max_files: 1 # High risk, focus on atomic changes
    green:
      - path: "tests"
        max_files: 10 # Low risk, allow batch updates
```

### Prohibitions (`prohibitions`)

Explicit "DO NOT" rules with reasoning and severity.

```yaml
axioms:
  prohibitions:
    - rule: "Never use System.out.println"
      reason: "Use SLF4J logging instead for proper log management."
      severity: "BLOCKING"
```

### Hexagonal Boundaries (`hexagonal_boundaries`)

Strict import/dependency rules to enforce architecture.

```yaml
axioms:
  hexagonal_boundaries:
    permitted:
      - "Application -> Domain"
      - "Infrastructure -> Application"
    forbidden:
      - "Domain -> Infrastructure" # The dependency rule!
```

### Override Keywords (`override_keywords`)

Documented keywords that allow bypassing specific rules.

```yaml
axioms:
  override_keywords:
    - keyword: "BIGBANG_APPROVED"
      context: "Authorized large-scale refactors only."
```

## 3. Historical Context (`history`)

Teaches the AI about past failures to prevent optimized mistakes.

```yaml
history:
  incidents:
    - "2023-10: Production outage due to missing WHERE clause in corrective script."
  lessons_learned:
    - "Always verify JPA query parameters names match entity fields."
```

## Full Example

See [tools/axiom/axiom.config.yaml](../axiom.config.yaml) for the complete reference configuration.
