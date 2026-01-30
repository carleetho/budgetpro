# Validador de Inmutabilidad - Protección de Snapshots y Value Objects

## Propósito

El Validador de Inmutabilidad implementa el principio fundamental de BudgetPro: **"La verdad no es retroactiva"**.

### Doctrina: La Verdad No Es Retroactiva

En BudgetPro, la historia del proyecto **no se reescribe**. Los datos históricos no se corrigen, se explican mediante eventos formales como órdenes de cambio o excepciones justificadas.

**Implicaciones para el código:**

- Los **snapshots** (APUSnapshot, EstimacionSnapshot, CronogramaSnapshot, EVMSnapshot) capturan el estado del sistema en un momento dado y deben permanecer inmutables para preservar el audit trail.
- Los **value objects** (ProyectoId, PresupuestoId, MontoEstimado) representan conceptos sin identidad y deben ser inmutables por definición en Domain-Driven Design.
- Las **entidades de dominio** deben usar campos `final` para garantizar que el estado no cambie después de la construcción.

### Por Qué Es Crítico

1. **Integridad del Audit Trail**: Los snapshots son la única fuente de verdad histórica. Si pueden modificarse, se pierde la trazabilidad forense.
2. **Cumplimiento Financiero**: Los presupuestos congelados y las estimaciones aprobadas son contratos digitales. Modificarlos retroactivamente viola principios contables y legales.
3. **Integridad Criptográfica**: El sistema usa hashes SHA-256 para sellar presupuestos. Si los snapshots son mutables, los hashes pierden su validez.

## Reglas Implementadas

El validador incluye tres reglas de Semgrep que detectan violaciones de inmutabilidad:

### 1. Entity Final Fields (`04-entity-final-fields.yaml`)

**Qué detecta:** Campos privados sin la palabra clave `final` en entidades de dominio.

**Severidad:**
- **ERROR** (bloqueante) para dominios críticos: `presupuesto`, `estimacion`
- **WARNING** (no bloqueante) para otros dominios

**Ejemplo de Violación:**

```java
package com.budgetpro.domain.finanzas.presupuesto.model;

public class Presupuesto {
    // ❌ VIOLACIÓN: Campo sin 'final'
    private String nombre;
    
    // ✅ CORRECTO: Campo con 'final'
    private final PresupuestoId id;
}
```

**Corrección:**

```java
package com.budgetpro.domain.finanzas.presupuesto.model;

public class Presupuesto {
    // ✅ CORRECTO: Todos los campos son 'final'
    private final String nombre;
    private final PresupuestoId id;
}
```

**Paths escaneados:**
- `**/domain/**/model/*.java`
- Dominios críticos: `**/domain/**/presupuesto/**/model/*.java`, `**/domain/**/estimacion/**/model/*.java`

---

### 2. Snapshot Immutability (`05-snapshot-immutability.yaml`)

**Qué detecta:**
1. Métodos setter (`public void set*()`) en clases con "Snapshot" en el nombre
2. Clases Snapshot sin marcadores de inmutabilidad (`@Immutable` o `record`)

**Severidad:**
- **ERROR** (bloqueante) para setters
- **WARNING** (no bloqueante) para marcadores faltantes

**Ejemplo de Violación (Setter):**

```java
package com.budgetpro.domain.catalogo.model;

public class APUSnapshot {
    private BigDecimal rendimiento;
    
    // ❌ VIOLACIÓN: Setter en snapshot
    public void setRendimiento(BigDecimal valor) {
        this.rendimiento = valor;
    }
}
```

**Corrección (Factory Method):**

