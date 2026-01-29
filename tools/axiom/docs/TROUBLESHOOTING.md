# Solución de Problemas (Troubleshooting)

## 1. El archivo `.cursorrules` no se actualiza

**Problema:** Modificaste `axiom.config.yaml` pero la IA sigue comportándose igual.

**Causa:** AXIOM no se sincroniza automáticamente en segundo plano.

**Solución:** Debes ejecutar el script de sincronización manualmente después de cada cambio en la configuración.

```bash
python3 tools/axiom/sync_cursorrules.py
```

## 2. Error "Validation failed" al sincronizar

**Problema:**

```text
axiom-sync - ERROR - Configuration validation failed: 'role' is a required property
```

**Causa:** Tu archivo `axiom.config.yaml` no cumple con el esquema JSON requerido.

**Solución:** Revisa `tools/axiom/schema/axiom-config.schema.json` para ver qué campos son obligatorios. Asegúrate de que la indentación YAML sea correcta.

## 3. Advertencia "Generated content exceeds limits"

**Problema:**

```text
axiom-sync - WARNING - Generated content exceeds recommended word limits!
```

**Causa:** Tienes demasiadas reglas, prohibiciones o lecciones aprendidas. Los modelos de IA tienen un contexto limitado. Si las reglas son muy largas, la IA podría olvidar parte de tu código o instrucciones.

**Solución:**

- Elimina prohibiciones obvias o redundantes.
- Resume la descripción de las zonas de protección.
- Mantén solo el contexto histórico más crítico.

## 4. La IA ignora las zonas de protección

**Problema:** La IA sugiere cambios en archivos de zona ROJA sin pedir permiso.

**Causa:**

1. El archivo `.cursorrules` no está en la raíz del espacio de trabajo.
2. El editor (Cursor/Copilot) no está leyendo el archivo correctamente.

**Solución:**

- Verifica que `.cursorrules` esté en la raíz del proyecto.
- Reinicia el editor.
- Asegúrate de mencionar explícitamente "Revisa las reglas del proyecto" al inicio de tu prompt si la IA parece perdida.
