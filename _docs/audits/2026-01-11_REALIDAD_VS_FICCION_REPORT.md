# 📉 REPORTE DE REALIDAD FORENSE — VALIDACIÓN DE ESTADO_PROYECTO.md

**Fecha:** 2026-01-11  
**Auditor:** Auditor de Código Forense (Nivel Hostil)  
**Objetivo:** Validar veracidad del informe `ESTADO_PROYECTO.md` contrastándolo con el sistema de archivos real

---

## 🚨 RESUMEN EJECUTIVO

**Veredicto General:** 🟡 **VERDAD A MEDIAS** — El informe es mayormente preciso pero oculta un **RIESGO CRÍTICO** de corrupción de datos.

**Hallazgos Críticos:**
- ✅ Seguridad JWT: **VERDADERO** (implementación robusta)
- ✅ Migraciones SQL: **VERDADERO** (WBS, CHECK constraints, movimiento_caja creada)
- 💀 **Persistencia MovimientoCaja: FALSO** — **CORRUPCIÓN DE DATOS LATENTE**
- ✅ Tests: **VERDADERO** (Testcontainers configurado correctamente)

---

## 📊 TABLA DE REALIDAD vs FICCIÓN

| Claim del Informe | Realidad en Código | Veredicto | Evidencia |
| :--- | :--- | :--- | :--- |
| **FIX-02: Auth JWT CUMPLIDO 100%** | ✅ `SecurityConfig.java` (L47): `/api/**` requiere autenticación<br>✅ `JwtTokenService.java` (L65-69): `verifyWith(secretKey)` valida firma<br>✅ `JwtTokenService.java` (L73): Valida expiración<br>✅ `JwtAuthenticationFilter.java` (L126): Usa servicio real | ✅ **VERDAD** | `SecurityConfig.java:47`, `JwtTokenService.java:65-69` |
| **FIX-01: WBS SQL CUMPLIDO** | ✅ `V6__hardening_database_fix01.sql` (L12-14): `ADD COLUMN parent_id UUID, nivel INT`<br>✅ `V6__hardening_database_fix01.sql` (L18-19): FK auto-referencial creada<br>✅ `V6__hardening_database_fix01.sql` (L27-28): CHECK `nivel > 0` | ✅ **VERDAD** | `V6__hardening_database_fix01.sql:12-28` |
| **FIX-01: Hardening DB CUMPLIDO** | ✅ `V6__hardening_database_fix01.sql` (L36-38): `CHECK (saldo_actual >= 0)` en billetera<br>✅ `V6__hardening_database_fix01.sql` (L64-65): `CHECK (cantidad >= 0)` en inventario<br>✅ `V6__hardening_database_fix01.sql` (L102-117): Tabla `movimiento_caja` creada | ✅ **VERDAD** | `V6__hardening_database_fix01.sql:36-38,64-65,102-117` |
| **DOM-03: MovimientoCaja PARCIAL** | ⚠️ `BilleteraRepositoryAdapter.java` (L72-74): **COMENTARIO EXPLÍCITO**<br>"NO se persisten movimientos nuevos aquí"<br>❌ `BilleteraRepositoryAdapter.save()` NO llama a ningún repositorio de MovimientoCaja<br>❌ `RegistrarCompraDirectaUseCaseImpl.java` (L113): Solo guarda billetera, NO movimientos | 💀 **PELIGROSO** | `BilleteraRepositoryAdapter.java:72-74`, `RegistrarCompraDirectaUseCaseImpl.java:113` |
| **QA-02: Test Crítico CUMPLIDO** | ✅ `CompraIntegrationTest.java` (L50): Extiende `AbstractIntegrationTest`<br>✅ `AbstractIntegrationTest.java`: Usa `@Testcontainers` y `PostgreSQLContainer`<br>✅ `CompraIntegrationTest.java` (L268-269): Valida saldo final<br>✅ `CompraIntegrationTest.java` (L282,287): Valida stock final | ✅ **VERDAD** | `CompraIntegrationTest.java:268-289`, `AbstractIntegrationTest.java` |

---

## 🔍 ANÁLISIS DETALLADO POR ÁREA

### 1. 🕵️ AUDITORÍA DE SEGURIDAD (Claim: "FIX-02 CUMPLIDO 100%")

**Estado:** ✅ **VERDADERO** — Implementación robusta y completa

**Evidencia Encontrada:**

1. **SecurityConfig.java** (Líneas 36-62)
   ```java
   .authorizeHttpRequests(auth -> auth
       .requestMatchers("/api/**").authenticated()  // ✅ Bloquea anónimos
   )
   .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
   ```

2. **JwtTokenService.java** (Líneas 64-69)
   ```java
   Claims claims = Jwts.parser()
       .verifyWith(secretKey)  // ✅ Valida firma real
       .build()
       .parseSignedClaims(token)
   ```

3. **JwtTokenService.java** (Líneas 72-76)
   ```java
   if (expiration != null && expiration.before(new Date())) {
       return null;  // ✅ Rechaza tokens expirados
   }
   ```

