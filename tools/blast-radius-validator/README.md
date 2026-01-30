# Blast Radius Validator

Herramienta CLI para validar el "blast radius" (alcance de impacto) de cambios en módulos BudgetPro. Previene commits que modifican demasiados archivos o que tocan áreas críticas sin aprobación explícita.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Instalación](#instalación)
- [Uso](#uso)
- [Configuración](#configuración)
- [Git Hooks](#git-hooks)
- [Override Keyword](#override-keyword)
- [CI/CD](#cicd)
- [Troubleshooting](#troubleshooting)

## ✨ Características

- **Validación de límites**: Controla el número máximo de archivos staged
- **Zonas de protección**: Clasifica archivos en zonas roja, amarilla y verde según su criticidad
- **Integración Git**: Detecta automáticamente archivos staged y mensaje de commit
- **Override keyword**: Permite bypass con palabra clave en commit message
- **Salida formateada**: Mensajes claros con colores (opcional)
- **Códigos de salida**: Compatible con CI/CD (0=éxito, 1=fallo, 2=error)

## 🚀 Instalación

### Prerrequisitos

- Java 17 o superior
- Maven (incluido via Maven Wrapper)
- Git

### Construcción

```bash
cd tools/blast-radius-validator
./mvnw clean package
```

El JAR ejecutable se generará en `target/blast-radius-validator-1.0.0-SNAPSHOT.jar`.

## 💻 Uso

### Uso Básico

```bash
# Validar desde el directorio actual
java -jar target/blast-radius-validator-1.0.0-SNAPSHOT.jar .

# Validar un repositorio específico
java -jar target/blast-radius-validator-1.0.0-SNAPSHOT.jar /path/to/repo

# Usar configuración personalizada
java -jar target/blast-radius-validator-1.0.0-SNAPSHOT.jar . --config custom-config.json
```

### Opciones

```
Usage: blast-radius-validator [-hV] [--no-colors] [-c=<configPath>]
                              <repositoryPath>
Valida el blast radius de cambios staged en el repositorio Git
      <repositoryPath>   Ruta al directorio del repositorio Git (default: .)
  -c, --config=<configPath>
                         Ruta al archivo de configuración JSON (opcional, usa
                           defaults si no se especifica)
  -h, --help             Show this help message and exit.
      --no-colors        Deshabilitar colores en la salida
  -V, --version          Print version information and exit.
```

### Ejemplo de Salida

**Validación Exitosa:**
```
=== Blast Radius Validation ===

Files staged: 5
  Red zone:   0
  Yellow zone: 2
  Green zone:  3

✓ Validation PASSED
  All limits respected
```

**Validación Fallida:**
```
=== Blast Radius Validation ===

Files staged: 15
  Red zone:   2
  Yellow zone: 3
  Green zone:  10

✗ Validation FAILED

Violations detected:

  • Total staged files (15) exceeds limit (10) without approval
    Files:
      - domain/presupuesto/Budget.java
      - domain/presupuesto/Calculator.java
      ...

  • Red zone files (2) exceed limit (1)
    Zone: RED
    Files:
      - domain/presupuesto/Budget.java
      - domain/estimacion/Estimate.java
```

## ⚙️ Configuración

### Archivo de Configuración JSON

El validador acepta un archivo JSON con la siguiente estructura:

```json
{
  "max_files_without_approval": 10,
  "max_files_red_zone": 1,
  "max_files_yellow_zone": 3,
  "red_zone_paths": [
    "domain/presupuesto/",
    "domain/estimacion/",
    "domain/valueobjects/",
    "domain/entities/"
  ],
  "yellow_zone_paths": [
    "infrastructure/persistence/"
  ],
  "override_keyword": "BIGBANG_APPROVED"
}
```

### Parámetros

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `max_files_without_approval` | int | 10 | Número máximo de archivos staged sin aprobación |
| `max_files_red_zone` | int | 1 | Número máximo de archivos en zona roja |
| `max_files_yellow_zone` | int | 3 | Número máximo de archivos en zona amarilla |
| `red_zone_paths` | string[] | Ver defaults | Paths que definen la zona roja (prefix matching) |
| `yellow_zone_paths` | string[] | Ver defaults | Paths que definen la zona amarilla (prefix matching) |
| `override_keyword` | string | "BIGBANG_APPROVED" | Palabra clave para bypass de validación |

### Configuración por Defecto

Si no se especifica un archivo de configuración, se usan estos valores:

- **Red Zone**: `domain/presupuesto/`, `domain/estimacion/`, `domain/valueobjects/`, `domain/entities/`
- **Yellow Zone**: `infrastructure/persistence/`
- **Límites**: 10 total, 1 red, 3 yellow

### Clasificación de Zonas

Los archivos se clasifican usando **prefix matching** (case-sensitive):

1. **Red Zone**: Archivos cuyo path empieza con algún path de `red_zone_paths`
2. **Yellow Zone**: Archivos cuyo path empieza con algún path de `yellow_zone_paths` (si no coincidió con red)
3. **Green Zone**: Todos los demás archivos

**Estrategia First-Match-Wins**: Si un archivo coincide con red zone, no se verifica yellow zone.

## 🔗 Git Hooks

### Instalación del Hook Pre-commit

Para ejecutar la validación automáticamente en cada commit:

```bash
cd tools/blast-radius-validator
./install-hook.sh
```

El script:
1. Construye el JAR si no existe
2. Crea el hook pre-commit
3. Se integra con hooks existentes (si los hay)

### Bypass del Hook

Si necesitas hacer commit sin validación (no recomendado):

```bash
git commit --no-verify
```

**Nota**: Es mejor usar el override keyword en el mensaje de commit.

## 🔑 Override Keyword

Puedes bypassar todas las validaciones incluyendo la palabra clave configurada (por defecto `BIGBANG_APPROVED`) en tu mensaje de commit:

```bash
git commit -m "feat: Major refactoring

BIGBANG_APPROVED

This change has been reviewed and approved."
```

Cuando se detecta el override keyword:
- ✅ Todas las validaciones se saltan
- ✅ El commit se permite
- ✅ Se muestra un mensaje indicando que se usó override

## 🔄 CI/CD

### GitHub Actions

Ejemplo de workflow para validar en CI:

```yaml
name: Blast Radius Validation

on:
  pull_request:
    branches: [main, develop]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build validator
        run: |
          cd tools/blast-radius-validator
          ./mvnw package -DskipTests
      
      - name: Validate blast radius
        run: |
          java -jar tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar .
```

### Códigos de Salida

| Código | Significado | Uso en CI/CD |
|--------|-------------|--------------|
| 0 | Validación exitosa | ✅ Permitir merge |
| 1 | Validación fallida | ❌ Bloquear merge |
| 2 | Error (config/Git) | ❌ Bloquear merge |

## 🐛 Troubleshooting

### "Git repository not found"

**Problema**: El validador no encuentra el directorio `.git`.

**Solución**: Asegúrate de ejecutar el comando desde dentro del repositorio Git o especifica la ruta correcta.

### "Configuration error"

**Problema**: Error al cargar el archivo de configuración.

**Soluciones**:
- Verifica que el JSON sea válido
- Asegúrate de que todos los campos requeridos estén presentes
- Revisa que los valores numéricos sean positivos
- Verifica que las listas de paths no estén vacías

### "No staged files found"

**Problema**: No hay archivos staged en el índice de Git.

**Solución**: Esto es normal si no hay cambios staged. El validador retornará éxito con 0 archivos.

### Hook no se ejecuta

**Problema**: El hook pre-commit no se ejecuta automáticamente.

**Soluciones**:
1. Verifica que el hook sea ejecutable: `chmod +x .git/hooks/pre-commit`
2. Verifica que el hook esté instalado: `ls -la .git/hooks/pre-commit`
3. Reinstala el hook: `./tools/blast-radius-validator/install-hook.sh`

### Colores no se muestran

**Problema**: La salida no tiene colores.

**Solución**: 
- Los colores se deshabilitan automáticamente si no hay consola (p. ej., en CI)
- Para deshabilitar manualmente: `--no-colors`
- Para forzar colores: `export TERM=xterm-color`

## 📚 Ejemplos

Ver [EXAMPLES.md](EXAMPLES.md) para ejemplos detallados de configuración y uso.

## 🤝 Contribución

Este validador es parte del conjunto de herramientas de validación de BudgetPro. Para reportar problemas o sugerir mejoras, crea un issue en el repositorio.

## 📄 Licencia

Parte del proyecto BudgetPro.
