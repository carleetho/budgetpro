---
name: budgetpro-axiom-gate
description: Sesión de validación AXIOM en BudgetPro — handbook, modos operativos, axiom.sh y blast radius. Usar cuando el usuario mencione AXIOM, validadores, arquitectura hexagonal bloqueada o gobernanza de commits.
---

# AXIOM gate y handbook

## Pasos

1. Leer `.budgetpro/handbook/AXIOM_SAFE_OPERATIONS.md` completo antes de recomendar excepciones.
2. Ejecutar desde la raíz del repositorio: `./axiom.sh --dry-run` (u otros argumentos que el usuario indique y que soporte `tools/axiom/axiom_sentinel.py`).
3. **Declarar** en el chat: modo operativo (MODE_0 / MODE_1 / MODE_2), riesgo (LOW / MID / HIGH) y breve justificación según el handbook y la salida de AXIOM.
4. Si hay `[BLOCKING]`: leer la sección de sugerencia de AXIOM y aplicar o proponer el arreglo **mecánico** adecuado al modo activo (sin nuevas features en MODE_0).
5. **Blast radius**: si AXIOM o el handbook exigen batches más pequeños, planificar commits temáticos antes de editar masivamente.
6. **Warnings de seguridad** (LEAK, SECRET, etc.): tratarlos como bloqueantes hasta corregir o escalar con el usuario.

## No hacer

- No sugerir bypass de verificación para “ir más rápido”.
- No mezclar refactors de dominio con fixes de compilación en MODE_0 sin plan explícito.
