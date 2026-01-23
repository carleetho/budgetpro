# BudgetPro Domain Validator

CLI tool para validar el orden de desarrollo de módulos BudgetPro contra el roadmap canónico del dominio.

## 📋 Tabla de Contenidos

- [Instalación](#instalación)
- [Uso Básico](#uso-básico)
- [Comandos](#comandos)
- [Exit Codes](#exit-codes)
- [Integración CI/CD](#integración-cicd)
- [Ejemplos](#ejemplos)
- [Troubleshooting](#troubleshooting)
- [Arquitectura](#arquitectura)

## Instalación

### Requisitos

- Java 17 o superior
- Maven 3.6+

### Construcción

```bash
cd tools/domain-validator
mvn clean package
```

Esto generará un JAR ejecutable en `target/domain-validator-1.0.0-SNAPSHOT.jar`.

## Uso Básico

### Ver ayuda general

```bash
java -jar target/domain-validator-1.0.0-SNAPSHOT.jar --help
```

### Validar código actual

```bash
java -jar target/domain-validator-1.0.0-SNAPSHOT.jar validate --repo-path ../../backend
```

### Generar roadmap canónico

```bash
java -jar target/domain-validator-1.0.0-SNAPSHOT.jar generate-roadmap --output-dir ../../docs/context
```

## Comandos

### `validate`

Valida el código fuente contra el roadmap canónico.

**Sintaxis:**
```bash
validate [--repo-path <path>] [--strict] [--output-format <format>] [--output-file <file>]
```

**Opciones:**
- `--repo-path`: Ruta al directorio del repositorio (default: `./backend`)
- `--strict`: Modo estricto - las advertencias también bloquean (default: `false`)
- `--output-format`: Formato de salida (`text`, `json`) (default: `text`)
- `--output-file`: Archivo de salida (solo para JSON) (default: `stdout`)

**Ejemplos:**

```bash
# Validación básica
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate

# Validación con modo estricto
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --strict

# Generar reporte JSON
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --output-format json --output-file validation-report.json

# Validar ruta específica
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --repo-path /path/to/backend
```

### `generate-roadmap`

Genera visualizaciones del roadmap canónico.

**Sintaxis:**
```bash
generate-roadmap [--output-dir <dir>] [--format <format>]
```

**Opciones:**
- `--output-dir`: Directorio de salida (default: `./docs/context`)
- `--format`: Formato de salida (`mermaid`, `markdown`, `all`) (default: `all`)

**Ejemplos:**

```bash
# Generar todos los formatos
java -jar domain-validator-1.0.0-SNAPSHOT.jar generate-roadmap

# Solo diagrama Mermaid
java -jar domain-validator-1.0.0-SNAPSHOT.jar generate-roadmap --format mermaid

# Solo documento Markdown
java -jar domain-validator-1.0.0-SNAPSHOT.jar generate-roadmap --format markdown
```

### `check-module`

Verifica el estado de un módulo específico.

**Sintaxis:**
```bash
check-module <module-id> [--show-dependencies]
```

**Opciones:**
- `--show-dependencies`: Muestra dependencias y módulos habilitados

**Ejemplos:**

```bash
# Verificar módulo Presupuesto
java -jar domain-validator-1.0.0-SNAPSHOT.jar check-module presupuesto

# Con dependencias
java -jar domain-validator-1.0.0-SNAPSHOT.jar check-module compras --show-dependencies
```

## Exit Codes

El validador retorna códigos de salida estándar:

- **`0`**: Validación pasada sin violaciones
- **`1`**: Violaciones críticas detectadas (bloquea CI/CD)
- **`2`**: Advertencias detectadas (requiere revisión)
- **`3`**: Error durante el análisis (estructura inválida)

**Uso en scripts:**

```bash
#!/bin/bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --strict

EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Validación pasada"
elif [ $EXIT_CODE -eq 1 ]; then
    echo "❌ Violaciones críticas detectadas"
    exit 1
elif [ $EXIT_CODE -eq 2 ]; then
    echo "⚠️ Advertencias detectadas"
    exit 2
else
    echo "💥 Error durante la validación"
    exit 3
fi
```

## Integración CI/CD

El validador está integrado automáticamente en GitHub Actions. El workflow se ejecuta en cada pull request que afecta `backend/src/**`.

### Configuración Manual

Si necesitas ejecutar el validador manualmente en CI/CD:

```yaml
# .github/workflows/validate.yml
- name: Run domain validator
  run: |
    cd tools/domain-validator
    mvn clean package -DskipTests
    java -jar target/domain-validator-1.0.0-SNAPSHOT.jar \
      validate \
      --repo-path ../../backend \
      --strict \
      --output-format json \
      --output-file validation-report.json
```

### Pre-commit Hook

Para validar antes de cada commit:

```bash
#!/bin/bash
# .git/hooks/pre-commit
cd tools/domain-validator
mvn clean package -DskipTests -q
java -jar target/domain-validator-1.0.0-SNAPSHOT.jar validate --strict
```

## Ejemplos

### Ejemplo 1: Validación Exitosa

```bash
$ java -jar domain-validator-1.0.0-SNAPSHOT.jar validate

Validating repository: /path/to/backend
Strict mode: false

✅ Validation completed: PASSED
Total violations: 0
```

### Ejemplo 2: Violación Crítica

```bash
$ java -jar domain-validator-1.0.0-SNAPSHOT.jar validate

❌ Validation completed: CRITICAL_VIOLATIONS
Total violations: 2

🔴 Critical Violations (1):
  Module: compras
  Type: STATE_DEPENDENCY
  Message: Presupuesto freeze mechanism missing
  Suggestion: Implement PresupuestoService.congelar() method before developing Compras module
```

### Ejemplo 3: Reporte JSON

```bash
$ java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --output-format json --output-file report.json

Generated JSON report: report.json
```

```json
{
  "validation_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-21T12:00:00Z",
  "repository_path": "/path/to/backend",
  "canonical_version": "1.0.0",
  "status": "CRITICAL_VIOLATIONS",
  "violations": [
    {
      "module_id": "compras",
      "severity": "CRITICAL",
      "type": "STATE_DEPENDENCY",
      "message": "Presupuesto freeze mechanism missing",
      "suggestion": "Implement PresupuestoService.congelar() method",
      "blocking": true
    }
  ],
  "module_statuses": [...]
}
```

## Troubleshooting

### Error: "Roadmap resource not found"

**Problema:** El validador no puede encontrar `canonical-roadmap.json`.

**Solución:**
```bash
# Verificar que el archivo existe
ls -la tools/domain-validator/src/main/resources/canonical-roadmap.json

# Reconstruir el proyecto
cd tools/domain-validator
mvn clean package
```

### Error: "No se encontraron archivos Java en el directorio"

**Problema:** El validador no encuentra código fuente Java.

**Solución:**
```bash
# Verificar la ruta del repositorio
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --repo-path /ruta/correcta/backend

# Verificar estructura de directorios
ls -R backend/src/main/java/com/budgetpro/domain/
```

### Violaciones inesperadas

**Problema:** El validador detecta violaciones que no esperabas.

**Solución:**
1. Revisa el reporte JSON para detalles completos
2. Verifica que las dependencias del módulo están completas
3. Consulta `docs/context/ROADMAP_CANONICO.md` para el orden correcto
4. Ejecuta `check-module` para ver dependencias específicas

### El workflow de CI/CD no se ejecuta

**Problema:** GitHub Actions no ejecuta el workflow.

**Solución:**
1. Verifica que el archivo `.github/workflows/validate-roadmap.yml` existe
2. Verifica que el PR afecta `backend/src/**`
3. Revisa los logs del workflow en GitHub Actions

## Arquitectura

El validador sigue una arquitectura hexagonal consistente con BudgetPro:

```
domain-validator/
├── src/main/java/com/budgetpro/validator/
│   ├── DomainValidator.java          # CLI entry point
│   ├── model/                        # Modelos de datos
│   │   ├── ValidationResult.java
│   │   ├── Violation.java
│   │   └── ModuleStatus.java
│   ├── roadmap/                      # Carga del roadmap canónico
│   │   ├── RoadmapLoader.java
│   │   ├── ModuleDefinition.java
│   │   └── CanonicalRoadmap.java
│   ├── analyzer/                     # Análisis de código
│   │   ├── CodebaseAnalyzer.java
│   │   ├── EntityDetector.java
│   │   └── ServiceDetector.java
│   ├── engine/                       # Motor de validación
│   │   ├── ValidationEngine.java
│   │   ├── DependencyValidator.java
│   │   └── ConstraintValidator.java
│   └── output/                       # Generadores de salida
│       ├── MermaidGenerator.java
│       ├── MarkdownGenerator.java
│       └── JsonReportGenerator.java
└── src/main/resources/
    └── canonical-roadmap.json        # Roadmap canónico
```

### Componentes Principales

- **DomainValidator**: CLI usando Picocli
- **RoadmapLoader**: Carga y valida el roadmap canónico desde JSON
- **CodebaseAnalyzer**: Analiza código fuente usando JavaParser
- **ValidationEngine**: Orquesta la validación completa
- **Output Generators**: Generan Mermaid, Markdown y JSON

## Dependencias

- **Picocli 4.7.5**: Framework CLI ligero y basado en anotaciones
- **Jackson 2.15.2**: Procesamiento JSON
- **JavaParser 3.25.4**: Análisis estático de código Java
- **JUnit 5.10.0**: Framework de testing
- **Java 17**: Versión mínima requerida

## Estado de Implementación

### ✅ Completado

- ✅ Estructura del proyecto Maven
- ✅ Modelos de datos completos
- ✅ CLI framework con Picocli
- ✅ Comandos básicos (validate, generate-roadmap, check-module)
- ✅ Análisis de código fuente
- ✅ Validación de dependencias y constraints
- ✅ Generadores de salida (Mermaid, Markdown, JSON)
- ✅ Integración CI/CD con GitHub Actions
- ✅ Suite de tests completa

## Contribuir

Ver [DEVELOPMENT.md](DEVELOPMENT.md) para guía de desarrollo y contribución.

## 📊 Análisis de Reportes

### Script de Análisis Automatizado

El script `analyze-report.sh` proporciona un análisis detallado del reporte JSON:

```bash
# Generar reporte JSON
./validate.sh --format json --output report.json

# Analizar el reporte
./analyze-report.sh report.json
```

**El script genera**:
- 📋 Resumen ejecutivo con métricas clave
- 🔴 Violaciones críticas agrupadas por módulo
- 📊 Violaciones clasificadas por tipo
- 🔧 Identificación automática de falsos positivos
- 📈 Estado detallado de cada módulo

### Planes Estratégicos

- **PLAN_ESTRATEGICO_VALIDACION.md**: Plan completo a largo plazo con todas las fases
- **PLAN_ACCION_INMEDIATO.md**: Acciones prioritarias para el sprint actual
- **RESUMEN_ANALISIS_REPORTE.md**: Resumen ejecutivo del análisis más reciente

### Flujo de Trabajo Recomendado

```bash
# 1. Ejecutar validación
./validate.sh --format json --output report.json

# 2. Analizar resultados
./analyze-report.sh report.json

# 3. Revisar plan estratégico
cat PLAN_ACCION_INMEDIATO.md

# 4. Trabajar en correcciones según prioridad

# 5. Re-validar para medir progreso
./validate.sh --format json --output report-nuevo.json
```

## Referencias

- [ROADMAP_CANONICO.md](../../docs/context/ROADMAP_CANONICO.md) - Roadmap canónico del dominio
- [DEVELOPMENT.md](DEVELOPMENT.md) - Guía para desarrolladores
- [PLAN_ESTRATEGICO_VALIDACION.md](PLAN_ESTRATEGICO_VALIDACION.md) - Plan estratégico completo
- [PLAN_ACCION_INMEDIATO.md](PLAN_ACCION_INMEDIATO.md) - Plan de acción inmediato