4. **JwtAuthenticationFilter.java** (Líneas 126-130)
   ```java
   String userId = jwtTokenService.validateAndExtractUserId(token);
   if (userId == null) {
       response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // ✅ Rechaza inválidos
   }
   ```

**Conclusión:** ✅ **VERDAD** — La seguridad JWT está completamente implementada y funcional.

---

### 2. 🗄️ AUDITORÍA DE DATOS (Claim: "WBS CUMPLIDO", "Hardening DB CUMPLIDO")

**Estado:** ✅ **VERDADERO** — Migraciones SQL completas y correctas

**Evidencia Encontrada:**

1. **V6__hardening_database_fix01.sql** (Líneas 12-14)
   ```sql
   ALTER TABLE partida
       ADD COLUMN IF NOT EXISTS parent_id UUID,
       ADD COLUMN IF NOT EXISTS nivel INT NOT NULL DEFAULT 1;
   ```

2. **V6__hardening_database_fix01.sql** (Líneas 18-19)
   ```sql
   ADD CONSTRAINT fk_partida_parent
       FOREIGN KEY (parent_id) REFERENCES partida(id) ON DELETE CASCADE;
   ```

3. **V6__hardening_database_fix01.sql** (Líneas 36-38)
   ```sql
   ALTER TABLE billetera
       ADD CONSTRAINT chk_billetera_saldo_no_negativo
           CHECK (saldo_actual >= 0);
   ```

4. **V6__hardening_database_fix01.sql** (Líneas 102-117)
   ```sql
   CREATE TABLE IF NOT EXISTS movimiento_caja (
       id UUID PRIMARY KEY,
       billetera_id UUID NOT NULL,
       tipo VARCHAR(20) NOT NULL,
       monto NUMERIC(19,4) NOT NULL,
       ...
   );
   ```

**Conclusión:** ✅ **VERDAD** — Todas las migraciones SQL están implementadas correctamente.

---

### 3. 💸 AUDITORÍA FINANCIERA (Claim: "Transacciones atómicas validadas")

**Estado:** 💀 **PELIGROSO** — **CORRUPCIÓN DE DATOS LATENTE DETECTADA**

**Evidencia Crítica:**

#### ❌ PROBLEMA 1: MovimientoCaja NO se persiste

**BilleteraRepositoryAdapter.java** (Líneas 72-74):
```java
// NOTA: Según el ERD físico definitivo, solo se persiste la billetera.
// Los movimientos de caja se tratarán en una tarea posterior cuando se defina la tabla en el ERD.
// Por lo tanto, NO se persisten movimientos nuevos aquí (están fuera del alcance según CURRENT_TASK.md).
```

**Análisis:**
- ✅ La tabla `movimiento_caja` **EXISTE** en la BD (V6__hardening_database_fix01.sql:102)
- ✅ El dominio `Billetera` **GENERA** movimientos (`Billetera.egresar()` línea 111-115)
- ✅ Los movimientos se agregan a `movimientosNuevos` (línea 115)
- ❌ **PERO** `BilleteraRepositoryAdapter.save()` **NO PERSISTE** los movimientos
- ❌ Solo guarda el saldo actualizado, **PERDIENDO LA TRAZABILIDAD**

#### ❌ PROBLEMA 2: Contrato del Puerto Violado

**BilleteraRepository.java** (Líneas 32-35):
```java
/**
 * REGLA CRÍTICA: Este método debe:
 * 1. Persistir la billetera (con el saldo y versión actualizados)
 * 2. Persistir TODOS los movimientos nuevos del agregado  // ❌ NO SE CUMPLE
 * 3. Ejecutarse en una transacción ACID única
 */
```

**RegistrarCompraDirectaUseCaseImpl.java** (Línea 113):
```java
billeteraRepository.save(billetera);  // ❌ Solo guarda saldo, NO movimientos
```

#### 💀 IMPACTO REAL:

1. **Pérdida de Auditoría:** No hay registro histórico de cambios en billetera
2. **Violación de Invariante:** El dominio dice "Todo cambio genera un MovimientoCaja", pero no se persiste
3. **Corrupción de Datos:** El saldo cambia sin evidencia en BD
4. **Imposibilidad de Rollback:** Sin movimientos, no se puede revertir transacciones

**Conclusión:** 💀 **CORRUPCIÓN DE DATOS LATENTE** — El sistema funciona pero **PIERDE TRAZABILIDAD CRÍTICA**.

---

### 4. 🧪 AUDITORÍA DE TESTS (Claim: "Test Crítico Compra CUMPLIDO")

**Estado:** ✅ **VERDADERO** — Test válido con Testcontainers

**Evidencia Encontrada:**

1. **CompraIntegrationTest.java** (Línea 50):
   ```java
   * - Base de datos PostgreSQL real en contenedor (Testcontainers)
   ```

2. **AbstractIntegrationTest.java**:
   ```java
   @Testcontainers
   public abstract class AbstractIntegrationTest {
       @Container
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
   ```

