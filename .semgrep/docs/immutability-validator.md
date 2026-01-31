# Validador de Inmutabilidad - BudgetPro

## Política de Gobernanza

Este documento define la estrategia de validación de inmutabilidad para el dominio de BudgetPro, implementada mediante reglas Semgrep ejecutables.

## Contexto y Rationale

La inmutabilidad en el dominio es crítica para:

1. **Integridad de Datos**: Prevenir modificaciones accidentales de estado
2. **Thread-Safety**: Garantizar seguridad en contextos concurrentes
3. **Auditabilidad**: Facilitar trazabilidad de cambios
4. **Arquitectura Hexagonal**: Mantener pureza del dominio

## Dominios Críticos

Los siguientes dominios requieren enforcement bloqueante (severidad ERROR):

### Presupuesto

- **Rationale**: Core financiero con integridad criptográfica
- **Enforcement**: Violaciones de inmutabilidad bloquean PRs
- **Reglas Aplicables**: 04-entity-final-fields (critical), 05-snapshot-immutability, 06-valueobject-no-setters

### Estimación

- **Rationale**: Cálculos financieros deben ser reproducibles e inmutables
- **Enforcement**: Violaciones de inmutabilidad bloquean PRs
- **Reglas Aplicables**: 04-entity-final-fields (critical), 05-snapshot-immutability, 06-valueobject-no-setters

## Catálogo de Reglas de Inmutabilidad

### Regla 04: Entity Final Fields

**ID**: `budgetpro.domain.immutability.entity-final-fields`

**Descripción**: Detecta campos privados sin `final` en entidades de dominio

**Severidad**:

- ERROR para dominios críticos (presupuesto, estimacion)
- WARNING para otros dominios

**Paths Escaneados**:

- Críticos: `**/domain/**/presupuesto/**/model/*.java`
- Críticos: `**/domain/**/estimacion/**/model/*.java`
- Otros: `**/domain/**/model/*.java`

**Archivo de Regla**: [04-entity-final-fields.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/04-entity-final-fields.yaml)

**Tests**: [04-entity-final-fields.java](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/tests/domain/04-entity-final-fields.java)

**Ejemplo de Violación**:

```java
// ❌ INCORRECTO
public class PresupuestoEntity {
    private BigDecimal monto; // Sin final
}
```

**Corrección**:

```java
// ✅ CORRECTO
public class PresupuestoEntity {
    private final BigDecimal monto;

    public PresupuestoEntity(BigDecimal monto) {
        this.monto = monto;
    }
}
```

---

### Regla 05: Snapshot Immutability

**ID**:

- `budgetpro.domain.immutability.snapshot-no-setters` (ERROR)
- `budgetpro.domain.immutability.snapshot-markers` (WARNING)

**Descripción**:

- Sub-regla 1: Detecta setters en clases Snapshot
- Sub-regla 2: Detecta snapshots sin marcadores de inmutabilidad (@Immutable o record)

**Severidad**:

- ERROR para setters en snapshots
- WARNING para snapshots sin marcadores explícitos

**Paths Escaneados**: `**/*Snapshot.java`

**Archivo de Regla**: [05-snapshot-immutability.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/05-snapshot-immutability.yaml)

**Tests**: [05-snapshot-immutability.java](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/tests/domain/05-snapshot-immutability.java)

**Ejemplo de Violación**:

```java
// ❌ INCORRECTO
public class PresupuestoSnapshot {
    private BigDecimal monto;

    public void setMonto(BigDecimal monto) { // Setter prohibido
        this.monto = monto;
    }
}
```

**Corrección**:

```java
// ✅ CORRECTO - Opción 1: Record
public record PresupuestoSnapshot(BigDecimal monto) {}

// ✅ CORRECTO - Opción 2: Clase con @Immutable
@Immutable
public class PresupuestoSnapshot {
    private final BigDecimal monto;

    public PresupuestoSnapshot(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMonto() {
        return monto;
    }
}
```

---

### Regla 06: Value Object No Setters

**ID**: `budgetpro.domain.immutability.valueobject-no-setters`

**Descripción**: Detecta setters en Value Objects

**Severidad**: ERROR (todos los dominios)

**Paths Escaneados**: `**/domain/**/valueobjects/*.java`

**Archivo de Regla**: [06-valueobject-no-setters.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/06-valueobject-no-setters.yaml)

**Tests**: [06-valueobject-no-setters.java](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/tests/domain/06-valueobject-no-setters.java)

**Ejemplo de Violación**:

```java
// ❌ INCORRECTO
public class Moneda {
    private String codigo;

    public void setCodigo(String codigo) { // Setter prohibido
        this.codigo = codigo;
    }
}
```

**Corrección**:

```java
// ✅ CORRECTO - Opción 1: Record
public record Moneda(String codigo) {}

// ✅ CORRECTO - Opción 2: Clase inmutable
public class Moneda {
    private final String codigo;

    public Moneda(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    // Para "modificar", crear nueva instancia
    public Moneda withCodigo(String nuevoCodigo) {
        return new Moneda(nuevoCodigo);
    }
}
```

## Patrones de Exclusión

Las siguientes ubicaciones están excluidas de validación de inmutabilidad:

### Archivos de Test

- `**/test/**`
- `**/tests/**`

**Rationale**: Tests pueden necesitar mutabilidad para setup/mocking

### Código Legacy

- `**/legacy/**`

**Rationale**: Código legacy no cumple estándares actuales, refactorización planificada

### Entidades JPA de Infraestructura

- `**/infrastructure/persistence/entity/**`

**Rationale**: Frameworks de persistencia (Hibernate, JPA) requieren mutabilidad para proxies y lazy loading

