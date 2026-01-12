# 🕵️ AUDITORÍA FORENSE — VALIDACIÓN CRUZADA DIRECTIVA v2.0

**Fecha:** 2026-01-11  
**Auditor:** Auditor Técnico Forense (Hostil)  
**Objetivo:** Validar cumplimiento real de la Directiva Maestra de Ingeniería v2.0 (RECOVERY)

---

## 🚦 SEMÁFORO DE CUMPLIMIENTO REAL

| Área | Estado (REAL) | Evidencia Encontrada (Archivo/Línea) | Brecha Restante |
|------|---------------|--------------------------------------|-----------------|
| **Seguridad** | 🟡 **PARCIAL** | `SecurityConfig.java` (Línea 36-62) | JWT Filter acepta cualquier token (no valida firma/expiración) |
| **WBS / Datos** | 🟢 **CUMPLIDO** | `V6__hardening_database_fix01.sql` (Línea 12-28) | ✅ `parent_id` y `nivel` agregados a `partida` |
| **Hardening DB** | 🟢 **CUMPLIDO** | `V6__hardening_database_fix01.sql` (Línea 36-38, 64-67) | ✅ CHECK constraints en `billetera` e `inventario_item` |
| **Tablas Faltantes** | 🟢 **CUMPLIDO** | `V6__hardening_database_fix01.sql` (Línea 47-120) | ✅ `inventario_item`, `consumo_partida`, `movimiento_caja` creadas |
| **Gestión Secretos** | 🟢 **CUMPLIDO** | `application.yml` (Línea 6-8) | ✅ Variables de entorno configuradas, credenciales eliminadas |
| **Arquitectura Puertos** | 🔴 **FALLO CRÍTICO** | `application/compra/port/out/CompraRepository.java` | ❌ Puertos en `application` en lugar de `domain/model/{agregado}/port/out` |
| **Lógica WBS Partida** | 🔴 **FALLO CRÍTICO** | `domain/finanzas/presupuesto/Partida.java` | ❌ No tiene métodos `addSubPartida()`, `calcularTotalRollup()` |
| **Movimiento Caja** | 🟡 **PARCIAL** | `domain/finanzas/model/Billetera.java` (Línea 72-118) | ⚠️ Genera `MovimientoCaja` pero no garantiza persistencia en MISMA transacción |
| **Testing** | 🔴 **FALLO CRÍTICO** | `src/test/java/` | ❌ No existe `CompraIntegrationTest` con flujo completo |

---

## 🚨 ALERTA DE ALUCINACIONES

### 1. **JWT Filter: Validación Falsa**
**Archivo:** `JwtAuthenticationFilter.java` (Línea 123-155)

**Problema:** El método `validateAndCreateAuthentication()` acepta **cualquier token no vacío** como válido. No valida:
- ❌ Firma del token
- ❌ Expiración
- ❌ Revocación
- ❌ Claims reales

**Evidencia:**
```java
// Línea 133-135: Implementación temporal: aceptar token si no está vacío
// En producción, debe validar con:
// Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
```

**Impacto:** **CRÍTICO** — Sistema vulnerable a tokens falsos. Cualquier string no vacío es aceptado como autenticación válida.

**Estado:** 🟡 **PARCIAL** — Estructura correcta, validación real NO implementada.

---

### 2. **Puertos de Salida: Ubicación Incorrecta**
**Archivos:**
- `application/compra/port/out/CompraRepository.java`
- `application/compra/port/out/InventarioRepository.java`
- `application/compra/port/out/OutboxEventRepository.java`
- `application/recurso/port/out/RecursoRepository.java`

**Problema:** Según Directiva v2.0, los puertos de salida DEBEN estar en `domain/model/{agregado}/port/out`, pero están en `application/`.

**Evidencia:**
```
❌ ACTUAL: src/main/java/com/budgetpro/application/compra/port/out/CompraRepository.java
✅ DEBE SER: src/main/java/com/budgetpro/domain/finanzas/compra/port/out/CompraRepository.java
```

**Impacto:** **CRÍTICO** — Violación de Arquitectura Hexagonal. El dominio depende de la capa de aplicación.

**Estado:** 🔴 **FALLO CRÍTICO** — No cumplido.

---

### 3. **WBS en Partida: Lógica Faltante**
**Archivo:** `domain/finanzas/presupuesto/Partida.java`

**Problema:** La tabla `partida` tiene `parent_id` y `nivel` (✅ SQL correcto), pero el agregado `Partida` NO tiene:
- ❌ Método `addSubPartida(Partida hijo)`
- ❌ Método `calcularTotalRollup()` (cálculo ascendente)
- ❌ Campo `parentId` o `nivel` en el dominio
- ❌ Lógica para manejar jerarquía

**Evidencia:** El archivo `Partida.java` no contiene ninguna referencia a `parent`, `nivel`, `hijo`, `subPartida`, o `rollup`.

**Impacto:** **CRÍTICO** — WBS jerárquico existe en BD pero NO en dominio. Imposible crear partidas hijas desde código.

**Estado:** 🔴 **FALLO CRÍTICO** — Estructura SQL correcta, lógica de dominio NO implementada.

---

### 4. **Movimiento Caja: Persistencia No Garantizada**
**Archivo:** `domain/finanzas/model/Billetera.java` (Línea 72-118)

**Problema:** `Billetera.egresar()` y `Billetera.ingresar()` generan `MovimientoCaja` y lo agregan a `movimientosNuevos`, pero:
- ⚠️ No hay garantía de que se persista en la MISMA transacción que actualiza el saldo
- ⚠️ El repositorio debe llamar explícitamente a `getMovimientosNuevos()` y persistirlos
- ⚠️ Si el repositorio olvida persistir movimientos, se pierde trazabilidad

