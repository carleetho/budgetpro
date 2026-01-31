# Exception Handling Guidelines - Semgrep

## Propósito

Este documento establece las políticas formales para suprimir hallazgos de Semgrep mediante anotaciones `nosemgrep`.

## Principios Fundamentales

1. **Excepciones son Excepcionales**: Las supresiones deben ser raras y justificadas
2. **Justificación Obligatoria**: Toda supresión requiere explicación técnica detallada
3. **Auditabilidad**: Todas las excepciones son rastreables y revisables
4. **Revisión por Pares**: Excepciones requieren aprobación en code review

## Cuándo Suprimir una Regla

### ✅ Casos Válidos

#### 1. Requisitos de Frameworks

```java
@Entity
public class PresupuestoJpaEntity {
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    // Justificación: JPA requiere setters para proxies y lazy loading.
    // Esta es una entidad de infraestructura, no de dominio puro.
    // Ticket: ARCH-123
    private BigDecimal monto;

    protected void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
```

#### 2. Serialización/Deserialización

```java
public class ConfiguracionDTO {
    // nosemgrep: budgetpro.domain.immutability.valueobject-no-setters
    // Justificación: Jackson requiere setter para deserialización JSON.
    // DTO de infraestructura, no value object de dominio.
    private String valor;

    public void setValor(String valor) {
        this.valor = valor;
    }
}
```

#### 3. Interoperabilidad con Bibliotecas de Terceros

```java
public class LegacyAdapter {
    // nosemgrep: budgetpro.security.hardcoded-secrets
    // Justificación: API key pública de sandbox para testing.
    // Documentado en: https://docs.external-api.com/sandbox
    private static final String SANDBOX_KEY = "pk_test_public_sandbox_key";
}
```

#### 4. Código de Test con Propósito Específico

```java
public class SecurityRuleTest {
    // nosemgrep: budgetpro.security.hardcoded-secrets
    // Justificación: Credencial de prueba para validar regla de seguridad.
    // Este test verifica que la regla detecta secretos hardcodeados.
    private static final String TEST_PASSWORD = "test123";
}
```

### ❌ Casos Inválidos

#### 1. Conveniencia sin Justificación Técnica

```java
// ❌ RECHAZADO
public class MiClase {
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    // Justificación: Es más fácil usar setters
    private String campo;
}
```

#### 2. Desacuerdo con la Política

```java
// ❌ RECHAZADO
public class MiClase {
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    // Justificación: No creo que la inmutabilidad sea necesaria aquí
    private String campo;
}
```

#### 3. Sin Justificación

```java
// ❌ RECHAZADO
public class MiClase {
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    private String campo;
}
```

#### 4. Justificación Vaga

```java
// ❌ RECHAZADO
public class MiClase {
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    // Justificación: Necesario para el sistema
    private String campo;
}
```

## Formato de Supresión

### Estructura Obligatoria

```java
// nosemgrep: <rule-id>
// Justificación: <explicación técnica detallada>
// [Opcional] Ticket: <REQ-XXX o TECH-XXX>
// [Opcional] Documentación: <URL o referencia>
<código suprimido>
```

### Elementos Requeridos

1. **Anotación nosemgrep**: Debe estar en la línea inmediatamente anterior al código
2. **Rule ID**: ID exacto de la regla que se suprime (ej: `budgetpro.domain.immutability.entity-final-fields`)
3. **Justificación**: Explicación técnica de POR QUÉ es necesaria la excepción
4. **Contexto**: Información adicional que ayude a entender la decisión

### Elementos Opcionales pero Recomendados

1. **Ticket**: Referencia a requisito o decisión técnica documentada
2. **Documentación**: URL o referencia a documentación externa
3. **Alternativas Consideradas**: Por qué otras soluciones no funcionan
4. **Plan de Remediación**: Si es temporal, cuándo se eliminará

## Proceso de Revisión

### 1. Desarrollador Propone Excepción

```java
// nosemgrep: budgetpro.security.hardcoded-secrets
// Justificación: Clave pública de API de terceros para integración.
// La clave es pública y está documentada en su sitio oficial.
// Documentación: https://docs.payment-provider.com/public-keys
// Alternativa considerada: Variable de entorno (rechazada porque
// la clave es pública y debe estar versionada con el código)
private static final String PUBLIC_API_KEY = "pk_live_abc123";
```

### 2. Revisor Valida

El revisor debe verificar:

- [ ] ¿La justificación es técnicamente sólida?
- [ ] ¿Se consideraron alternativas?
- [ ] ¿La excepción es realmente necesaria?
- [ ] ¿El formato cumple con las guías?
- [ ] ¿Hay documentación de soporte si es necesario?

### 3. Aprobación Especial para Dominios Críticos

