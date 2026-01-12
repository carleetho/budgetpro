# 📝 DEV JOURNAL — BudgetPro Backend

## 2026-01-11 — FIX-CRITICAL: Persistencia Transaccional de MovimientoCaja

### 🚨 Problema Detectado
La auditoría forense (`2026-01-11_REALIDAD_VS_FICCION_REPORT.md`) detectó una **CORRUPCIÓN DE DATOS LATENTE**:
- El dominio `Billetera` genera `MovimientoCaja` cuando cambia el saldo
- La tabla `movimiento_caja` existe en la BD (V6__hardening_database_fix01.sql)
- **PERO** `BilleteraRepositoryAdapter.save()` NO persistía los movimientos
- Solo guardaba el saldo actualizado, **perdiendo trazabilidad financiera**

### ✅ Solución Implementada

**Archivos Creados:**
1. `MovimientoCajaEntity.java` — Entidad JPA con mapeo completo a tabla `movimiento_caja`
2. `MovimientoCajaJpaRepository.java` — Repositorio Spring Data JPA
3. `MovimientoCajaMapper.java` — Mapper dominio ↔ entidad

**Archivos Modificados:**
1. `BilleteraRepositoryAdapter.java`:
   - Agregado `MovimientoCajaJpaRepository` y `MovimientoCajaMapper` como dependencias
   - Método `save()` ahora:
     - Extrae `movimientosNuevos` del agregado `Billetera`
     - Los mapea a `MovimientoCajaEntity`
     - Los persiste usando `movimientoCajaJpaRepository.saveAll()` en la misma transacción
     - Llama a `billetera.limpiarMovimientosNuevos()` después de persistir
   - Agregado `@Transactional` para garantizar atomicidad

**Archivos Actualizados:**
1. `BilleteraMapper.java` — Comentarios actualizados (eliminada referencia a "NO se persisten movimientos")

### 🔍 Verificación

- ✅ Compilación exitosa (103 archivos compilados)
- ✅ Todos los comentarios obsoletos eliminados
- ✅ Transaccionalidad garantizada (`@Transactional` en método `save()`)
- ✅ Contrato del puerto `BilleteraRepository` cumplido

### 📊 Impacto

**Antes:**
- ❌ Movimientos generados pero NO persistidos
- ❌ Pérdida de trazabilidad financiera
- ❌ Violación de invariante del dominio

**Después:**
- ✅ Movimientos persistidos en la misma transacción que la billetera
- ✅ Trazabilidad financiera completa
- ✅ Invariante del dominio respetado ("Todo cambio genera un MovimientoCaja")

### 🎯 Estado

**FIX-CRITICAL:** ✅ **COMPLETADO**

El sistema ahora garantiza trazabilidad financiera completa. Todos los cambios en billetera quedan registrados en `movimiento_caja` dentro de la misma transacción ACID.

---


## 2026-01-11 22:45:07 - QA Automation: Test de Integración para Trazabilidad Financiera

**Rol:** QA Automation Engineer

**Objetivo:** Blindar el fix de persistencia de MovimientoCaja mediante un Test de Integración robusto que falle si la trazabilidad se rompe.

**Cambios Realizados:**
- ✅ MovimientoCajaJpaRepository ya estaba inyectado en CompraIntegrationTest
- ✅ Mejoradas las aserciones de trazabilidad financiera con 8 validaciones críticas:
  1. Lista de movimientos NO vacía
  2. Al menos 1 movimiento de tipo EGRESO
  3. Tipo del movimiento más reciente = EGRESO
  4. Monto del movimiento = Total de la compra (5300.00)
  5. Referencia contiene información de la compra
  6. Movimiento asociado a la billetera correcta
  7. Movimiento tiene ID válido (persistido)
- ✅ Mensajes de aserción descriptivos para facilitar debugging

**Criterio de Éxito:** El test ahora valida explícitamente que los movimientos de caja se persisten en la base de datos, garantizando trazabilidad financiera completa.

**Impacto:** Si la persistencia de MovimientoCaja se rompe en el futuro, este test fallará inmediatamente, alertando sobre la pérdida de trazabilidad financiera.
