# Motor de Cálculo Dinámico de APU

## Descripción General

El Motor de Cálculo Dinámico implementa fórmulas de ingeniería civil estándar para calcular costos unitarios de APUs (Análisis de Precios Unitarios) de forma dinámica, en lugar de usar precios estáticos almacenados en la base de datos.

Este sistema permite:
- **Cálculo en tiempo real** basado en fórmulas matemáticas
- **Normalización de unidades** antes de sumar cantidades
- **Recálculo automático** cuando cambian los parámetros (rendimiento, precios, etc.)
- **Validación de integridad** en presupuestos aprobados

## Arquitectura

El motor de cálculo está implementado como un servicio de dominio (`CalculoApuDinamicoService`) que no tiene dependencias de infraestructura, siguiendo los principios de Arquitectura Hexagonal.

```
┌─────────────────────────────────────────┐
│  APUSnapshot (Aggregate Root)           │
│  ┌───────────────────────────────────┐  │
│  │ calcularCostoTotal(service, moneda)│  │
│  └───────────────────────────────────┘  │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  CalculoApuDinamicoService              │
│  ┌───────────────────────────────────┐  │
│  │ calcularCostoTotalAPU()            │  │
│  │ calcularCostoInsumo()               │  │
│  │ calcularCostoDiaCuadrilla()        │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

## Fórmulas de Cálculo por Tipo de Recurso

### 1. MATERIAL

**Fórmula:**
```
Costo Unitario Material = Precio Puesto en Obra × Aporte Unitario × (1 + Desperdicio) × TipoCambio
```

**Parámetros:**
- `Precio Puesto en Obra`: Precio del material incluyendo flete y transporte
- `Aporte Unitario`: Cantidad de material necesaria por unidad de obra
- `Desperdicio`: Porcentaje de desperdicio (0.05 = 5%)
- `TipoCambio`: Factor de conversión de moneda si es necesario

**Ejemplo:**
```
Cemento:
- Precio: S/ 22.50/bolsa
- Aporte: 9.73 bolsas/m³
- Desperdicio: 5% (0.05)
- TipoCambio: 1.0 (PEN)

Cálculo:
22.50 × 9.73 × (1 + 0.05) × 1.0 = 229.87 S/ por m³
```

**Implementación:**
```java
private BigDecimal calcularCostoMaterial(APUInsumoSnapshot insumo, String monedaProyecto) {
    BigDecimal precioNormalizado = normalizarPrecioAMonedaProyecto(...);
    BigDecimal factorDesperdicio = BigDecimal.ONE.add(desperdicio);
    return precioNormalizado.multiply(aporteUnitario).multiply(factorDesperdicio);
}
```

### 2. MANO_OBRA

**Fórmula:**
```
Costo Unitario MO = (Costo Día Cuadrilla / Rendimiento Vigente) × Aporte Unitario
```

**Cálculo de Costo Día Cuadrilla:**
```
Costo Día Cuadrilla = Σ(Cantidad Personal × Costo Día Personal × TipoCambio)
```

**Parámetros:**
- `Costo Día Cuadrilla`: Suma de costos de todos los integrantes de la cuadrilla
- `Rendimiento Vigente`: Rendimiento editable por el ingeniero (m³/día, m²/día, etc.)
- `Aporte Unitario`: Cantidad de cuadrillas necesarias por unidad de obra

**Ejemplo con Cuadrilla Compuesta:**
```
Cuadrilla:
- 0.1 Capataz × S/ 120/día = S/ 12.00
- 1.0 Operario × S/ 80/día = S/ 80.00
- 2.0 Peones × S/ 60/día = S/ 120.00
Total: S/ 212.00/día

Rendimiento: 25 m³/día
Aporte: 1.0 cuadrilla

Cálculo:
(212 / 25) × 1.0 = 8.48 S/ por m³
```

**Implementación:**
```java
private BigDecimal calcularCostoManoObra(APUInsumoSnapshot insumo,
                                        BigDecimal rendimientoVigente,
                                        String monedaProyecto) {
    BigDecimal costoDiaCuadrilla = calcularCostoDiaCuadrilla(
        insumo.getComposicionCuadrilla(), monedaProyecto);
    return costoDiaCuadrilla
        .divide(rendimientoVigente, PRECISION_INTERMEDIA, ROUNDING_MODE)
        .multiply(aporteUnitario);
}
```

### 3. EQUIPO_MAQUINA

**Fórmula:**
```
Costo Unitario Equipo = Costo Hora Máquina × (Horas Uso / Rendimiento Vigente)
```

**Parámetros:**
- `Costo Hora Máquina`: Costo por hora de operación de la máquina
- `Horas Uso`: Horas de uso de la máquina por unidad de obra
- `Rendimiento Vigente`: Rendimiento editable por el ingeniero

**Ejemplo:**
```
Mezcladora:
- Costo Hora: S/ 45.00/hr
- Horas Uso: 0.5 hr
- Rendimiento: 25 m³/día (8 hrs jornada)

