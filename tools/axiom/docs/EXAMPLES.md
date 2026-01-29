# Ejemplos de Configuración AXIOM

## 1. Configuración Básica (BudgetPro)

Esta es la configuración estándar recomendada para proyectos con Arquitectura Hexagonal y Java/Spring.

```yaml
system:
  role: "Arquitecto Java Senior (Spring Boot 3, Hexagonal)"
  priorities:
    - "Integridad de los datos"
    - "Mantenibilidad a largo plazo"

axioms:
  protection_zones:
    - name: "Dominio"
      level: "RED"
      paths: ["src/main/java/com/budgetpro/domain/**"]
      description: "Lógica de negocio pura. Sin dependencias externas."

    - name: "Infraestructura"
      level: "GREEN"
      paths: ["src/main/java/com/budgetpro/infrastructure/**"]
      description: "Implementación técnica. Modificable."

  prohibitions:
    - "No usar @Autowired en entidades de dominio"
    - "No eliminar código sin entender su propósito"

  hexagonal_boundaries:
    permitted:
      - from: "infrastructure"
        to: "domain"
    forbidden:
      - from: "domain"
        to: "infrastructure"

  override_keywords:
    HOTFIX: "Parche rápido para producción"
    LEGACY_REFACTOR: "Permiso para tocar código antiguo"

  historical_context:
    lessons_learned:
      - "Siempre usar DTOs en los controladores"
```

## 2. Configuración Estricta (Legacy Rescue)

Para proyectos donde se quiere "congelar" el código existente y solo permitir cambios muy controlados.

```yaml
system:
  role: "Guardián de Código Legacy"
  priorities:
    - "No romper funcionalidad existente"
    - "Minimizar cambios"

axioms:
  protection_zones:
    - name: "Legacy Core"
      level: "RED"
      paths: ["src/**"] # Todo en rojo por defecto
      description: "Código antiguo muy frágil."

    - name: "Tests"
      level: "GREEN"
      paths: ["src/test/**"]
      description: "La única zona segura para cambios masivos."

  prohibitions:
    - "NO refactorizar lógica sin cobertura de tests del 100%"
```

## 3. Resultado Generado (.cursorrules)

El ejemplo #1 generará un archivo `.cursorrules` similar a este:

```markdown
# BUDGETPRO - REGLAS MAESTRAS DE COMPORTAMIENTO PARA LA IA

# 1. ROL Y PERSONALIDAD

- Actúa como: Arquitecto Java Senior (Spring Boot 3, Hexagonal)
- TUS PRIORIDADES SON:
  - Integridad de los datos
  - Mantenibilidad a largo plazo

# 2. PROHIBICIONES ESTRICTAS

- ⛔ No usar @Autowired en entidades de dominio
- ...

# 3. ZONAS DE PROTECCIÓN

### Dominio [NIVEL: RED]

- Archivos: `src/main/java/com/budgetpro/domain/**`
- REGLA: NO MODIFICAR sin autorización explícita...
  ...
```
