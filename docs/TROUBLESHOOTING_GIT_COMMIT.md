# Solución: `git commit` — error "unknown option `trailer'"

## Causa

Cursor IDE añade por defecto `--trailer "Made-with: Cursor"` a los comandos `git commit` (función **Attribution**). Git requiere **2.32+** para soportar `--trailer`; versiones antiguas devuelven este error.

## Solución recomendada: desactivar Attribution en Cursor

1. Abrir **Cursor Settings** (Ctrl+, o Cmd+, en Mac)
2. Ir a **Agents** → **Attribution**
3. **Desactivar** el toggle

Con esto, Cursor dejará de inyectar `--trailer` y los commits funcionarán con cualquier versión de Git.

## Alternativas

### Actualizar Git a 2.32+

```bash
git --version   # verificar versión actual
# En Debian/Ubuntu:
sudo apt update && sudo apt install git
```

### Usar terminal para hacer commits

Los commits desde la terminal integrada o externa no pasan por la integración de Cursor y no incluyen trailers.

### Script de commits (hooks desactivados)

Para ejecutar `scripts/atomic-commits-req64.sh` u otros scripts que usen `git commit`:

```bash
# Desactivar temporalmente hooks (incl. AXIOM)
git config core.hooksPath /dev/null
# ... ejecutar commits ...
# Restaurar
git config --unset core.hooksPath
```

---

**Referencia**: [Cursor Forum — trailer in git commit](https://forum.cursor.com/t/trailer-in-git-commit-messages-cant-be-stopped/150552)