Cálculo:
45 × (0.5 / (25/8)) = 45 × 0.16 = 7.20 S/ por m³
```

**Implementación:**
```java
private BigDecimal calcularCostoEquipoMaquina(APUInsumoSnapshot insumo,
                                              BigDecimal rendimientoVigente,
                                              String monedaProyecto) {
    BigDecimal costoNormalizado = normalizarPrecioAMonedaProyecto(...);
    return costoNormalizado
        .multiply(horasUso)
        .divide(rendimientoVigente, PRECISION_INTERMEDIA, ROUNDING_MODE);
}
```

### 4. EQUIPO_HERRAMIENTA

**Fórmula:**
```
Costo Unitario Herramienta = Costo Total Mano de Obra × Porcentaje Herramienta
```

**Parámetros:**
- `Costo Total MO`: Suma de todos los costos de MANO_OBRA del APU
- `Porcentaje Herramienta`: Porcentaje del costo de MO (0.03 = 3%)

**⚠️ Orden de Dependencia:**
Este cálculo debe realizarse **DESPUÉS** de calcular todos los costos de MANO_OBRA, ya que depende de ellos.

**Ejemplo:**
```
Costo Total MO del APU: S/ 8.48/m³
% Herramienta: 3% (0.03)

Cálculo:
8.48 × 0.03 = 0.25 S/ por m³
```

**Implementación:**
```java
private BigDecimal calcularCostoEquipoHerramienta(APUInsumoSnapshot insumo,
                                                   BigDecimal costoTotalMO) {
    return costoTotalMO.multiply(porcentajeManoObra);
}
```

### 5. SUBCONTRATO

**Fórmula:**
```
Costo Unitario Subcontrato = Precio Puesto en Obra × TipoCambio
```

Los subcontratos usan precio fijo, normalizado a la moneda del proyecto.

## Orden de Cálculo y Dependencias

El cálculo del costo total del APU respeta un orden de dependencias:

```
1. MATERIAL (independiente)
   ↓
2. MANO_OBRA (independiente)
   ↓
3. EQUIPO_MAQUINA (independiente)
   ↓
4. EQUIPO_HERRAMIENTA (depende de paso 2)
   ↓
5. Sumar todos los costos
```

**Implementación del Algoritmo de Dos Pasadas:**

```java
public BigDecimal calcularCostoTotalAPU(APUSnapshot apuSnapshot, String monedaProyecto) {
    // Primera pasada: calcular independientes
    BigDecimal costoTotalMaterial = BigDecimal.ZERO;
    BigDecimal costoTotalMO = BigDecimal.ZERO;
    BigDecimal costoTotalEquipoMaquina = BigDecimal.ZERO;
    
    for (APUInsumoSnapshot insumo : insumos) {
        switch (tipoRecurso) {
            case MATERIAL:
                costoTotalMaterial += calcularCostoInsumo(...);
                break;
            case MANO_OBRA:
                costoTotalMO += calcularCostoInsumo(...);
                break;
            case EQUIPO_MAQUINA:
                costoTotalEquipoMaquina += calcularCostoInsumo(...);
                break;
        }
    }
    
    // Segunda pasada: calcular dependientes
    BigDecimal costoTotalHerramienta = BigDecimal.ZERO;
    for (APUInsumoSnapshot insumo : insumos) {
        if (tipoRecurso == EQUIPO_HERRAMIENTA) {
            costoTotalHerramienta += calcularCostoInsumo(..., costoTotalMO);
        }
    }
    
    return costoTotalMaterial + costoTotalMO + costoTotalEquipoMaquina + costoTotalHerramienta;
}
```

## Normalización de Unidades

### Sistema de Unidades

El sistema utiliza tres niveles de unidades:

1. **Unidad de Aporte**: Unidad en la que se especifica el aporte del insumo
   - Ejemplo: `bolsas/m³`, `kg/m²`, `horas/m³`

2. **Unidad Base**: Unidad común para normalizar antes de sumar
   - Ejemplo: `KG` (kilogramos), `HR` (horas), `M3` (metros cúbicos)

3. **Unidad de Compra**: Unidad en la que se compra el recurso
   - Ejemplo: `BOL` (bolsas), `TON` (toneladas), `HR` (horas)

### Factor de Conversión

```
Cantidad en Unidad Base = Cantidad en Unidad de Aporte × Factor de Conversión
```

**Ejemplo de Normalización:**
```
Cemento:
- Aporte: 9.73 bolsas/m³
- Factor: 42.5 kg/bolsa (1 bolsa = 42.5 kg)
- Unidad Base: KG

