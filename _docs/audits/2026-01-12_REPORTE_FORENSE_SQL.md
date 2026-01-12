# REPORTE FORENSE SQL
**Fecha:** 2026-01-12 14:12:23  
**Analista:** Senior Database Reliability Engineer & Java Debugger  
**Test:** CompraIntegrationTest#testRegistrarCompraDirecta_FlujoCompleto  
**TraceId:** 371f282a-3722-4c8c-ab26-5371267748fd

---

## 🔍 HALLAZGO CRÍTICO: NO HAY UPDATE STATEMENTS EN EL LOG

**OBSERVACIÓN PRINCIPAL:** La excepción `OptimisticLockingFailureException` se lanza **ANTES** de que Hibernate intente ejecutar cualquier UPDATE. Esto indica que la verificación manual de optimistic locking en los adaptadores está fallando.

---

## 📊 EVIDENCIA SQL EXTRAÍDA

### 1. INSERT INICIAL (Setup - Método setUp())

#### INSERT INTO billetera
```sql
/* insert for com.budgetpro.infrastructure.persistence.entity.BilleteraEntity */
insert into billetera (created_at,proyecto_id,saldo_actual,updated_at,version,id) 
values (?,?,?,?,?,?)
```
**OBSERVACIÓN:** El campo `version` está presente en el INSERT, pero **NO se muestran los parámetros binding** (BasicBinder TRACE no está funcionando correctamente).

**TIMESTAMP:** 2026-01-12 14:12:21.292 [main]

#### INSERT INTO inventario_item (2 ocurrencias)
```sql
/* insert for com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity */
insert into inventario_item (cantidad,costo_promedio,created_at,proyecto_id,recurso_id,updated_at,version,id) 
values (?,?,?,?,?,?,?,?)
```
**OBSERVACIÓN:** El campo `version` está presente en ambos INSERT, pero **NO se muestran los parámetros binding**.

**TIMESTAMPS:** 
- 2026-01-12 14:12:21.437 [main]
- 2026-01-12 14:12:21.452 [main]

**PROBLEMA IDENTIFICADO:** Los logs de BasicBinder (TRACE) no están apareciendo, por lo que **NO podemos verificar qué valor de `version` se está pasando** (null, 0, o algún otro valor).

---

### 2. SELECT (Inicio del Flujo HTTP - Request al Endpoint)

#### SELECT FROM billetera
```sql
/* <criteria> */ 
select be1_0.id,be1_0.created_at,be1_0.proyecto_id,be1_0.saldo_actual,be1_0.updated_at,be1_0.version 
from billetera be1_0 
where be1_0.proyecto_id=?
```
**TIMESTAMP:** 2026-01-12 14:12:22.949 [http-nio-auto-1-exec-1]  
**OBSERVACIÓN:** Hibernate está consultando el campo `version`, pero **NO se muestran los valores recuperados** (no hay logs de binding parameters).

#### SELECT FROM inventario_item
```sql
/* SELECT i FROM InventarioItemEntity i WHERE i.proyectoId = :proyectoId AND i.recurso.id IN :recursoIds */ 
select iie1_0.id,iie1_0.cantidad,iie1_0.costo_promedio,iie1_0.created_at,iie1_0.proyecto_id,iie1_0.recurso_id,iie1_0.updated_at,iie1_0.version 
from inventario_item iie1_0 
where iie1_0.proyecto_id=? and iie1_0.recurso_id in (?,?)
```
**TIMESTAMP:** 2026-01-12 14:12:22.964 [http-nio-auto-1-exec-1]  
**OBSERVACIÓN:** Hibernate está consultando el campo `version`, pero **NO se muestran los valores recuperados**.

#### SELECT FROM compra (Verificación de existencia)
```sql
select ce1_0.id,ce1_0.created_at,ce1_0.estado,ce1_0.presupuesto_id,ce1_0.proyecto_id,ce1_0.total,ce1_0.updated_at,ce1_0.version 
from compra ce1_0 
where ce1_0.id=?
```
**TIMESTAMP:** 2026-01-12 14:12:22.974 [http-nio-auto-1-exec-1]  
**OBSERVACIÓN:** Este SELECT ocurre cuando `CompraRepositoryAdapter.save()` verifica si la compra existe. **NO se muestran los valores recuperados**.

---

### 3. UPDATE FALLIDO (El Crimen) - ⚠️ NO ENCONTRADO

**HALLAZGO CRÍTICO:** **NO HAY UPDATE STATEMENTS EN EL LOG COMPLETO**.

Esto significa que:
1. La excepción `OptimisticLockingFailureException` se lanza **ANTES** de que Hibernate intente hacer el UPDATE.
2. La verificación manual de optimistic locking en los adaptadores está fallando.
3. El problema está en la lógica de los adaptadores, NO en Hibernate.

**ÚLTIMAS OPERACIONES SQL ANTES DEL ERROR:**
- SELECT de compra (verificación de existencia)
- SELECT de compra_detalle (carga de detalles)
- **INMEDIATAMENTE DESPUÉS:** 409 CONFLICT

