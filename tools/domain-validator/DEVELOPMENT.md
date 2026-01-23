# Guía de Desarrollo - BudgetPro Domain Validator

Esta guía está dirigida a desarrolladores que quieren contribuir o entender la arquitectura interna del validador.

## 📋 Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Componentes Principales](#componentes-principales)
- [Flujo de Validación](#flujo-de-validación)
- [Agregar Nuevas Reglas de Validación](#agregar-nuevas-reglas-de-validación)
- [Testing](#testing)
- [Debugging](#debugging)

## Arquitectura

El validador sigue una arquitectura hexagonal (puertos y adaptadores) consistente con BudgetPro:

```
┌─────────────────────────────────────────────────────────┐
│                    CLI Layer (Picocli)                    │
│                  DomainValidator.java                     │
└──────────────────────┬────────────────────────────────────┘
                       │
┌──────────────────────▼────────────────────────────────────┐
│              Application Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ RoadmapLoader│  │CodebaseAnalyzer│ │ValidationEngine│ │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└──────────────────────┬────────────────────────────────────┘
                       │
┌──────────────────────▼────────────────────────────────────┐
│              Domain Layer                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Validators  │  │   Detectors   │  │   Generators │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└───────────────────────────────────────────────────────────┘
```

### Principios de Diseño

1. **Separación de Responsabilidades**: Cada componente tiene una responsabilidad única
2. **Inmutabilidad**: Los modelos de datos son inmutables cuando es posible
3. **Testabilidad**: Todos los componentes son fácilmente testeables
4. **Extensibilidad**: Fácil agregar nuevos detectores, validadores o generadores

## Estructura del Proyecto

```
tools/domain-validator/
├── src/main/java/com/budgetpro/validator/
│   ├── DomainValidator.java          # Entry point CLI
│   ├── model/                         # Modelos de dominio
│   │   ├── ValidationResult.java      # Resultado de validación
│   │   ├── Violation.java             # Violación detectada
│   │   ├── ModuleStatus.java          # Estado de módulo
│   │   └── enums/                     # Enumeraciones
│   ├── roadmap/                       # Roadmap canónico
│   │   ├── RoadmapLoader.java         # Carga desde JSON
│   │   ├── CanonicalRoadmap.java      # Roadmap completo
│   │   ├── ModuleDefinition.java      # Definición de módulo
│   │   └── DependencyConstraint.java # Constraint de dependencia
│   ├── analyzer/                      # Análisis de código
│   │   ├── CodebaseAnalyzer.java      # Orquestador
│   │   ├── EntityDetector.java        # Detecta entidades
│   │   ├── ServiceDetector.java       # Detecta servicios
│   │   ├── ApiDetector.java           # Detecta endpoints REST
│   │   ├── StateMachineDetector.java  # Detecta state machines
│   │   └── IntegrationPointDetector.java # Detecta puntos de integración
│   ├── engine/                        # Motor de validación
│   │   ├── ValidationEngine.java      # Orquestador
│   │   ├── DependencyValidator.java    # Valida dependencias
│   │   ├── ConstraintValidator.java    # Valida constraints
│   │   ├── ValidationRuleExecutor.java # Ejecuta reglas
│   │   └── ViolationBuilder.java      # Construye violaciones
│   └── output/                        # Generadores de salida
│       ├── MermaidGenerator.java       # Genera diagramas Mermaid
│       ├── MarkdownGenerator.java      # Genera Markdown GSOT
│       ├── JsonReportGenerator.java    # Genera reportes JSON
│       └── DiagramStyler.java         # Estilos para diagramas
├── src/main/resources/
│   └── canonical-roadmap.json         # Roadmap canónico
├── src/test/java/                     # Tests unitarios e integración
└── pom.xml                            # Configuración Maven
```

## Componentes Principales

### RoadmapLoader

Carga el roadmap canónico desde `canonical-roadmap.json` y valida su estructura.

**Uso:**
```java
RoadmapLoader loader = new RoadmapLoader();
CanonicalRoadmap roadmap = loader.load();
```

**Responsabilidades:**
- Parsear JSON usando Jackson
- Validar estructura del roadmap
- Lanzar excepciones descriptivas si hay errores

### CodebaseAnalyzer

Analiza el código fuente del repositorio para detectar módulos implementados.

**Uso:**
```java
CodebaseAnalyzer analyzer = new CodebaseAnalyzer();
List<ModuleStatus> statuses = analyzer.analyze(repositoryPath, roadmap);
```

**Responsabilidades:**
- Escanear directorios Java
- Usar detectores especializados para encontrar elementos
- Inferir estado de implementación de cada módulo

**Detectores:**
- `EntityDetector`: Detecta entidades y aggregate roots
- `ServiceDetector`: Detecta servicios y use cases
- `ApiDetector`: Detecta endpoints REST
- `StateMachineDetector`: Detecta enums de estado
- `IntegrationPointDetector`: Detecta repositorios y adapters

### ValidationEngine

Orquesta la validación completa comparando código contra roadmap.

**Uso:**
```java
ValidationEngine engine = new ValidationEngine();
ValidationResult result = engine.validate(repositoryPath);
```

**Flujo:**
1. Carga roadmap canónico
2. Analiza código fuente
3. Ejecuta validadores:
   - `ValidationRuleExecutor`: Ejecuta reglas de validación
   - `DependencyValidator`: Valida dependencias entre módulos
   - `ConstraintValidator`: Valida constraints (temporal coupling, etc.)
4. Agrega violaciones al resultado
5. Determina estado final (PASSED, WARNINGS, CRITICAL_VIOLATIONS, ERROR)

### Output Generators

Generan diferentes formatos de salida:

- **MermaidGenerator**: Diagramas de dependencias en formato Mermaid
- **MarkdownGenerator**: Documento GSOT (Golden Source of Truth) en Markdown
- **JsonReportGenerator**: Reportes JSON para consumo automatizado

## Flujo de Validación

```
1. CLI recibe comando `validate`
   ↓
2. RoadmapLoader carga canonical-roadmap.json
   ↓
3. CodebaseAnalyzer escanea código fuente
   ├── EntityDetector encuentra entidades
   ├── ServiceDetector encuentra servicios
   ├── ApiDetector encuentra endpoints
   ├── StateMachineDetector encuentra state machines
   └── IntegrationPointDetector encuentra repositorios
   ↓
4. ValidationEngine ejecuta validación
   ├── ValidationRuleExecutor ejecuta reglas
   ├── DependencyValidator valida dependencias
   ├── ConstraintValidator valida constraints
   └── ViolationBuilder crea violaciones
   ↓
5. ValidationResult contiene violaciones y estados
   ↓
6. Output Generator genera formato solicitado
   ↓
7. CLI retorna exit code apropiado
```

## Agregar Nuevas Reglas de Validación

### Paso 1: Definir Regla en Roadmap

Agrega la regla en `canonical-roadmap.json`:

```json
{
  "validation_rules": [
    {
      "type": "entity_exists",
      "target": "MiNuevaEntidad",
      "required": true
    }
  ]
}
```

### Paso 2: Implementar Ejecución

En `ValidationRuleExecutor.java`, agrega el caso:

```java
case "entity_exists":
    return validateEntityExists(rule, moduleStatus);
```

### Paso 3: Agregar Test

Crea test en `ValidationRuleExecutorTest.java`:

```java
@Test
void deberiaValidarNuevaRegla() {
    // Test implementation
}
```

## Testing

### Ejecutar Tests

```bash
cd tools/domain-validator
mvn test
```

### Estructura de Tests

- **Unit Tests**: Tests de componentes individuales
- **Integration Tests**: Tests end-to-end del flujo completo
- **Baseline Tests**: Tests específicos del principio de baseline

### Ejemplos de Tests

```java
@Test
void deberiaDetectarEntidad() {
    EntityDetector detector = new EntityDetector();
    List<String> entities = detector.detect(repositoryPath);
    assertTrue(entities.contains("Presupuesto"));
}

@Test
void deberiaDetectarViolacionCritica() {
    ValidationEngine engine = new ValidationEngine();
    ValidationResult result = engine.validate(repositoryPath);
    assertTrue(result.hasCriticalViolations());
}
```

## Debugging

### Habilitar Logging

El validador usa `System.out.println` para logging básico. Para debugging más detallado:

```java
// En DomainValidator.java
System.setProperty("java.util.logging.config.file", "logging.properties");
```

### Verificar Roadmap

```bash
# Validar estructura JSON
cat src/main/resources/canonical-roadmap.json | jq .

# Verificar módulo específico
cat src/main/resources/canonical-roadmap.json | jq '.roadmap.modules[] | select(.id == "presupuesto")'
```

### Debugging de Análisis

Para ver qué detecta el analizador:

```java
CodebaseAnalyzer analyzer = new CodebaseAnalyzer();
List<ModuleStatus> statuses = analyzer.analyze(repositoryPath, roadmap);
statuses.forEach(status -> {
    System.out.println("Module: " + status.getModuleId());
    System.out.println("  Entities: " + status.getDetectedEntities());
    System.out.println("  Services: " + status.getDetectedServices());
});
```

### Debugging de Validación

Para ver violaciones detalladas:

```java
ValidationResult result = engine.validate(repositoryPath);
result.getViolations().forEach(v -> {
    System.out.println("Violation: " + v.getMessage());
    System.out.println("  Module: " + v.getModuleId());
    System.out.println("  Severity: " + v.getSeverity());
    System.out.println("  Context: " + v.getContext());
});
```

## Mejores Prácticas

### 1. Inmutabilidad

Los modelos de datos deben ser inmutables cuando sea posible:

```java
public record Violation(
    String moduleId,
    ViolationSeverity severity,
    String message
) {}
```

### 2. Manejo de Errores

Usa excepciones específicas:

```java
public static class RoadmapLoadException extends Exception {
    public RoadmapLoadException(String message) {
        super(message);
    }
}
```

### 3. Testing

- Un test por escenario
- Tests descriptivos con nombres claros
- Usa `@TempDir` para tests de archivos

### 4. Documentación

- JavaDoc para clases públicas
- Comentarios para lógica compleja
- README actualizado

## Contribuir

1. Fork el repositorio
2. Crea branch para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Implementa cambios con tests
4. Ejecuta tests (`mvn test`)
5. Actualiza documentación
6. Crea Pull Request

## Referencias

- [Picocli Documentation](https://picocli.info/)
- [JavaParser Documentation](https://javaparser.org/)
- [Jackson Documentation](https://github.com/FasterXML/jackson)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