Para excepciones en dominios críticos (presupuesto, estimacion):

- Requiere aprobación de Arquitecto de Software
- Debe documentarse en ADR (Architecture Decision Record) si es significativa
- Revisión en reunión de arquitectura si afecta múltiples componentes

### 4. Auditoría Periódica

Mensualmente, el equipo de arquitectura ejecuta:

```bash
# Listar todas las excepciones de inmutabilidad
grep -r "nosemgrep.*immutability" backend/src/ --include="*.java" -B 2

# Listar todas las excepciones de seguridad
grep -r "nosemgrep.*security" backend/src/ --include="*.java" -B 2
```

Revisar:

- ¿Siguen siendo necesarias?
- ¿Hay nuevas alternativas disponibles?
- ¿Se puede refactorizar para eliminar la excepción?

## Categorías de Reglas y Políticas de Excepción

### Security (Severidad: ERROR - Bloqueante)

**Política**: Excepciones extremadamente raras, requieren aprobación de Security Lead

**Reglas**:

- `budgetpro.security.hardcoded-secrets`: Solo para claves públicas documentadas
- `budgetpro.security.jwt-weak-secret`: Sin excepciones permitidas
- `budgetpro.security.weak-cryptographic-algorithm`: Sin excepciones permitidas
- `budgetpro.security.cors-misconfiguration`: Solo para endpoints públicos específicos
- `budgetpro.security.missing-input-validation`: Solo para DTOs internos no expuestos

### Domain - Immutability (Severidad: ERROR para críticos, WARNING para otros)

**Política**: Excepciones permitidas para entidades de infraestructura, no para dominio puro

**Reglas**:

- `budgetpro.domain.immutability.entity-final-fields`: Permitido en JPA entities de infraestructura
- `budgetpro.domain.immutability.snapshot-no-setters`: Sin excepciones en dominio
- `budgetpro.domain.immutability.valueobject-no-setters`: Permitido en DTOs de infraestructura

### Architecture (Severidad: ERROR - Bloqueante)

**Política**: Excepciones requieren ADR y aprobación de arquitecto

**Reglas**:

- `budgetpro.architecture.domain-layer-isolation`: Sin excepciones
- `budgetpro.architecture.transactional-boundary`: Sin excepciones
- `budgetpro.architecture.dto-validation-boundary`: Excepciones solo para casos edge documentados

### Performance (Severidad: WARNING)

**Política**: Excepciones permitidas con justificación de trade-offs

**Reglas**:

- `budgetpro.performance.n-plus-one-query`: Permitido si se documenta que N es pequeño
- `budgetpro.performance.inefficient-bigdecimal-operations`: Permitido en código no crítico

### Quality (Severidad: WARNING/INFO)

**Política**: Excepciones permitidas con justificación razonable

**Reglas**:

- `budgetpro.quality.null-safety-patterns`: Permitido en código legacy con plan de migración
- `budgetpro.quality.exception-handling-standards`: Permitido en casos específicos documentados
- `budgetpro.quality.logging-standards`: Permitido en utilidades de bajo nivel

## Ejemplos Completos

### Ejemplo 1: Entidad JPA (VÁLIDO)

```java
package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class PresupuestoJpaEntity {

    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    // Justificación: JPA/Hibernate requiere setters para:
    // 1. Creación de proxies para lazy loading
    // 2. Gestión de estado de entidad (attached/detached)
    // 3. Dirty checking para optimistic locking
    // Esta es una entidad de INFRAESTRUCTURA, no de dominio.
    // El dominio usa PresupuestoEntity (inmutable) y el mapper
    // convierte entre ambas representaciones.
    // Ticket: ARCH-101 - Separación Dominio/Infraestructura
    private BigDecimal monto;

    protected PresupuestoJpaEntity() {
        // Constructor sin args requerido por JPA
    }

    protected void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMonto() {
        return monto;
    }
}
```

### Ejemplo 2: Clave Pública de API (VÁLIDO)

```java
package com.budgetpro.infrastructure.payment;

public class PaymentGatewayConfig {

    // nosemgrep: budgetpro.security.hardcoded-secrets
    // Justificación: Clave PÚBLICA de API de Stripe para checkout.
    // Esta clave es intencionalmente pública y se expone en el frontend.
    // Documentación oficial: https://stripe.com/docs/keys#public-keys
    // La clave secreta (sk_) está en variables de entorno.
    // Alternativa considerada: Variable de entorno (rechazada porque
    // esta clave debe estar versionada y es pública por diseño)
    public static final String STRIPE_PUBLIC_KEY = "pk_live_51H...";

    // La clave secreta SÍ está en variable de entorno
    private final String stripeSecretKey = System.getenv("STRIPE_SECRET_KEY");
}
```