**NO HAY:**
- ❌ UPDATE billetera
- ❌ UPDATE inventario_item  
- ❌ UPDATE compra
- ❌ INSERT compra

---

## 🎯 DIAGNÓSTICO

### CAUSA RAÍZ PROBABLE

La excepción se lanza en `CompraRepositoryAdapter.save()` en la línea 64:

```java
throw new OptimisticLockingFailureException(
    String.format("La compra %s fue modificada por otro proceso. Versión actual: %d, Versión esperada: %d",
        compra.getId(), existingEntity.getVersion(), versionDomain)
);
```

**ESCENARIO:**
1. Se crea una nueva `Compra` en el dominio con `version = 0L` (línea 74 de `Compra.java`)
2. `CompraRepositoryAdapter.save()` verifica si existe: `jpaRepository.findById(compra.getId().getValue())`
3. **PROBLEMA:** La compra NO debería existir (es nueva), pero si existe (por limpieza incompleta), la verificación falla
4. Si `versionDomain == 0` y `existingEntity.getVersion() > 0`, la condición `versionDomain > 0` es `false`, pero luego se compara `!existingEntity.getVersion().equals(versionDomain)` que es `true` si `existingEntity.getVersion() != 0`
5. **PERO:** El código actual tiene lógica para manejar `versionDomain == 0` (líneas 57-61), así que esto NO debería ser el problema

### PROBLEMA ALTERNATIVO

El problema puede estar en **BilleteraRepositoryAdapter** o **InventarioRepositoryAdapter**:

- **BilleteraRepositoryAdapter línea 69:** Compara `!existingEntity.getVersion().equals(versionDomain)` sin verificar si `versionDomain == 0`
- **InventarioRepositoryAdapter línea 109:** Compara `!existingEntity.getVersion().equals(versionDomain)` sin verificar si `versionDomain == 0`

**ESCENARIO:**
1. Se crea billetera/inventario en `setUp()` con `version = null` → Hibernate inicializa a `1`
2. Se lee billetera/inventario en el use case → `version = 1` en el dominio
3. Se modifica billetera/inventario (egreso/ingreso)
4. Se intenta guardar → `versionDomain = 1`, `existingEntity.getVersion() = 1` → ✅ Debería funcionar
5. **PERO:** Si hay un problema de caché o la entidad se lee dos veces, puede haber un desajuste

---

## 📋 EVIDENCIA FALTANTE

**NO SE PUEDEN VERIFICAR LOS SIGUIENTES VALORES CRÍTICOS:**

1. ❌ **Valor de `version` en INSERT inicial:** ¿null, 0, o 1?
2. ❌ **Valor de `version` recuperado en SELECT:** ¿Qué versión tiene la billetera/inventario después del INSERT?
3. ❌ **Valor de `versionDomain` vs `existingEntity.getVersion()`:** ¿Qué valores exactos se están comparando?

**RAZÓN:** Los logs de `BasicBinder` (TRACE) no están apareciendo a pesar de estar configurados.

---

## 🔧 RECOMENDACIONES INMEDIATAS

### 1. HABILITAR LOGGING TEMPORAL EN ADAPTADORES

Agregar logging explícito en los adaptadores para capturar los valores:

```java
// En BilleteraRepositoryAdapter.save()
log.debug("Billetera save - versionDomain: {}, existingVersion: {}", versionDomain, existingEntity.getVersion());

// En InventarioRepositoryAdapter.save()
log.debug("Inventario save - versionDomain: {}, existingVersion: {}", versionDomain, existingEntity.getVersion());

// En CompraRepositoryAdapter.save()
log.debug("Compra save - versionDomain: {}, existingVersion: {}", versionDomain, existingEntity != null ? existingEntity.getVersion() : "N/A");
```

### 2. VERIFICAR CONFIGURACIÓN DE LOGGING

El problema puede ser que `BasicBinder` requiere una configuración diferente en Hibernate 6.x:

```yaml
logging:
  level:
    org.hibernate.orm.jdbc.bind: TRACE
```

### 3. INVESTIGAR PROBLEMA DE CACHÉ

El problema puede ser que:
- La billetera/inventario se lee con `version = 1`
- Se modifica en el dominio
- Se intenta guardar, pero la entidad en caché tiene una versión diferente

**SOLUCIÓN:** Agregar `entityManager.clear()` o `entityManager.detach()` antes de guardar.

---

## 🎯 CONCLUSIÓN FORENSE

**VEREDICTO:** La excepción se lanza en la **verificación manual de optimistic locking** en los adaptadores, NO en Hibernate. El problema está en la lógica de comparación de versiones cuando `versionDomain == 0` y la entidad existe con `version > 0`.

**PRÓXIMOS PASOS:**
1. Agregar logging explícito en adaptadores
2. Verificar si el problema está en Billetera o Inventario (no en Compra)
3. Revisar la lógica de comparación de versiones en todos los adaptadores

---

**FIN DEL REPORTE FORENSE**