Cálculo:
9.73 bol × 42.5 kg/bol = 413.525 kg/m³
```

### Explosión de Insumos con Normalización

Al explotar insumos de un presupuesto completo:

1. **Calcular cantidad en unidad de aporte**: `metrado × aporteUnitario`
2. **Normalizar a unidad base**: `cantidad × factorConversionUnidadBase`
3. **Sumar todas las cantidades normalizadas** (mismo recurso, misma unidad base)
4. **Convertir a unidad de compra**: `cantidadBase / factorConversion`
5. **Redondear hacia arriba**: No puedes comprar 0.3 bolsas

**Ejemplo Completo:**
```
Partida A: 100 m³ × 9.73 bol/m³ × 42.5 kg/bol = 41,352.5 kg
Partida B: 50 m² × 0.5 kg/m² × 1.0 = 25 kg
Total Normalizado: 41,377.5 kg
En bolsas: 41,377.5 / 42.5 = 974.18 → 975 bolsas (redondeado hacia arriba)
```

## Precisión y Redondeo

El sistema utiliza diferentes niveles de precisión:

- **PRECISION_INTERMEDIA = 10**: Para cálculos intermedios
- **PRECISION_FINAL = 2**: Para resultados finales (moneda)
- **ROUNDING_MODE = HALF_UP**: Redondeo estándar

**Excepción:** Para cantidades de compra, se usa `RoundingMode.UP` (redondeo hacia arriba) porque no puedes comprar fracciones de unidades.

## Recálculo en Cascada

Cuando se modifica el rendimiento vigente de un APU:

1. Se actualiza el `rendimientoVigente` en el dominio
2. Se registra auditoría (quién, cuándo, valores anterior/nuevo)
3. Se recalcula automáticamente:
   - Costos de MANO_OBRA (dependen de rendimiento)
   - Costos de EQUIPO_MAQUINA (dependen de rendimiento)
   - Costos de EQUIPO_HERRAMIENTA (dependen de MO, que depende de rendimiento)
4. Si el presupuesto está aprobado:
   - Se valida la integridad
   - Se actualiza el hash de ejecución

## Validación de Integridad

En presupuestos aprobados:

- El hash de aprobación es **inmutable** (no cambia)
- El hash de ejecución se **actualiza** después de cada cambio que afecte costos
- Cualquier modificación dispara validación de integridad antes de permitir cambios

## Ejemplos de Uso

### Ejemplo 1: Cálculo de APU Completo

```java
APUSnapshot apu = // ... obtener APU
CalculoApuDinamicoService calculoService = // ... inyectar servicio
String monedaProyecto = "PEN";

BigDecimal costoTotal = apu.calcularCostoTotal(calculoService, monedaProyecto);
```

### Ejemplo 2: Actualizar Rendimiento

```java
ActualizarRendimientoUseCase useCase = // ... inyectar use case
UUID apuSnapshotId = // ... ID del APU
BigDecimal nuevoRendimiento = new BigDecimal("30.00");
UUID usuarioId = // ... ID del usuario

useCase.actualizarRendimiento(apuSnapshotId, nuevoRendimiento, usuarioId);
// El costo se recalcula automáticamente
```

### Ejemplo 3: Explosión de Insumos

```java
ExplotarInsumosPresupuestoUseCase useCase = // ... inyectar use case
UUID presupuestoId = // ... ID del presupuesto

ExplosionInsumosResponse explosion = useCase.ejecutar(presupuestoId);
// explosion.recursosPorTipo() contiene recursos agrupados por tipo
```

## Referencias

- **Requerimiento**: REQ-2: Motor de Cálculo y Explosión de Insumos con Validación de Presupuesto
- **Servicio de Dominio**: `CalculoApuDinamicoService`
- **Casos de Uso**: `ActualizarRendimientoUseCase`, `ExplotarInsumosPresupuestoUseCase`
- **Modelo de Dominio**: `APUSnapshot`, `APUInsumoSnapshot`, `ComposicionCuadrillaSnapshot`
