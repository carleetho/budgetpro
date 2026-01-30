# BudgetPro Naming Validator

Herramienta CLI para validar convenciones de nombres en el proyecto BudgetPro. Esta herramienta asegura que las clases sigan los patrones de nomenclatura definidos, especialmente en la arquitectura hexagonal y DDD.

## Requisitos

- Java 17
- Maven (incluido vía wrapper)

## Construcción

Para generar el archivo JAR ejecutable:

```bash
./mvnw clean package -DskipTests
```

El artefacto se generará en `target/naming-validator-1.0.0-SNAPSHOT.jar`.

## Uso

Ejecutar la herramienta pasando la ruta del directorio de fuentes:

```bash
java -jar target/naming-validator-1.0.0-SNAPSHOT.jar <ruta-al-codigo-fuente>
```

Ejemplo:

```bash
java -jar target/naming-validator-1.0.0-SNAPSHOT.jar ../../src/main/java
```

## Salida y Códigos de Retorno

- **0**: Éxito. No se encontraron violaciones bloqueantes.
- **1**: Error. Se encontraron una o más violaciones bloqueantes (`BLOCKING`).

Los mensajes de error y sugerencias están en español para facilitar la corrección por parte del equipo.
