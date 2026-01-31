# AXIOM Handbook

Este documento sirve como la guía definitiva para la operación y mantenimiento del sistema AXIOM en BudgetPro.

## 1. Modos de Operación

| Modo       | Descripción            | Uso                                                                                       |
| :--------- | :--------------------- | :---------------------------------------------------------------------------------------- |
| **MODE_0** | Lockdown de Emergencia | Cuando el proyecto no compila (>50 errores). Solo se permiten correcciones estructurales. |
| **MODE_1** | Estabilización         | Compila pero hay fallos masivos en tests o deuda crítica.                                 |
| **MODE_2** | Operación Normal       | CI verde, sin deuda crítica. Un cambio por commit.                                        |

## 2. Protocolo de Restauración de Emergencia

En caso de una refactorización fallida o estado inconsistente masivo:

1. **Declarar MODE_0**: Informar al equipo que el proyecto está en estado crítico.
2. **Identificar Baseline Estable**: Localizar la última rama o commit conocido (ej: `rescue/post-audit-base`).
3. **Bulk Restoration**:
   - Reemplazar directorios afectados usando el baseline.
   - Ejecutar `./mvnw clean compile` para asegurar integridad sintáctica.
4. **Validación AXIOM**: Ejecutar `./axiom.sh --dry-run`.
5. **Commit de Recuperación**:
   - Usar el tag `BIGBANG_APPROVED` para bypass de blast radius.
   - Documentar la fuente de la restauración.

## 3. Manejo de Overrides

| Tag                    | Propósito              | Aplicación                                              |
| :--------------------- | :--------------------- | :------------------------------------------------------ |
| `BIGBANG_APPROVED`     | Bypass de Blast Radius | Para restauraciones masivas o refactors autorizados.    |
| `OVERRIDE_ESTIMACION`  | Bypass zona Estimación | Saltarse bloqueos específicos del módulo de Estimación. |
| `OVERRIDE_DOMAIN_CORE` | Bypass zona Dominio    | Saltarse límites en Value Objects y Entities.           |

## 4. Mantenimiento de Validadores

### Falsos Positivos en Código Perezoso (Lazy Code)

El validador de código perezoso puede detectar falsos positivos en:

- **Java Records**: El cuerpo vacío `{ }` es detectado como método vacío.
- **Solución**: Añadir un marcador estático interno para romper la detección del regex:
  ```java
  public record MyRecord(...) {
      private static final boolean AXIOM_COMPLIANT = true;
  }
  ```

### Resolución de Rutas de Maven

Si el validador de seguridad falla al encontrar `mvnw`, asegúrese de que el `SecurityValidator.py` maneje correctamente el `cwd` del subproceso, especialmente cuando se ejecuta desde subdirectorios como `backend/`.