3. **CompraIntegrationTest.java** (Líneas 264-269):
   ```java
   // Then: Verificar que el saldo se rebajó correctamente
   Optional<Billetera> billeteraDespues = billeteraRepository.findByProyectoId(proyectoId);
   assertThat(billeteraDespues).isPresent();
   BigDecimal saldoFinal = billeteraDespues.get().getSaldoActual();
   BigDecimal saldoEsperado = saldoInicial.subtract(new BigDecimal("5300.00"));
   assertThat(saldoFinal).isEqualByComparingTo(saldoEsperado);
   ```

4. **CompraIntegrationTest.java** (Líneas 277-289):
   ```java
   // Then: Verificar que el stock aumentó correctamente
   BigDecimal stockFinal1 = inventariosDespues.stream()
       .filter(i -> i.getRecursoId().getValue().equals(recursoId1))
       .findFirst()
       .map(InventarioItem::getStock)
       .orElse(BigDecimal.ZERO);
   assertThat(stockFinal1).isEqualByComparingTo(new BigDecimal("100.00"));
   ```

**Conclusión:** ✅ **VERDAD** — El test usa Testcontainers con PostgreSQL real y valida saldo y stock.

---

## 🚨 HALLAZGOS CRÍTICOS

### 💀 HALLAZGO CRÍTICO #1: MovimientoCaja No Persistido

**Severidad:** 🔴 **CRÍTICA**  
**Categoría:** Corrupción de Datos / Pérdida de Auditoría

**Descripción:**
- El dominio `Billetera` genera `MovimientoCaja` cuando cambia el saldo
- La tabla `movimiento_caja` existe en la BD
- **PERO** `BilleteraRepositoryAdapter.save()` NO persiste los movimientos
- Solo guarda el saldo actualizado, perdiendo trazabilidad histórica

**Evidencia:**
- `BilleteraRepositoryAdapter.java:72-74` — Comentario explícito admitiendo el problema
- `BilleteraRepository.java:34` — Contrato del puerto requiere persistir movimientos
- `RegistrarCompraDirectaUseCaseImpl.java:113` — Solo guarda billetera, no movimientos

**Impacto:**
- ❌ Imposibilidad de auditar cambios en billetera
- ❌ Violación de invariante del dominio ("Todo cambio genera un MovimientoCaja")
- ❌ Imposibilidad de revertir transacciones sin evidencia
- ❌ Riesgo de corrupción de datos en producción

**Recomendación:** 🔴 **BLOQUEANTE** — Implementar persistencia de MovimientoCaja antes de producción.

---

## 📋 VEREDICTO FINAL

### ✅ VERDADERO (4/5)

1. ✅ **Seguridad JWT:** Implementación completa y robusta
2. ✅ **Migraciones SQL:** WBS, CHECK constraints, movimiento_caja creada
3. ✅ **Tests:** Testcontainers con PostgreSQL real, validaciones completas
4. ✅ **Arquitectura:** Puertos en capa correcta, transacciones atómicas

### 💀 FALSO / PELIGROSO (1/5)

5. 💀 **Persistencia MovimientoCaja:** **NO IMPLEMENTADA** — Corrupción de datos latente

---

## 🎯 RECOMENDACIONES INMEDIATAS

### 🔴 PRIORIDAD CRÍTICA (Bloqueante para Producción)

1. **Implementar Persistencia de MovimientoCaja**
   - Crear `MovimientoCajaEntity` y `MovimientoCajaJpaRepository`
   - Modificar `BilleteraRepositoryAdapter.save()` para persistir movimientos
   - Actualizar `BilleteraMapper` para manejar movimientos
   - Validar que se persistan en la misma transacción que la billetera

**Estimación:** 1-2 días  
**Riesgo si no se corrige:** 🔴 **ALTO** — Pérdida de auditoría financiera

---

## 📊 MÉTRICA DE VERACIDAD DEL INFORME

| Métrica | Valor |
|---------|-------|
| **Claims Verificados** | 5 |
| **Claims Verdaderos** | 4 (80%) |
| **Claims Falsos/Peligrosos** | 1 (20%) |
| **Severidad del Falso Positivo** | 🔴 **CRÍTICA** |
| **Veracidad General** | 🟡 **VERDAD A MEDIAS** |

---

## 🏁 CONCLUSIÓN

El informe `ESTADO_PROYECTO.md` es **mayormente preciso** pero **oculta un riesgo crítico**:

- ✅ La mayoría de las afirmaciones son **VERDADERAS**
- 💀 **PERO** la persistencia de MovimientoCaja **NO ESTÁ IMPLEMENTADA**
- 💀 Esto representa un **RIESGO CRÍTICO** de corrupción de datos y pérdida de auditoría

**Recomendación:** El informe debe actualizarse para reflejar este hallazgo crítico y marcar DOM-03 como **NO CUMPLIDO** en lugar de "PARCIAL".

---

**FIN DEL REPORTE FORENSE**