```java
package com.budgetpro.domain.catalogo.model;

public final class APUSnapshot {
    private final BigDecimal rendimiento;
    
    private APUSnapshot(BigDecimal rendimiento) {
        this.rendimiento = rendimiento;
    }
    
    // ✅ CORRECTO: Factory method para crear nuevos snapshots
    public static APUSnapshot crear(BigDecimal rendimiento) {
        return new APUSnapshot(rendimiento);
    }
    
    // ✅ CORRECTO: Método de negocio controlado (no es setter)
    public void actualizarRendimiento(BigDecimal nuevoRendimiento, UUID usuarioId) {
        // Lógica de negocio con validación y auditoría
        if (nuevoRendimiento.compareTo(BigDecimal.ZERO) > 0) {
            this.rendimiento = nuevoRendimiento;
        }
    }
}
```

**Ejemplo de Violación (Marcador Faltante):**

```java
// ❌ VIOLACIÓN: Snapshot sin marcador de inmutabilidad
public class EstimacionSnapshot {
    private final String itemsSnapshot;
    // ...
}
```

**Corrección:**

```java
// ✅ CORRECTO: Con anotación @Immutable
@Immutable
public class EstimacionSnapshot {
    private final String itemsSnapshot;
    // ...
}

// ✅ CORRECTO: Como record (inmutable por definición)
public record CronogramaSnapshot(String fechasJson) {
}
```

**Paths escaneados:**
- `**/*Snapshot.java`

**Nota importante:** El validador **no detecta** métodos de negocio controlados como `actualizarRendimiento()` porque no siguen el patrón `set*`.

---

### 3. Value Object No Setters (`06-valueobject-no-setters.yaml`)

**Qué detecta:** Métodos setter (`public void set*()`) con implementación en value objects.

**Severidad:** **ERROR** (bloqueante) para todas las violaciones.

**Ejemplo de Violación:**

```java
package com.budgetpro.domain.finanzas.valueobjects;

public class MontoVO {
    private BigDecimal amount;
    
    // ❌ VIOLACIÓN: Setter en value object
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
```

**Corrección:**

```java
package com.budgetpro.domain.finanzas.valueobjects;

public final class MontoEstimado {
    private final BigDecimal value;
    
    // ✅ CORRECTO: Constructor privado
    private MontoEstimado(BigDecimal value) {
        this.value = validateAndRound(value);
    }
    
    // ✅ CORRECTO: Factory method
    public static MontoEstimado of(BigDecimal value) {
        return new MontoEstimado(value);
    }
    
    public BigDecimal getValue() {
        return value;
    }
}
```

**Paths escaneados:**
- `**/domain/**/valueobjects/*.java`

**Nota importante:** El validador **no detecta** declaraciones abstractas en interfaces porque requiere implementación (llave de apertura `{`).

---

## Configuración

La configuración del validador se encuentra en `.semgrep/config/immutability-validator.yaml`.

### Dominios Críticos

Los dominios críticos son aquellos donde las violaciones generan severidad **ERROR** (bloqueante) en lugar de **WARNING**.

**Dominios críticos por defecto:**
- `presupuesto`: Core financiero, integridad criptográfica crítica
- `estimacion`: Core financiero, cálculos de estimación deben ser inmutables

**Para agregar un nuevo dominio crítico:**

1. Editar `.semgrep/config/immutability-validator.yaml` y agregar el dominio a `critical_domains`
2. Actualizar la regla `04-entity-final-fields.yaml` agregando el path:
   ```yaml
   paths:
     include:
       - "**/domain/**/<nuevo-dominio>/**/model/*.java"
   ```
3. Actualizar los tests en `.semgrep/tests/domain/04-entity-final-fields.java`

### Exclusiones

Los siguientes paths están excluidos de la validación:

- `**/test/**`, `**/tests/**`: Archivos de prueba (manejados por `.semgrepignore`)
- `**/legacy/**`: Código legacy que no cumple con estándares actuales
- `**/infrastructure/persistence/entity/**`: Entidades JPA pueden necesitar mutabilidad para frameworks de persistencia

**Para agregar una exclusión:**

