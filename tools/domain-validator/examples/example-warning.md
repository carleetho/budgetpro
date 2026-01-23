# Ejemplo: Advertencia (Warning)

Este ejemplo muestra una advertencia que no bloquea el merge pero requiere revisión.

## Escenario

Código que implementa parcialmente un módulo antes de que todas sus dependencias estén completamente implementadas.

## Comando

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --repo-path ../../backend
```

## Salida Esperada

```
Validating repository: /path/to/backend
Strict mode: false

⚠️  Validation completed: WARNINGS

📊 Summary:
  Total violations: 1
  Critical violations: 0
  Warnings: 1
  Info: 0

⚠️  Warnings (1):

  Module: inventarios
  Type: BUSINESS_LOGIC
  Message: Premature module development detected. Some dependencies are not complete.
  Suggestion: Complete prerequisite modules first: compras (currently IN_PROGRESS)
  Blocking: false
  Context:
    - Dependency: inventarios → compras (BUSINESS_LOGIC)
    - Current status: compras is IN_PROGRESS
    - Recommendation: Wait for compras to be COMPLETE before continuing
```

## Exit Code

```
$ echo $?
2
```

## Interpretación

- ⚠️ **Advertencia**: Desarrollo prematuro detectado
- ✅ **No bloquea**: El merge está permitido
- 📋 **Revisión recomendada**: Se recomienda completar dependencias primero

## Modo Estricto

Si ejecutas con `--strict`, la advertencia se convierte en crítica:

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --strict
```

```
❌ Validation completed: CRITICAL_VIOLATIONS
```

Exit code: `1` (bloquea merge)

## Acción Recomendada

1. **Revisar dependencias**:
   ```bash
   java -jar domain-validator-1.0.0-SNAPSHOT.jar check-module inventarios --show-dependencies
   ```

2. **Completar módulo dependiente**:
   - Asegurar que `compras` está completo antes de continuar con `inventarios`

3. **O justificar el desarrollo prematuro**:
   - Si hay razón válida, documentar en el PR
   - El merge está permitido en modo no-estricto

## Reporte JSON

```json
{
  "validation_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-21T12:00:00Z",
  "status": "WARNINGS",
  "violations": [
    {
      "module_id": "inventarios",
      "severity": "WARNING",
      "type": "BUSINESS_LOGIC",
      "message": "Premature module development detected",
      "suggestion": "Complete prerequisite modules first: compras",
      "blocking": false
    }
  ]
}
```
