# {{ system.role }}

## 🚀 System Priorities

{% for priority in system.priorities %}

- {{ priority }}
  {% endfor %}

## 🛡️ AXIOM: Fundamental Architectural Rules

### 1. Protection Zones

Limits on how many files can be touched per commit.

{% for zone, rules in axioms.protection_zones.items() %}
**{{ zone|upper }} ZONE**:
{% for rule in rules %}

- `{{ rule.path }}` (Max {{ rule.max_files }} files)
  {% endfor %}
  {% endfor %}

### 2. Absolute Prohibitions

NEVER violate these rules unless an override is authorized.

{% for prohib in axioms.prohibitions %}

- **NEVER**: {{ prohib.rule }}
  - _Reason_: {{ prohib.reason }}
    (Severity: {{ prohib.severity }})
    {% endfor %}

### 3. Hexagonal Boundaries

Strict control of dependency direction.

**✅ Permitted:**
{% for perm in axioms.hexagonal_boundaries.permitted %}

- {{ perm }}
  {% endfor %}

**❌ Forbidden:**
{% for forb in axioms.hexagonal_boundaries.forbidden %}

- {{ forb }}
  {% endfor %}

## 🔓 Override Keywords

Use these keywords in commit messages to bypass specific checks.

{% for ov in axioms.override_keywords %}

- `{{ ov.keyword }}`: {{ ov.context }}
  {% endfor %}

## 📋 Operation Protocol

Before making changes, verify:

1.  **Understand the Goal**: Read the PR description or task.
2.  **Check Constraints**: Review Protection Zones and Prohibitions below.
3.  **Plan**: If in a Red Zone, ensure you have a plan.
4.  **Validate**: Run `./axiom.sh` before committing.

## 📜 Historical Context & Lessons Learned

Remember these incidents to avoid repeating past mistakes.

### Lessons Learned

{% for lesson in history.lessons_learned %}

- {{ lesson }}
  {% endfor %}

### Known Incidents

{% for incident in history.incidents %}

- {{ incident }}
  {% endfor %}
