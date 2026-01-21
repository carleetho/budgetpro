# BudgetPro Domain Validator

CLI tool para validar el orden de desarrollo de módulos BudgetPro contra el roadmap canónico del dominio.

## Construcción

```bash
cd tools/domain-validator
mvn clean package
```

Esto generará un JAR ejecutable en `target/domain-validator-1.0.0-SNAPSHOT.jar`.

## Uso

### Ver ayuda general

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar --help
```

### Validar código actual

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --repo-path ./backend
```

### Generar roadmap canónico

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar generate-roadmap --output-dir ./docs/roadmap
```

### Verificar módulo específico

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar check-module presupuesto --show-dependencies
```

## Exit Codes

- `0`: Validación pasada sin violaciones
- `1`: Violaciones críticas detectadas (bloquea CI/CD)
- `2`: Advertencias detectadas (requiere revisión)
- `3`: Error durante el análisis (estructura inválida)

## Arquitectura

El validador sigue una arquitectura hexagonal consistente con BudgetPro:

- **Model**: Clases de datos (`ValidationResult`, `Violation`, `ModuleStatus`)
- **CLI**: Interfaz de línea de comandos usando Picocli
- **Validators**: Lógica de validación (a implementar en tareas futuras)
- **Generators**: Generadores de salida (Mermaid, Markdown, JSON) (a implementar en tareas futuras)

## Estado de Implementación

### ✅ Completado (Task 1)

- Estructura del proyecto Maven
- Modelos de datos completos
- CLI framework con Picocli
- Comandos básicos (validate, generate-roadmap, check-module)
- Lógica de exit codes

### ⏳ Pendiente (Tareas Futuras)

- Análisis real del código Java
- Generación de roadmap canónico
- Validación de dependencias
- Generadores de salida (Mermaid, Markdown, JSON)
- Integración CI/CD

## Dependencias

- **Picocli 4.7.5**: Framework CLI ligero y basado en anotaciones
- **Jackson 2.15.2**: Procesamiento JSON
- **Java 17**: Versión mínima requerida
