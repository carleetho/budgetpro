# <MODULO>_GAP_STUDY.md — Plantilla

> Copiar a `<MODULO>_GAP_STUDY.md` y rellenar. Borrar esta línea y las instrucciones entre `<>` al publicar.

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | `<nombre>` |
| % oficial (tablero) | `<n>%` — fuente: [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [`<FILE>_MODULE_CANONICAL.md`](../../modules/<FILE>_MODULE_CANONICAL.md) |
| Fecha revisión | YYYY-MM-DD |
| Autor / revisores | |

## 2. Superficie de código (evidencia)

- Dominio / paquete: `<ruta o paquete Java>`
- REST (`*Controller`): `<lista>`
- Migraciones Flyway relevantes: `<Vxx__...>`
- Otros: `<listeners, jobs, …>`

## 3. Gaps funcionales (REST / casos de uso)

| ID | Esperado (canónico §) | Observado (código) | Severidad |
|----|------------------------|---------------------|-----------|
| GF-01 | | | P0 / P1 / P2 |

## 4. Gaps de reglas / invariantes

| ID | Regla (ID canónico) | Estado doc | Estado código | Notas |
|----|---------------------|------------|-----------------|-------|
| GR-01 | | | | |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas | Enlace a O-* / H-* |
|----|------|-------|---------------------|
| DT-01 | | | |

## 6. Candidatos de cierre (priorizado)

1. **P0**: …
2. **P1**: …
3. **P2**: …

## 7. Definición de hecho para subir %

- Condiciones mínimas: `<ej. cerrar todos los P0 + prueba manual documentada>`
- % objetivo siguiente: `<n>%` (justificación breve)

## 8. Referencias cruzadas

- Actualizaciones aplicadas en este PR: `<MODULE_SPECS_CURRENT / CODE_DOC_REVIEW_LOG / canónico …>`
