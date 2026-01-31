# 🏷️ BudgetPro Naming Validator

Herramienta de línea de comandos (CLI) para validar las convenciones de nomenclatura en el proyecto BudgetPro, asegurando la consistencia entre capas arquitectónicas (DDA/Hexagonal).

## 🚀 Características

- **Detección Automática de Capas**: Identifica si una clase pertenece al Dominio, Infraestructura o Aplicación basándose en su ruta y nombre.
- **Validación de Reglas**:
  - **Entidades de Dominio**: No deben tener sufijos técnicos (ej. `User` ✅, `UserEntity` ❌).
  - **Entidades JPA**: Deben terminar en `JpaEntity`.
  - **Mappers**: Deben terminar en `Mapper`.
  - **Value Objects**: No deben tener sufijos como `VO` o `ValueObject`.
  - **Servicios de Dominio**: Deben terminar en `Service`.
- **Configuración Externa**: Soporte completo para personalizar reglas, sufijos y severidades mediante YAML.
- **Integración CI**: Devuelve códigos de salida (0 éxito, 1 fallo) para integrarse en pipelines de integración continua.

## 🛠️ Instalación y Uso

### Requisitos

- Java 17 o superior.
- Maven.

### Construcción

```bash
mvn clean package
```

### Ejecución

```bash
java -jar target/naming-validator-1.0.0-SNAPSHOT.jar <ruta-al-codigo>
```

### Opciones

- `-c, --config <file>`: Especifica un archivo de configuración YAML personalizado.
- `-h, --help`: Muestra la ayuda.

## ⚙️ Configuración (naming-config.yaml)

```yaml
layers:
  DOMAIN_ENTITY:
    pathPatterns: ["/domain/"]
    classNamePatterns: ["/entities/", "/model/"]
  JPA_ENTITY:
    pathPatterns: ["/infrastructure/persistence/entity/"]

rules:
  DOMAIN_ENTITY:
    enabled: true
    severity: BLOCKING
    forbiddenSuffixes: ["Entity", "JpaEntity"]
  JPA_ENTITY:
    enabled: true
    expectedSuffix: "JpaEntity"
    severity: BLOCKING

exclusions:
  - "**/Legacy*"
  - "**/Test*"
```

## 🧪 Pruebas

```bash
mvn test
```

Actualmente cuenta con una suite de 26 pruebas unitarias que cubren todas las reglas y el motor de detección.