1. Editar `.semgrep/config/immutability-validator.yaml` y agregar el path a `exclusions`
2. Actualizar las reglas correspondientes agregando el path a `exclude`:
   ```yaml
   paths:
     exclude:
       - "**/nuevo-path-excluido/**"
   ```

### Overrides de Severidad

Para cambiar la severidad de un dominio específico:

1. Editar `.semgrep/config/immutability-validator.yaml` y descomentar/agregar en `severity_overrides`:
   ```yaml
   severity_overrides:
     catalogo: ERROR  # Hacer que catalogo sea bloqueante
   ```
2. Actualizar la regla correspondiente en `.semgrep/rules/domain/`

---

## Ejemplos Completos

### Ejemplo 1: Entidad de Dominio Correcta

```java
package com.budgetpro.domain.finanzas.presupuesto.model;

public final class Presupuesto {
    // ✅ Todos los campos son 'final'
    private final PresupuestoId id;
    private final UUID proyectoId;
    private final String nombre;
    private final EstadoPresupuesto estado;
    
    // ✅ Constructor privado
    private Presupuesto(PresupuestoId id, UUID proyectoId, 
                       String nombre, EstadoPresupuesto estado) {
        this.id = Objects.requireNonNull(id);
        this.proyectoId = Objects.requireNonNull(proyectoId);
        this.nombre = Objects.requireNonNull(nombre);
        this.estado = Objects.requireNonNull(estado);
    }
    
    // ✅ Factory method
    public static Presupuesto crear(UUID proyectoId, String nombre) {
        return new Presupuesto(
            PresupuestoId.nuevo(),
            proyectoId,
            nombre,
            EstadoPresupuesto.BORRADOR
        );
    }
}
```

### Ejemplo 2: Snapshot Correcto

```java
package com.budgetpro.domain.catalogo.model;

@Immutable
public final class APUSnapshot {
    private final APUSnapshotId id;
    private final BigDecimal rendimientoOriginal;
    private BigDecimal rendimientoVigente;  // Campo controlado con método de negocio
    
    private APUSnapshot(APUSnapshotId id, BigDecimal rendimientoOriginal, 
                       BigDecimal rendimientoVigente) {
        this.id = Objects.requireNonNull(id);
        this.rendimientoOriginal = rendimientoOriginal;
        this.rendimientoVigente = rendimientoVigente;
    }
    
    // ✅ Factory method
    public static APUSnapshot crear(APUSnapshotId id, BigDecimal rendimiento) {
        return new APUSnapshot(id, rendimiento, rendimiento);
    }
    
    // ✅ Método de negocio controlado (no es setter)
    public void actualizarRendimiento(BigDecimal nuevoRendimiento, UUID usuarioId) {
        if (nuevoRendimiento.compareTo(BigDecimal.ZERO) > 0) {
            this.rendimientoVigente = nuevoRendimiento;
            // Registrar auditoría...
        }
    }
}
```

### Ejemplo 3: Value Object Correcto

```java
package com.budgetpro.domain.finanzas.estimacion.model;

public final class MontoEstimado {
    private final BigDecimal value;
    
    private MontoEstimado(BigDecimal value) {
        this.value = validateAndRound(value);
    }
    
    // ✅ Factory methods
    public static MontoEstimado of(BigDecimal value) {
        return new MontoEstimado(value);
    }
    
    public static MontoEstimado zero() {
        return new MontoEstimado(BigDecimal.ZERO);
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    // ✅ Métodos de transformación (crean nuevos objetos)
    public MontoEstimado sumar(MontoEstimado other) {
        return new MontoEstimado(this.value.add(other.value));
    }
}
```

---

## Troubleshooting

### Falsos Positivos Comunes

#### 1. Métodos de Negocio Controlados

**Problema:** El validador detecta `actualizarRendimiento()` como setter.

**Solución:** Los métodos de negocio controlados no siguen el patrón `set*`, por lo que no son detectados. Si tu método se llama `setRendimiento()`, renómbralo a `actualizarRendimiento()` o `modificarRendimiento()`.