**Evidencia:**
```java
// Línea 111-115: Crea movimiento pero no garantiza persistencia
MovimientoCaja movimiento = MovimientoCaja.crearEgreso(this.id, monto, referencia, evidenciaUrl);
this.saldoActual = saldoResultante;
this.version = this.version + 1;
this.movimientosNuevos.add(movimiento); // ⚠️ Depende de que el repositorio lo persista
```

**Impacto:** **ALTO** — Riesgo de pérdida de trazabilidad si el repositorio no persiste movimientos.

**Estado:** 🟡 **PARCIAL** — Lógica correcta, garantía transaccional NO explícita.

---

### 5. **Testing: Test Crítico Faltante**
**Archivo:** `src/test/java/`

**Problema:** Según Directiva v2.0, debe existir `CompraIntegrationTest` que valide:
- Crear Partida
- Ingresar Saldo
- Comprar
- Verificar rebaja de Saldo y aumento de Stock

**Evidencia:**
- ✅ Existe `AbstractIntegrationTest` con Testcontainers (PostgreSQL real)
- ✅ Existe `RecursoControllerIT` (test de integración básico)
- ❌ **NO existe** `CompraIntegrationTest`

**Impacto:** **CRÍTICO** — Flujo crítico (Compra Directa) sin validación de integración.

**Estado:** 🔴 **FALLO CRÍTICO** — Test crítico NO implementado.

---

## 📉 CONCLUSIÓN DE AUDITORÍA

### ¿El código actual cumple con la "Directiva Maestra v2.0"? 

**NO** — Cumplimiento parcial (60%)

**Desglose:**
- ✅ **FASE 1 (Fundamentos & Seguridad):** 80% cumplido
  - ✅ FIX-01: Hardening DB — CUMPLIDO
  - 🟡 FIX-02: Spring Security — PARCIAL (JWT sin validación real)
  - ✅ FIX-03: Gestión Secretos — CUMPLIDO
- ❌ **FASE 2 (Integridad de Dominio):** 0% cumplido
  - ❌ DOM-01: WBS Partida — NO CUMPLIDO (SQL sí, lógica no)
  - ❌ DOM-02: APU — NO INICIADO
  - 🟡 DOM-03: Movimiento Caja — PARCIAL (lógica sí, garantía transaccional no)
- ❌ **FASE 3 (Testing):** 50% cumplido
  - ✅ QA-01: Infraestructura Test — CUMPLIDO
  - ❌ QA-02: Test Crítico Compra — NO CUMPLIDO

### ¿Es seguro desplegar esto ahora mismo?

**NO** — **RIESGO CRÍTICO**

**Razones:**
1. 🔴 **JWT Filter vulnerable:** Acepta cualquier token como válido
2. 🔴 **Arquitectura violada:** Puertos en capa incorrecta
3. 🔴 **WBS no funcional:** Estructura SQL correcta pero lógica de dominio ausente
4. 🔴 **Sin tests críticos:** Flujo de Compra no validado

---

## 🛠️ ACCIONES OBLIGATORIAS ANTES DE DESPLIEGUE

### Prioridad CRÍTICA (Bloqueantes):

1. **Mover Puertos de Salida a Domain**
   - Mover `CompraRepository`, `InventarioRepository`, `OutboxEventRepository` de `application/` a `domain/finanzas/compra/port/out/`
   - Mover `RecursoRepository` de `application/` a `domain/recurso/port/out/`
   - Actualizar imports en todos los UseCases

2. **Implementar Validación Real de JWT**
   - Usar `jjwt` para validar firma con secret key
   - Validar expiración (`exp` claim)
   - Extraer `userId` real del claim `sub` o `userId`
   - Rechazar tokens inválidos/expirados

3. **Implementar Lógica WBS en Partida**
   - Agregar campo `parentId` (opcional) y `nivel` (int) al agregado
   - Implementar método `addSubPartida(Partida hijo)`
   - Implementar método `calcularTotalRollup()` (suma ascendente)
   - Actualizar `PartidaMapper` para mapear `parent_id` y `nivel`

4. **Crear CompraIntegrationTest**
   - Test que valide flujo completo: Crear Partida → Ingresar Saldo → Comprar → Verificar Saldo y Stock
   - Usar `AbstractIntegrationTest` (Testcontainers con PostgreSQL real)
   - Validar que saldo se rebaja y stock aumenta correctamente

### Prioridad ALTA (Recomendado):

5. **Garantizar Persistencia Transaccional de MovimientoCaja**
   - Modificar `BilleteraRepositoryAdapter` para persistir `movimientosNuevos` en la MISMA transacción que actualiza saldo
   - O usar evento de dominio que garantice atomicidad

---

## 📊 RESUMEN EJECUTIVO

**Estado General:** 🟡 **EN PROGRESO — NO LISTO PARA PRODUCCIÓN**

**Logros:**
- ✅ Base de datos hardened (CHECK constraints, WBS estructura)
- ✅ Variables de entorno configuradas
- ✅ Infraestructura de testing configurada

**Fallas Críticas:**
- 🔴 JWT sin validación real (vulnerabilidad de seguridad)
- 🔴 Puertos en capa incorrecta (violación arquitectónica)
- 🔴 WBS sin lógica de dominio (estructura SQL correcta pero no funcional)
- 🔴 Sin test crítico de Compra (riesgo de regresiones)

**Recomendación:** **NO DESPLEGAR** hasta corregir las 4 fallas críticas identificadas.

---

**FIN DEL REPORTE FORENSE**
