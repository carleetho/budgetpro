# Auditoría de archivos en raíz del repositorio

**Fecha**: 2026-03-08  
**Propósito**: Identificar archivos en la raíz que puedan ser inseguros, redundantes o estar mal ubicados.

---

## 1. Riesgo ALTO — Acción inmediata recomendada

### 1.1 Archivos de resultados de escaneo (Semgrep) — **TRACKEADOS EN GIT**

| Archivo           | Tamaño aprox. | Estado Git | Riesgo |
|------------------|---------------|------------|--------|
| `findings.json`  | ~5.7 MB       | Trackeado  | Alto   |
| `findings.sarif` | ~4.1 MB       | Trackeado  | Alto   |
| `semgrep-findsecbug.json` | ~139 KB | Trackeado  | Medio  |

- **Problema**: Contienen resultados de escaneos de seguridad (Semgrep/FindSecBugs). Pueden revelar vulnerabilidades a atacantes.
- **Recomendación**: Remover del historial de Git, agregar a `.gitignore` y no volver a commitear.
- **Acción**: `git rm --cached findings.json findings.sarif semgrep-findsecbug.json` + commit de limpieza.

### 1.2 Archivos de credenciales — **NO trackeados (correcto)**

| Archivo             | Contenido sensible      | .gitignore |
|---------------------|-------------------------|------------|
| `database.env`      | DB password, JWT secret | Sí         |
| `RESEND_API_KEY.env`| API key Resend          | Sí (*.env) |

- **Estado**: No se commitean. Mantener así.
- **Nota**: Asegurarse de que ningún hook o script accidentalmente los agregue.

---

## 2. Archivos generados / artefactos — Revisar si deben estar en repo

### 2.1 Archivos `.txt` (listados generados por AXIOM/validator)

| Archivo                | Tamaño | Descripción                              | Estado Git |
|------------------------|--------|------------------------------------------|------------|
| `domain_files.txt`     | ~45 KB | Lista de archivos de dominio             | Trackeado  |
| `domain_files_clean.txt` | ~20 KB | Versión filtrada                        | Trackeado  |
| `domain_files_final.txt` | ~19 KB | Versión final                            | Trackeado  |
| `full_domain_list.txt` | ~19 KB | Lista completa de dominio                 | Trackeado  |
| `hardened_files.txt`   | ~2 KB  | Archivos endurecidos (hardened)           | Trackeado  |
| `unprotected_files.txt`| ~17 KB | Archivos sin protección                  | Trackeado  |
| `status_detailed.txt`  | ~19 KB | Estado detallado (output de herramienta) | Trackeado  |

- **Problema**: Son output de herramientas; cambian con cada ejecución. Generan ruido en diffs y no aportan al código fuente.
- **Recomendación**: Agregar a `.gitignore` y quitarlos del índice si no son necesarios para CI/CD.
- **Alternativa**: Mover a `.budgetpro/` o `tools/axiom/output/` y excluirlos de commits.

---

## 3. Scripts `.sh` en raíz

| Archivo           | Propósito                         | ¿En repo? |
|-------------------|-----------------------------------|-----------|
| `axiom.sh`        | Ejecutar pipeline AXIOM           | Sí        |
| `dev.sh`          | Iniciar entorno de desarrollo     | Sí        |
| `secure-commit.sh`| Quality gate pre-commit           | Sí        |
| `workflow.sh`     | Flujo BrainGrid + AXIOM           | Sí        |

- **Evaluación**: Tienen sentido en raíz para invocación rápida (`./axiom.sh`, `./dev.sh`).
- **Alternativa**: Mover a `scripts/` y documentar en README (ej. `./scripts/axiom.sh`).
- **Recomendación**: Opcional. Mantener en raíz es aceptable si el equipo los usa así.

---

## 4. Documentación `.md` en raíz

### 4.1 Documentos que conviene mantener en raíz

| Archivo                        | Motivo                                   |
|--------------------------------|------------------------------------------|
| `README.md`                    | Convención estándar de repos              |
| `GAP_ANALISIS_EVM_CPI_SPI.md`  | Análisis de brechas EVM activo y referenciado |

### 4.2 Borradores de PR (sin trackear)

| Archivo         | Estado Git |
|-----------------|------------|
| `pr_body_req62.md` | Untracked |
| `pr_body_req63.md` | Untracked |

- **Recomendación**: Añadir `pr_body_*.md` a `.gitignore` o guardarlos en `docs/borradores/` si se quieren versionar.

### 4.3 Documentos históricos / auditorías

| Archivo                             | Posible destino |
|-------------------------------------|-----------------|
| `ANALISIS_PROVIDER_ENTITY_REQUIREMENT.md` | `docs/audits/` o `docs/archive/` |
| `ANALISIS_STASHES.md`               | `docs/archive/` |
| `AUDITORIA_CURSORRULES_COMPLIANCE.md`| `docs/audits/`  |
| `CLEAN_SLATE_REPORT.md`             | `docs/archive/` |
| `DIAGNOSTICO_AXIOM_PRE_PR.md`       | `docs/audits/`  |
| `ESTRUCTURA_PROYECTO.md`            | `docs/`         |
| `INVESTIGACION_MODULE_SPECS.md`      | `docs/archive/` |
| `MAIN_READINESS_CONFIRMATION.md`     | `docs/archive/` |
| `MODULE_STATUS_REPORT.md`           | `docs/archive/` |
| `PR_DESCRIPTION*.md`                | `docs/archive/` o borrar si obsoletos |
| `REQUERIMIENTO_INICIO_STATUS.md`     | `docs/archive/` |
| `SECURITY_FIX.md`                    | `docs/` o `docs/security/` |

- **Recomendación**: Mover a `docs/` para mantener la raíz limpia.

---

## 5. Archivos que no deberían estar en raíz (no trackeados)

| Patrón / Archivo      | Cantidad | Recomendación                    |
|-----------------------|----------|----------------------------------|
| `startup*.log`        | ~15      | Ya cubiertos por `*.log` en .gitignore |
| `restoration_build.log`| 1       | Idem                             |
| `findings.json`       | 1        | Mover a .gitignore y dejar de trackear |

---

## 6. Resumen de acciones recomendadas

### Prioridad 1 (seguridad)

1. **Eliminar del índice de Git** (mantener local si se necesita):
   ```bash
   git rm --cached findings.json findings.sarif semgrep-findsecbug.json
   ```

2. **Añadir a `.gitignore`**:
   ```
   findings.json
   findings.sarif
   semgrep-findsecbug.json
   ```

### Prioridad 2 (limpieza)

3. **Eliminar del índice** los `.txt` generados (si no los usa CI):
   ```bash
   git rm --cached domain_files.txt domain_files_clean.txt domain_files_final.txt \
     full_domain_list.txt hardened_files.txt unprotected_files.txt status_detailed.txt
   ```

4. **Añadir a `.gitignore`**:
   ```
   domain_files*.txt
   full_domain_list.txt
   hardened_files.txt
   unprotected_files.txt
   status_detailed.txt
   ```

### Prioridad 3 (organización, opcional)

5. Mover documentos históricos a `docs/archive/` o `docs/audits/`.
6. Mover scripts a `scripts/` si se prefiere raíz más limpia.
7. Añadir `pr_body_*.md` a `.gitignore` si son solo borradores locales.

---

## 7. Verificación post-cambios

```bash
# Ver qué archivos quedarían en la raíz tras los cambios
ls -la *.md *.sh *.txt 2>/dev/null

# Confirmar que findings no se trackea
git ls-files | grep findings
# (debería estar vacío)
```