```java
// ❌ Detectado como setter
public void setRendimiento(BigDecimal valor) { ... }

// ✅ No detectado (método de negocio)
public void actualizarRendimiento(BigDecimal valor, UUID usuarioId) { ... }
```

#### 2. Entidades JPA en Infraestructura

**Problema:** El validador detecta campos sin `final` en entidades JPA.

**Solución:** Las entidades JPA están excluidas automáticamente por el path `**/infrastructure/persistence/entity/**`. Si necesitas excluir otro path, agrégalo a `.semgrep/config/immutability-validator.yaml`.

#### 3. Archivos de Prueba

**Problema:** El validador detecta violaciones en archivos de test.

**Solución:** Los archivos de test están excluidos automáticamente por `.semgrepignore`. Si un archivo de test está siendo escaneado, verifica que esté en un directorio `test/` o `tests/`.

#### 4. Supresión de Violaciones

Si necesitas suprimir una violación legítima (caso excepcional), usa el comentario `nosemgrep`:

```java
// nosemgrep: budgetpro.domain.immutability.entity-final-fields
// Razón: Campo temporal para migración legacy, será removido en v2.0
private String campoLegacy;
```

**Importante:** Siempre proporciona una razón para la supresión.

---

## Integración con CI/CD

El validador se ejecuta automáticamente en cada Pull Request y push a las ramas `main` y `develop` mediante el workflow `.github/workflows/semgrep.yml`.

### Comportamiento en CI/CD

- **Violaciones ERROR**: Bloquean el merge del PR. El workflow falla con código de salida no-cero.
- **Violaciones WARNING**: Se muestran en comentarios del PR pero no bloquean el merge.

### Verificación Local

Para ejecutar el validador localmente antes de hacer push:

```bash
# Escanear todas las reglas (incluye inmutabilidad)
semgrep --config .semgrep/rules/ .

# Escanear solo reglas de dominio
semgrep --config .semgrep/rules/domain/ .

# Ejecutar tests de las reglas
semgrep --test .semgrep/rules/domain/
```

---

## Referencias

- **Configuración:** `.semgrep/config/immutability-validator.yaml`
- **Reglas:**
  - `04-entity-final-fields.yaml`
  - `05-snapshot-immutability.yaml`
  - `06-valueobject-no-setters.yaml`
- **Tests:** `.semgrep/tests/domain/04-entity-final-fields.java`, etc.
- **Workflow CI/CD:** `.github/workflows/semgrep.yml`
- **Doctrina de Negocio:** `docs/context/BUSINESS_MANIFESTO.md` (Principio 5: Verdad No Retroactiva)

---

## Preguntas Frecuentes

**P: ¿Por qué los snapshots deben ser inmutables?**  
R: Los snapshots capturan el estado del sistema en un momento dado para auditoría. Si pueden modificarse, se pierde la trazabilidad forense y se viola el principio "la verdad no es retroactiva".

**P: ¿Qué pasa si necesito actualizar un snapshot?**  
R: Crea un nuevo snapshot con el estado actualizado. Los snapshots históricos deben permanecer inmutables. Si necesitas modificar el rendimiento de un APU, usa el método controlado `actualizarRendimiento()` que registra auditoría.

**P: ¿Por qué los value objects no pueden tener setters?**  
R: Los value objects representan conceptos sin identidad en DDD. Deben ser inmutables por definición. Si necesitas un valor diferente, crea un nuevo value object.

**P: ¿Cómo agrego un nuevo dominio crítico?**  
R: Ver sección "Configuración > Dominios Críticos" arriba.

**P: ¿El validador detecta métodos como `actualizarRendimiento()`?**  
R: No. El validador solo detecta métodos que siguen el patrón `set*`. Los métodos de negocio controlados no son detectados.
