# Ejemplos de Configuración y Uso

Este documento contiene ejemplos prácticos de configuración y uso del Blast Radius Validator.

## 📝 Ejemplos de Configuración

### Configuración Básica

```json
{
  "max_files_without_approval": 10,
  "max_files_red_zone": 1,
  "max_files_yellow_zone": 3,
  "red_zone_paths": [
    "domain/presupuesto/",
    "domain/estimacion/"
  ],
  "yellow_zone_paths": [
    "infrastructure/persistence/"
  ],
  "override_keyword": "BIGBANG_APPROVED"
}
```

### Configuración Estricta (Desarrollo Temprano)

Para proyectos en desarrollo temprano donde se quiere control más estricto:

```json
{
  "max_files_without_approval": 5,
  "max_files_red_zone": 0,
  "max_files_yellow_zone": 1,
  "red_zone_paths": [
    "domain/presupuesto/",
    "domain/estimacion/",
    "domain/valueobjects/",
    "domain/entities/"
  ],
  "yellow_zone_paths": [
    "infrastructure/persistence/",
    "application/services/"
  ],
  "override_keyword": "BIGBANG_APPROVED"
}
```

### Configuración Permisiva (Proyecto Maduro)

Para proyectos maduros con más flexibilidad:

```json
{
  "max_files_without_approval": 20,
  "max_files_red_zone": 3,
  "max_files_yellow_zone": 5,
  "red_zone_paths": [
    "domain/presupuesto/",
    "domain/estimacion/"
  ],
  "yellow_zone_paths": [
    "infrastructure/persistence/"
  ],
  "override_keyword": "BIGBANG_APPROVED"
}
```

### Configuración Multi-Módulo

Para proyectos con múltiples módulos que requieren diferentes niveles de protección:

```json
{
  "max_files_without_approval": 15,
  "max_files_red_zone": 2,
  "max_files_yellow_zone": 4,
  "red_zone_paths": [
    "domain/presupuesto/",
    "domain/estimacion/",
    "domain/valueobjects/",
    "domain/entities/",
    "domain/shared/core/"
  ],
  "yellow_zone_paths": [
    "infrastructure/persistence/",
    "infrastructure/messaging/",
    "application/commands/",
    "application/queries/"
  ],
  "override_keyword": "BIGBANG_APPROVED"
}
```

## 🎯 Casos de Uso

### Caso 1: Commit Pequeño (Éxito)

**Escenario**: Modificas 3 archivos en utils.

```bash
$ git add utils/helper1.java utils/helper2.java utils/helper3.java
$ java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar .

=== Blast Radius Validation ===

Files staged: 3
  Red zone:   0
  Yellow zone: 0
  Green zone:  3

✓ Validation PASSED
  All limits respected
```

**Resultado**: ✅ Commit permitido

### Caso 2: Demasiados Archivos (Fallido)

**Escenario**: Intentas commitear 15 archivos.

```bash
$ git add file1.java file2.java ... file15.java
$ java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar .

=== Blast Radius Validation ===

Files staged: 15
  Red zone:   0
  Yellow zone: 0
  Green zone:  15

✗ Validation FAILED

Violations detected:

  • Total staged files (15) exceeds limit (10) without approval
    Files:
      - file1.java
      - file2.java
      ...
```

**Resultado**: ❌ Commit bloqueado

**Solución**: 
1. Dividir en commits más pequeños
2. Usar override keyword si es necesario

### Caso 3: Archivo en Red Zone (Fallido)

**Escenario**: Modificas 2 archivos en `domain/presupuesto/` (límite: 1).

```bash
$ git add domain/presupuesto/Budget.java domain/presupuesto/Calculator.java
$ java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar .

=== Blast Radius Validation ===

Files staged: 2
  Red zone:   2
  Yellow zone: 0
  Green zone:  0

✗ Validation FAILED

Violations detected:

  • Red zone files (2) exceed limit (1)
    Zone: RED
    Files:
      - domain/presupuesto/Budget.java
      - domain/presupuesto/Calculator.java
```

**Resultado**: ❌ Commit bloqueado

