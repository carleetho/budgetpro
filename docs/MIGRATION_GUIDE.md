# Guía de Migración: APUs Legacy a Cálculo Dinámico

## Introducción

Este documento describe cómo migrar APUs existentes (legacy) que usan cálculo estático al nuevo sistema de cálculo dinámico basado en fórmulas de ingeniería civil.

## Contexto

### Sistema Legacy (Antes)

Los APUs legacy almacenaban:
- `subtotal` calculado previamente y almacenado en base de datos
- Sin información de tipo de recurso (`tipoRecurso = null`)
- Sin campos de cálculo dinámico (desperdicio, cuadrilla, etc.)

### Sistema Nuevo (Después)

Los APUs nuevos incluyen:
- Campos de clasificación (`tipoRecurso`, `ordenCalculo`)
- Campos de unidades (`unidadBase`, `factorConversionUnidadBase`, etc.)
- Campos específicos por tipo (desperdicio, cuadrilla, horas uso, etc.)
- Cálculo dinámico usando fórmulas

## Compatibilidad hacia Atrás

El sistema mantiene **compatibilidad completa** con APUs legacy:

### 1. Factory Methods Legacy

```java
// Método legacy (sigue funcionando)
APUInsumoSnapshot insumo = APUInsumoSnapshot.crear(
    id, recursoId, nombre, cantidad, precioUnitario
);

// Método nuevo (recomendado)
APUInsumoSnapshot insumo = APUInsumoSnapshot.crear(
    id, recursoId, nombre, cantidad, precioUnitario,
    TipoRecurso.MATERIAL, 1, aporteUnitario, unidadAporte,
    unidadBase, factorConversion, unidadCompra, moneda,
    tipoCambio, precioMercado, flete, precioPuestoEnObra,
    desperdicio, composicionCuadrilla, costoDiaCuadrilla,
    jornadaHoras, costoHoraMaquina, horasUso, porcentajeMO, dependeDe
);
```

### 2. Cálculo Legacy

```java
// Método legacy (sigue funcionando)
BigDecimal costo = apuSnapshot.calcularCostoTotal();
// Usa subtotal almacenado × rendimiento

// Método nuevo (recomendado)
BigDecimal costo = apuSnapshot.calcularCostoTotal(calculoService, monedaProyecto);
// Usa fórmulas dinámicas
```

### 3. Detección Automática

El servicio de cálculo detecta automáticamente si un insumo es legacy:

```java
if (insumo.getTipoRecurso() == null) {
    // Backward compatibility: usar cálculo simple
    return insumo.getSubtotal();
}
```

## Estrategias de Migración

### Estrategia 1: Migración Gradual (Recomendada)

Migrar APUs conforme se necesiten actualizar o crear nuevos.

**Ventajas:**
- ✅ Sin downtime
- ✅ Sin riesgo de romper APUs existentes
- ✅ Permite validar migración con datos reales

**Pasos:**

1. **Identificar APUs a migrar**
   ```sql
   SELECT * FROM apu_snapshot 
   WHERE id IN (
       SELECT DISTINCT apu_snapshot_id 
       FROM apu_insumo_snapshot 
       WHERE tipo_recurso IS NULL
   );
   ```

2. **Para cada APU legacy:**
   - Identificar tipo de recurso de cada insumo
   - Completar campos faltantes (unidad base, factor conversión, etc.)
   - Actualizar usando el nuevo factory method

3. **Validar cálculo:**
   ```java
   BigDecimal costoLegacy = apuSnapshot.calcularCostoTotal();
   BigDecimal costoNuevo = apuSnapshot.calcularCostoTotal(calculoService, "PEN");
   // Comparar y validar que sean similares (dentro de tolerancia)
   ```

### Estrategia 2: Migración Masiva

Migrar todos los APUs de una vez usando un script de migración.

**⚠️ Requisitos:**
- Backup completo de la base de datos
- Ventana de mantenimiento
- Validación exhaustiva después de la migración

**Script de Ejemplo:**

```java
@Service
public class ApuMigrationService {
    
    public void migrarApuLegacy(UUID apuSnapshotId) {
        APUSnapshot apu = apuRepository.findById(apuSnapshotId).orElseThrow();
        
        for (APUInsumoSnapshot insumo : apu.getInsumos()) {
            if (insumo.getTipoRecurso() == null) {
                // Determinar tipo basado en datos disponibles
                TipoRecurso tipo = inferirTipoRecurso(insumo);
                
                // Crear nuevo insumo con todos los campos
                APUInsumoSnapshot nuevoInsumo = migrarInsumo(insumo, tipo);
                
                // Reemplazar en el APU
                // ... (requiere reconstruir el APU completo)
            }
        }
    }
    
    private TipoRecurso inferirTipoRecurso(APUInsumoSnapshot insumo) {
        // Lógica de inferencia basada en:
        // - Nombre del recurso
        // - Catálogo externo
        // - Patrones conocidos
        // Por defecto: MATERIAL
        return TipoRecurso.MATERIAL;
    }
}
```

## Mapeo de Campos Legacy → Nuevos

### MATERIAL

**Campos Legacy:**
- `cantidad`: Cantidad del insumo
- `precioUnitario`: Precio unitario
- `subtotal`: `cantidad × precioUnitario`

**Campos Nuevos Requeridos:**
- `tipoRecurso`: `MATERIAL`
- `aporteUnitario`: Usar `cantidad` legacy
- `unidadAporte`: Inferir del catálogo o usar "UN"
- `unidadBase`: Inferir del catálogo
- `factorConversionUnidadBase`: `1.0` si misma unidad
- `unidadCompra`: Inferir del catálogo
- `precioPuestoEnObra`: Usar `precioUnitario` legacy
- `desperdicio`: `0.0` por defecto (sin desperdicio)

