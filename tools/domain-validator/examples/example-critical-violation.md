# Ejemplo: Violación Crítica

Este ejemplo muestra una violación crítica que bloquea el merge.

## Escenario

Código que implementa el módulo `compras` sin que `presupuesto` tenga el mecanismo de freeze implementado.

## Comando

```bash
java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --repo-path ../../backend --strict
```

## Salida Esperada

```
Validating repository: /path/to/backend
Strict mode: true

❌ Validation completed: CRITICAL_VIOLATIONS

📊 Summary:
  Total violations: 2
  Critical violations: 1
  Warnings: 1
  Info: 0

🔴 Critical Violations (1):

  Module: compras
  Type: STATE_DEPENDENCY
  Message: Presupuesto freeze mechanism missing. Compras requires Presupuesto to be in CONGELADO state.
  Suggestion: Implement PresupuestoService.congelar() method and EstadoPresupuesto.CONGELADO enum value before developing Compras module.
  Blocking: true
  Context:
    - Expected: Presupuesto.estado === CONGELADO
    - Detected: Presupuesto entity exists but freeze mechanism not found
    - Dependency chain: compras → presupuesto (STATE_DEPENDENCY)

⚠️  Warnings (1):

  Module: compras
  Type: TEMPORAL_DEPENDENCY
  Message: Premature module development detected
  Suggestion: Complete prerequisite modules first: presupuesto, tiempo
```

## Exit Code

```
$ echo $?
1
```

## Interpretación

- ❌ **Violación Crítica**: El módulo `compras` requiere que `presupuesto` tenga un mecanismo de freeze, pero no está implementado
- ⚠️ **Advertencia**: Desarrollo prematuro detectado
- 🚫 **Bloqueo**: El merge está bloqueado hasta resolver la violación crítica

## Acción Requerida

1. **Implementar freeze mechanism en Presupuesto**:
   ```java
   // Presupuesto.java
   public void congelar() {
       this.estado = EstadoPresupuesto.CONGELADO;
   }
   ```

2. **Verificar acoplamiento temporal con Tiempo**:
   ```java
   // PresupuestoService.java
   public void congelarPresupuestoYPrograma(UUID presupuestoId) {
       presupuesto.congelar();
       programaObra.congelar(); // Acoplamiento temporal
   }
   ```

3. **Re-ejecutar validación**:
   ```bash
   java -jar domain-validator-1.0.0-SNAPSHOT.jar validate --strict
   ```

## Reporte JSON

```json
{
  "validation_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-21T12:00:00Z",
  "status": "CRITICAL_VIOLATIONS",
  "violations": [
    {
      "module_id": "compras",
      "severity": "CRITICAL",
      "type": "STATE_DEPENDENCY",
      "message": "Presupuesto freeze mechanism missing",
      "suggestion": "Implement PresupuestoService.congelar() method",
      "blocking": true
    }
  ]
}
```