### Ejemplo 3: Test de Seguridad (VÁLIDO)

```java
package com.budgetpro.security;

import org.junit.jupiter.api.Test;

public class HardcodedSecretDetectionTest {

    @Test
    void shouldDetectHardcodedPassword() {
        // nosemgrep: budgetpro.security.hardcoded-secrets
        // Justificación: Credencial de prueba para validar que la regla
        // de Semgrep detecta correctamente secretos hardcodeados.
        // Este test verifica el funcionamiento de la regla de seguridad.
        // La contraseña es ficticia y solo existe en este test.
        String testPassword = "hardcoded123";

        // Test logic...
    }
}
```

## Herramientas de Auditoría

### Script de Auditoría de Excepciones

```bash
#!/bin/bash
# .semgrep/scripts/audit-exceptions.sh

echo "=== Auditoría de Excepciones Semgrep ==="
echo ""

echo "## Excepciones de Seguridad (Requieren revisión prioritaria)"
grep -r "nosemgrep.*security" backend/src/ --include="*.java" -B 2 -A 1 | \
    grep -E "(nosemgrep|Justificación|File)" || echo "  ✅ No se encontraron excepciones de seguridad"

echo ""
echo "## Excepciones de Inmutabilidad en Dominios Críticos"
grep -r "nosemgrep.*immutability" backend/src/main/java/com/budgetpro/domain/**/presupuesto/ --include="*.java" -B 2 -A 1 | \
    grep -E "(nosemgrep|Justificación|File)" || echo "  ✅ No se encontraron excepciones en presupuesto"

grep -r "nosemgrep.*immutability" backend/src/main/java/com/budgetpro/domain/**/estimacion/ --include="*.java" -B 2 -A 1 | \
    grep -E "(nosemgrep|Justificación|File)" || echo "  ✅ No se encontraron excepciones en estimacion"

echo ""
echo "## Excepciones sin Justificación (CRÍTICO - Requieren corrección)"
grep -r "nosemgrep:" backend/src/ --include="*.java" -A 1 | \
    grep -v "Justificación" | \
    grep "nosemgrep" || echo "  ✅ Todas las excepciones tienen justificación"

echo ""
echo "=== Fin de Auditoría ==="
```

### Validación en Pre-commit

El hook de pre-commit puede validar que las nuevas excepciones tengan justificación:

```python
# En tools/axiom/validators/semgrep_validator.py
def validate_exception_justification(file_path: str, line_number: int) -> bool:
    """Valida que una excepción nosemgrep tenga justificación."""
    with open(file_path, 'r') as f:
        lines = f.readlines()

    # Buscar línea con nosemgrep
    if line_number >= len(lines):
        return False

    # Verificar que la siguiente línea contenga "Justificación:"
    if line_number + 1 < len(lines):
        next_line = lines[line_number + 1]
        return "Justificación:" in next_line or "justificación:" in next_line.lower()

    return False
```

## Métricas y Reportes

### Métricas a Monitorear

1. **Total de Excepciones**: Número total de supresiones en el codebase
2. **Excepciones por Categoría**: Desglose por tipo de regla
3. **Excepciones en Dominios Críticos**: Contador específico para presupuesto/estimacion
4. **Excepciones sin Justificación**: Contador de violaciones de política
5. **Tendencia Temporal**: Crecimiento o reducción de excepciones

### Reporte Mensual

```markdown
# Reporte de Excepciones Semgrep - [Mes/Año]

## Resumen Ejecutivo

- Total de excepciones: X
- Nuevas este mes: +Y
- Eliminadas este mes: -Z
- Tendencia: [Creciente/Estable/Decreciente]

## Desglose por Categoría

- Security: X excepciones (Y% del total)
- Immutability: X excepciones (Y% del total)
- Architecture: X excepciones (Y% del total)
- Performance: X excepciones (Y% del total)
- Quality: X excepciones (Y% del total)

## Excepciones en Dominios Críticos

- Presupuesto: X excepciones
- Estimación: X excepciones

## Acciones Requeridas

- [ ] Revisar excepciones sin justificación: X encontradas
- [ ] Auditar excepciones de seguridad: X requieren revisión
- [ ] Refactorizar para eliminar: X candidatas

## Recomendaciones

[Recomendaciones específicas basadas en hallazgos]
```

## Contacto y Soporte

Para preguntas sobre excepciones:

- **Política General**: Equipo de Arquitectura
- **Excepciones de Seguridad**: Security Lead
- **Excepciones de Dominio**: Domain Architect
- **Dudas Técnicas**: Canal #semgrep-support en Slack

---

**Última Actualización**: 2026-01-31  
**Versión**: 1.0  
**Mantenedor**: Equipo de Arquitectura BudgetPro