**Ejemplo de Migración:**
```java
APUInsumoSnapshot legacy = // ... insumo legacy

APUInsumoSnapshot nuevo = APUInsumoSnapshot.crear(
    legacy.getId(),
    legacy.getRecursoExternalId(),
    legacy.getRecursoNombre(),
    legacy.getCantidad(),
    legacy.getPrecioUnitario(),
    TipoRecurso.MATERIAL,  // ← Nuevo
    1,                      // ← Nuevo
    legacy.getCantidad(),   // aporteUnitario = cantidad legacy
    "UN",                   // unidadAporte (inferir del catálogo)
    "UN",                   // unidadBase (inferir del catálogo)
    BigDecimal.ONE,         // factorConversion
    "UN",                   // unidadCompra (inferir del catálogo)
    "PEN",                  // moneda (inferir del proyecto)
    BigDecimal.ONE,         // tipoCambio
    legacy.getPrecioUnitario(), // precioMercado
    BigDecimal.ZERO,        // flete
    legacy.getPrecioUnitario(), // precioPuestoEnObra
    BigDecimal.ZERO,        // desperdicio (0% por defecto)
    null,                   // composicionCuadrilla
    null,                   // costoDiaCuadrillaCalculado
    null,                   // jornadaHoras
    null,                   // costoHoraMaquina
    null,                   // horasUso
    null,                   // porcentajeManoObra
    null                    // dependeDe
);
```

### MANO_OBRA

**Campos Nuevos Requeridos:**
- `tipoRecurso`: `MANO_OBRA`
- `composicionCuadrilla`: Obtener del catálogo externo
- `jornadaHoras`: `8` por defecto
- `aporteUnitario`: Usar `cantidad` legacy

**⚠️ Importante:** Para MANO_OBRA, es **crítico** obtener la composición de cuadrilla del catálogo externo, ya que el cálculo depende de ella.

### EQUIPO_MAQUINA

**Campos Nuevos Requeridos:**
- `tipoRecurso`: `EQUIPO_MAQUINA`
- `costoHoraMaquina`: Obtener del catálogo
- `horasUso`: Obtener del catálogo o inferir

**Nota:** El enum `EQUIPO` está deprecado. Migrar a `EQUIPO_MAQUINA`.

### EQUIPO_HERRAMIENTA

**Campos Nuevos Requeridos:**
- `tipoRecurso`: `EQUIPO_HERRAMIENTA`
- `porcentajeManoObra`: Obtener del catálogo (típicamente 0.03 = 3%)
- `dependeDe`: `"MANO_OBRA"`

## Validación Post-Migración

### 1. Validar Cálculos

```java
@Test
void validarMigracionApu() {
    APUSnapshot apuLegacy = // ... cargar APU legacy
    APUSnapshot apuMigrado = migrarApu(apuLegacy);
    
    BigDecimal costoLegacy = apuLegacy.calcularCostoTotal();
    BigDecimal costoMigrado = apuMigrado.calcularCostoTotal(calculoService, "PEN");
    
    // Tolerancia del 1% para diferencias de redondeo
    BigDecimal diferencia = costoMigrado.subtract(costoLegacy).abs();
    BigDecimal tolerancia = costoLegacy.multiply(new BigDecimal("0.01"));
    
    assertThat(diferencia).isLessThan(tolerancia);
}
```

### 2. Validar Integridad de Datos

```sql
-- Verificar que todos los insumos migrados tengan tipoRecurso
SELECT COUNT(*) FROM apu_insumo_snapshot 
WHERE tipo_recurso IS NULL;

-- Debe ser 0 después de migración completa
```

### 3. Validar Rendimiento

```java
// Comparar tiempos de cálculo
long tiempoLegacy = medirTiempo(() -> apuLegacy.calcularCostoTotal());
long tiempoNuevo = medirTiempo(() -> 
    apuMigrado.calcularCostoTotal(calculoService, "PEN")
);

// El cálculo nuevo puede ser ligeramente más lento pero aceptable
assertThat(tiempoNuevo).isLessThan(tiempoLegacy * 2);
```

## Checklist de Migración

- [ ] Backup completo de base de datos
- [ ] Identificar todos los APUs legacy
- [ ] Crear script de migración
- [ ] Ejecutar migración en ambiente de pruebas
- [ ] Validar cálculos (legacy vs nuevo)
- [ ] Validar integridad de datos
- [ ] Ejecutar tests de regresión
- [ ] Documentar APUs migrados
- [ ] Ejecutar migración en producción
- [ ] Monitorear errores post-migración

## Rollback

Si es necesario revertir la migración:

1. **Restaurar backup** de base de datos
2. **O** ejecutar script de rollback que:
   - Establece `tipoRecurso = NULL` en insumos migrados
   - Restaura campos legacy si fueron eliminados

**⚠️ Nota:** El sistema mantiene compatibilidad hacia atrás, por lo que los APUs legacy seguirán funcionando sin migración.

## Soporte

Para dudas o problemas durante la migración:
- Revisar `CALCULO_DINAMICO.md` para entender las fórmulas
- Consultar tests de migración en `ApuMigrationTest`
- Contactar al equipo de desarrollo

## Referencias

- **Requerimiento**: REQ-2: Motor de Cálculo y Explosión de Insumos
- **Documentación de Fórmulas**: `CALCULO_DINAMICO.md`
- **Tests de Migración**: `backend/src/test/java/com/budgetpro/infrastructure/migration/`