**Solución**: 
1. Commitear archivos uno por uno
2. Revisar si realmente necesitas modificar ambos
3. Usar override si es un cambio aprobado

### Caso 4: Override Keyword (Éxito)

**Escenario**: Refactorización grande aprobada.

```bash
$ git add -A
$ git commit -m "refactor: Major domain refactoring

BIGBANG_APPROVED

This refactoring has been reviewed and approved by the team."

=== Blast Radius Validation ===

Files staged: 25
  Red zone:   5
  Yellow zone: 8
  Green zone:  12

✓ Override keyword detected - all validations skipped

✓ Validation PASSED
```

**Resultado**: ✅ Commit permitido (override activo)

### Caso 5: Múltiples Violaciones

**Escenario**: Excedes todos los límites.

```bash
$ git add domain/presupuesto/* infrastructure/persistence/* utils/*
$ java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar .

=== Blast Radius Validation ===

Files staged: 20
  Red zone:   3
  Yellow zone: 5
  Green zone:  12

✗ Validation FAILED

Violations detected:

  • Total staged files (20) exceeds limit (10) without approval
    Files (showing first 10):
      - domain/presupuesto/Budget.java
      - domain/presupuesto/Calculator.java
      ...

  • Red zone files (3) exceed limit (1)
    Zone: RED
    Files:
      - domain/presupuesto/Budget.java
      - domain/presupuesto/Calculator.java
      - domain/presupuesto/Repository.java

  • Yellow zone files (5) exceed limit (3)
    Zone: YELLOW
    Files:
      - infrastructure/persistence/BudgetRepository.java
      - infrastructure/persistence/EstimateRepository.java
      ...
```

**Resultado**: ❌ Commit bloqueado (múltiples violaciones)

## 🔧 Integración con Scripts

### Script de Pre-commit Personalizado

```bash
#!/bin/bash
# Custom pre-commit hook

# Run blast radius validator
java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar . \
  --config .blast-radius-config.json

if [ $? -ne 0 ]; then
    echo "Blast radius validation failed. Commit aborted."
    exit 1
fi

# Run other validators...
```

### Makefile Integration

```makefile
.PHONY: validate-blast-radius
validate-blast-radius:
	@echo "Validating blast radius..."
	@java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar . \
		--config .blast-radius-config.json
	@echo "✓ Blast radius validation passed"

.PHONY: validate
validate: validate-blast-radius
	@echo "All validations passed"
```

## 📊 Interpretación de Resultados

### Exit Code 0 (Éxito)
- ✅ Todas las validaciones pasaron
- ✅ Puedes hacer commit
- ✅ No hay violaciones

### Exit Code 1 (Fallido)
- ❌ Se excedieron uno o más límites
- ❌ Commit bloqueado
- ⚠️ Revisa las violaciones listadas

### Exit Code 2 (Error)
- ❌ Error de configuración o Git
- ❌ Commit bloqueado
- ⚠️ Revisa los mensajes de error

## 💡 Mejores Prácticas

1. **Commits Pequeños**: Mantén commits pequeños y enfocados
2. **Revisa Antes de Commitear**: Usa `git status` y `git diff --cached` antes de commitear
3. **Usa Override con Cuidado**: El override keyword debe usarse solo cuando es necesario
4. **Configuración por Proyecto**: Ajusta los límites según las necesidades de tu proyecto
5. **Documenta Overrides**: Si usas override, explica por qué en el mensaje de commit

## 🎓 Casos Avanzados

### Validación Solo en CI

Si prefieres no usar hooks locales pero validar en CI:

```yaml
# .github/workflows/blast-radius.yml
- name: Validate blast radius
  run: |
    cd tools/blast-radius-validator
    ./mvnw package -DskipTests
    java -jar target/blast-radius-validator-1.0.0-SNAPSHOT.jar .
```

### Configuración por Rama

Usa diferentes configuraciones según la rama:

```bash
#!/bin/bash
if [ "$(git branch --show-current)" = "main" ]; then
    CONFIG=".blast-radius-strict.json"
else
    CONFIG=".blast-radius-relaxed.json"
fi

java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar . \
  --config "$CONFIG"
```