### Configuración y Utilidades

- `**/config/**`
- `**/util/**`

**Rationale**: Clases de configuración y utilidades pueden tener requisitos específicos

## Manejo de Excepciones

### Cuándo Suprimir una Regla

Las supresiones son válidas SOLO en estos casos:

1. **Frameworks de Persistencia**: Entidades JPA que requieren setters para frameworks
2. **Serialización**: Clases que requieren mutabilidad para serialización/deserialización
3. **Interoperabilidad**: Integración con bibliotecas de terceros que requieren mutabilidad
4. **Casos de Borde Documentados**: Situaciones excepcionales con justificación técnica sólida

### Formato de Supresión

```java
// nosemgrep: budgetpro.domain.immutability.entity-final-fields
// Justificación: [Explicación técnica detallada del por qué es necesario]
// Ticket: [REQ-XXX o TECH-XXX si aplica]
private String campoMutable;
```

### Proceso de Revisión

1. **Desarrollador**: Agrega supresión con justificación completa
2. **Code Review**: Revisor valida que la justificación es técnicamente sólida
3. **Arquitecto**: En casos críticos, arquitecto aprueba excepciones en dominios críticos
4. **Auditoría**: Excepciones son auditables via `grep -r "nosemgrep.*immutability"`

### Ejemplos de Excepciones Válidas

```java
// ✅ VÁLIDO: Framework JPA requiere setter
@Entity
public class PresupuestoJpaEntity {
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    // Justificación: JPA requiere setters para proxies y lazy loading
    // Esta es una entidad de infraestructura, no de dominio
    private BigDecimal monto;

    protected void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
```

### Ejemplos de Excepciones Inválidas

```java
// ❌ INVÁLIDO: Justificación vaga
// nosemgrep: budgetpro.domain.immutability.entity-final-fields
// Justificación: Es más fácil así
private BigDecimal monto;

// ❌ INVÁLIDO: Sin justificación
// nosemgrep: budgetpro.domain.immutability.entity-final-fields
private BigDecimal monto;
```

## Enforcement en CI/CD

### Contexto Local (Desarrollo)

- **Config**: `.semgrep/config/local.yaml`
- **Comportamiento**: Warnings, no bloquea
- **Objetivo**: Feedback temprano sin interrumpir flujo

### Contexto PR (Pull Request)

- **Config**: `.semgrep/config/pr.yaml`
- **Comportamiento**: ERROR bloquea merge, WARNING permite merge con comentario
- **Objetivo**: Gate de calidad antes de integración

### Contexto Main (Branch Principal)

- **Config**: `.semgrep/config/main.yaml`
- **Comportamiento**: ERROR bloquea, genera SARIF para GitHub Code Scanning
- **Objetivo**: Protección de branch principal + métricas

## Integración con AXIOM

El validador de inmutabilidad está integrado en AXIOM como `semgrep_validator`:

```yaml
# axiom.config.yaml
validators:
  semgrep_validator:
    enabled: true
    rules_path: .semgrep/rules
    config_path: .semgrep/config/local.yaml
```

AXIOM ejecuta Semgrep en pre-commit hook para validación temprana.

## Troubleshooting

### "Regla no se ejecuta"

**Síntoma**: Violación obvia no es detectada

**Diagnóstico**:

```bash
# Verificar que la regla existe
ls .semgrep/rules/domain/04-entity-final-fields.yaml

# Ejecutar regla específica
semgrep --config .semgrep/rules/domain/04-entity-final-fields.yaml backend/src/
```

**Solución**: Verificar paths en la regla coinciden con estructura del proyecto

### "Falso Positivo"

**Síntoma**: Regla detecta violación en código correcto

**Diagnóstico**:

```bash
# Ejecutar con verbose para ver matching
semgrep --config .semgrep/rules/domain/ --verbose backend/src/path/to/file.java
```

**Solución**: Agregar supresión con justificación o refinar patrón de regla

### "Performance Lento"

**Síntoma**: Scan tarda más de 3 minutos

**Diagnóstico**:

```bash
# Medir tiempo de scan
time semgrep --config .semgrep/rules/ --metrics backend/src/
```

**Solución**:

- Verificar `.semgrepignore` excluye directorios grandes (target/, node_modules/)
- Considerar ejecutar solo en archivos modificados en pre-commit

## Mantenimiento

### Agregar Nuevo Dominio Crítico

1. Actualizar este documento: agregar dominio a sección "Dominios Críticos"
2. Actualizar regla 04: agregar path pattern en sección `critical`
3. Agregar tests: crear casos de prueba en `.semgrep/tests/domain/`
4. Validar: `semgrep --test .semgrep/rules/domain/04-entity-final-fields.yaml`
5. Documentar: actualizar `RULE_CATALOG.md`

### Modificar Severidad de Dominio

1. Actualizar este documento: documentar cambio y rationale
2. Modificar regla: cambiar `severity` en archivo YAML
3. Actualizar tests: verificar que tests reflejan nueva severidad
4. Validar: ejecutar scan completo y verificar comportamiento
5. Comunicar: notificar al equipo del cambio en política

## Referencias

- [Semgrep Developer Guide](file:///home/wazoox/Desktop/budgetpro-backend/docs/semgrep-guide.md)
- [Exception Guidelines](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/docs/exception-guidelines.md)
- [Rule Catalog](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/RULE_CATALOG.md)
- [AXIOM Configuration](file:///home/wazoox/Desktop/budgetpro-backend/axiom.config.yaml)

---

**Última Actualización**: 2026-01-31  
**Versión**: 1.0  
**Mantenedor**: Equipo de Arquitectura BudgetPro
