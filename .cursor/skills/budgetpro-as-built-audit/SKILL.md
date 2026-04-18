---
name: budgetpro-as-built-audit
description: Determina qué está implementado en el repositorio BudgetPro usando código y git como evidencia. Usar para madurez, cobertura, auditorías "as-built", o preguntas "¿existe X?".
---

# Auditoría as-built (Modo A)

## Objetivo

Responder **qué hay en el repo** sin confundir documentación con hechos verificados.

## Pasos

1. **Delimitar** la pregunta (módulo, endpoint, entidad, flujo).
2. **Evidencia primaria**: búsqueda en código (`grep`, codebase_search), lectura de implementaciones, tests, migraciones Flyway/Liquibase si aplica.
3. **Evidencia secundaria**: `git log` / rama / tags cuando la pregunta sea sobre **historial** o **quién introdujo** un cambio.
4. **Documentación canónica**: usar solo como **guía** o para contrastar intención; si difiere del código, **declarar la discrepancia** y citar rutas de código que sustentan el as-built.
5. **Conclusión**: lista de hallazgos con **rutas de archivo** o commits; evitar lenguaje absoluto si la evidencia es parcial.

## No hacer

- No implementar features en este skill salvo que el usuario cambie explícitamente de objetivo.
- No afirmar cobertura o comportamiento en runtime sin señal en código, configuración o tests.
