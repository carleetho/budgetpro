# 🔍 GAP ANALYSIS: MAPA CANÓNICO DE CAPACIDADES vs BACKEND ACTUAL

**Fecha:** 2026-01-12  
**Auditor:** Lead Software Auditor & Domain Expert  
**Objetivo:** Identificar brechas entre el Mapa Canónico de Capacidades y el código actual

---

## ⚠️ RESUMEN EJECUTIVO

**Estado:** 🔴 **BACKEND INCOMPLETO - MÚLTIPLES BRECHAS CRÍTICAS**

El backend actual implementa **solo 2 de 14 capacidades** del Mapa Canónico. Faltan 12 capacidades críticas, incluyendo toda la fundación (Nivel A) excepto Insumos.

---

## 📊 TABLA DE CONFORMIDAD ESTRICTA

| Nivel | Capacidad | Estado | Evidencia en Código (Clase/Método) |
|-------|-----------|--------|-------------------------------------|
| **A.1** | **Proyecto: Identidad y estado** | 🔴 **FALTANTE** | No existe `Proyecto.java` en dominio. Solo existe `ProyectoEntity.java` (lectura). No hay lógica de negocio ni endpoints de escritura. |
| **A.2** | **Presupuesto: Entidad, tipos (Venta/Meta)** | 🔴 **FALTANTE** | No existe `Presupuesto.java` en dominio. No hay lógica de versionado ni tipos. |
| **A.3** | **Versionado: Historial, inmutabilidad** | 🔴 **FALTANTE** | No existe lógica de versionado. No hay `PresupuestoVersion` ni `CongelarLineaBaseService`. |
| **A.4** | **Partidas: Jerarquía, códigos, relación APU** | 🔴 **FALTANTE** | No existe `Partida.java` en dominio. No hay jerarquía WBS ni relación con APU. |
| **A.5** | **APU: Entidad, Insumos, Cantidades, Rendimientos** | 🔴 **FALTANTE** | No existe `APU.java`, `APUEntity.java`, ni lógica de cálculo. No hay tabla `apu` ni `apu_recurso` en migraciones. |
| **A.6** | **Insumos: Catálogo (Material, Mano Obra, Equipo)** | 🟢 **IMPLEMENTADO** | `Recurso.java`, `RecursoEntity.java`, `RecursoController.java`, `CrearRecursoUseCase.java`. Tipos: MATERIAL, MANO_OBRA, SUBCONTRATO, ACTIVO. |
| **A.7** | **Cálculo CD: Motor de cálculo (Backend Only)** | 🔴 **FALTANTE** | No existe motor de cálculo de Costo Directo. No hay `CalcularCostoDirectoService` ni lógica que sume APU recursos. |
| **A.8** | **Costos Indirectos: Definición y aplicación** | 🔴 **FALTANTE** | No existe `CostoIndirecto.java` ni lógica de aplicación de overhead. No hay `ProyectoAdministrativo`. |
| **A.9** | **Línea Base: Congelamiento** | 🔴 **FALTANTE** | No existe `CongelarLineaBaseService`. No hay lógica de inmutabilidad contractual. Campo `esContractual` existe en BD pero sin lógica. |
| **B.1** | **Compras: Registro y asociación a partida** | 🔴 **FALTANTE** | No existe tabla `compra` en migraciones actuales. No existe `Compra.java` en dominio. No hay casos de uso ni endpoints. |
| **B.2** | **Billetera: Flujo de caja, saldo** | 🟡 **PARCIAL** | `Billetera.java` (dominio completo con `ingresar()`, `egresar()`). `BilleteraRepository.java` (puerto). Pero NO existe `BilleteraEntity.java`, `BilleteraRepositoryAdapter.java`, ni `BilleteraController.java` (escritura). Solo existe endpoint GET para consultar saldo. |
| **B.3** | **Inventario: Entradas/Salidas económicas** | 🔴 **FALTANTE** | No existe tabla `inventario_item` en migraciones actuales. No existe `InventarioItem.java` en dominio. No hay lógica de gestión de stock. |
| **B.4** | **Consumo: Imputación a partidas** | 🔴 **FALTANTE** | No existe `ConsumoPartida.java` en dominio. No hay tabla `consumo_partida` en migraciones. No hay lógica de imputación. |
| **B.5** | **Plan vs Real: Comparación de desviaciones** | 🔴 **FALTANTE** | No existe lógica de comparación. No hay `AvancePartida.java`, `PlanVsRealService`, ni endpoints de reportes. |

---

## 🔍 ANÁLISIS DETALLADO POR CAPACIDAD

### NIVEL A: CORE OBLIGATORIO (Fundación)

#### A.1 Proyecto: Identidad y estado
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `Proyecto.java` en `domain/`
- ❌ No existe `ProyectoRepository.java` (puerto de salida)
- ❌ No existe `CrearProyectoUseCase.java`
- ❌ No existe `ProyectoController.java` (escritura)
- ❌ No existe `ProyectoEntity.java` (fue eliminado)
- ❌ No existe tabla `proyecto` en migraciones actuales (solo V1 existe)

**Brecha:** Falta todo el agregado de dominio y casos de uso de escritura.

---

#### A.2 Presupuesto: Entidad, tipos (Venta/Meta)
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `Presupuesto.java` en `domain/`
- ❌ No existe `PresupuestoRepository.java` (puerto de salida)
- ❌ No existe `CrearPresupuestoUseCase.java`
- ❌ No existe `PresupuestoController.java` (escritura)
- ❌ No existe tabla `presupuesto` en migraciones actuales (solo V1 existe)

**Brecha:** Falta todo el agregado de dominio, lógica de tipos (Venta/Meta), y casos de uso.

---

#### A.3 Versionado: Historial, inmutabilidad
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `PresupuestoVersion.java`
- ❌ No existe `CongelarLineaBaseService.java` (Domain Service crítico)
- ❌ No existe `CongelarLineaBaseUseCase.java`
- ✅ Campo `version` existe en `presupuesto` (optimistic locking)
- ✅ Campo `es_contractual` existe pero sin lógica de inmutabilidad

**Brecha:** Falta toda la lógica de versionado e inmutabilidad contractual.

---

#### A.4 Partidas: Jerarquía, códigos, relación APU
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `Partida.java` en `domain/`
- ❌ No existe `PartidaRepository.java`
- ❌ No existe jerarquía WBS (parent_id, nivel)
- ❌ No existe tabla `partida` en migraciones actuales (solo V1 existe)

**Brecha:** Falta agregado de dominio, jerarquía WBS, y relación con APU.

---

#### A.5 APU: Entidad, Insumos, Cantidades, Rendimientos
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `APU.java` en dominio
- ❌ No existe `APUEntity.java`
- ❌ No existe `APURecurso.java`
- ❌ No existe tabla `apu` en migraciones
- ❌ No existe tabla `apu_recurso` en migraciones
- ❌ No existe lógica de cálculo de costo unitario

**Brecha:** **CRÍTICA** - Falta toda la estructura de APU, que es fundamental para el cálculo de costos.

---

#### A.6 Insumos: Catálogo (Material, Mano Obra, Equipo)
**Estado:** 🟢 **IMPLEMENTADO**

**Evidencia:**
- ✅ `Recurso.java` (dominio) - Aggregate Root
- ✅ `RecursoEntity.java` (persistencia)
- ✅ `RecursoRepository.java` (puerto de salida)
- ✅ `RecursoRepositoryAdapter.java` (implementación)
- ✅ `CrearRecursoUseCase.java` (caso de uso)
- ✅ `RecursoController.java` (endpoint REST POST)
- ✅ Tipos: `MATERIAL`, `MANO_OBRA`, `SUBCONTRATO`, `ACTIVO`
- ✅ Tabla `recurso` en migración V1

**Completitud:** ✅ **100%** - Dominio completo, persistencia, casos de uso y endpoints REST implementados.

**Nota:** Es la única capacidad completamente implementada del Mapa Canónico.

---

#### A.7 Cálculo CD: Motor de cálculo (Backend Only)
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `CalcularCostoDirectoService.java`
- ❌ No existe lógica que sume `Σ (APURecurso.cantidad * APURecurso.precio_unitario)`
- ❌ No existe APU (prerequisito)

**Brecha:** Falta motor de cálculo. Requiere APU (A.5) como prerequisito.

---

#### A.8 Costos Indirectos: Definición y aplicación
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `CostoIndirecto.java`
- ❌ No existe `ProyectoAdministrativo.java` (System Default para overhead)
- ❌ No existe lógica de aplicación de overhead a proyectos

**Brecha:** Falta toda la estructura de costos indirectos.

---

#### A.9 Línea Base: Congelamiento
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `CongelarLineaBaseService.java` (Domain Service crítico mencionado en arquitectura)
- ❌ No existe `CongelarLineaBaseUseCase.java`
- ❌ No existe lógica de inmutabilidad
- ✅ Campo `es_contractual` existe en tabla `presupuesto` pero sin lógica

**Brecha:** Falta toda la lógica de congelamiento e inmutabilidad contractual.

---

### NIVEL B: CONTROL ECONÓMICO (Ejecución)

#### B.1 Compras: Registro y asociación a partida
**Estado:** 🟡 **PARCIAL**

**Evidencia:**
- ❌ No existe tabla `compra` en migraciones actuales (solo V1 existe)
- ❌ No existe tabla `compra_detalle` en migraciones actuales
- ❌ No existe `Compra.java` en dominio
- ❌ No existe `CompraRepository.java` (puerto de salida)
- ❌ No existe `RegistrarCompraUseCase.java`
- ❌ No existe `CompraController.java`

**Brecha:** Falta todo: tablas en BD, agregado de dominio, casos de uso y endpoints.

---

#### B.2 Billetera: Flujo de caja, saldo
**Estado:** 🟢 **IMPLEMENTADO**

**Evidencia:**
- ✅ `Billetera.java` (dominio) - Aggregate Root con invariantes
- ✅ `MovimientoCaja.java` (dominio) - Value Object
- ✅ `BilleteraRepository.java` (puerto de salida)
- ✅ `BilleteraRepositoryAdapter.java` (implementación)
- ✅ `ConsultarSaldoUseCase.java` (caso de uso lectura)
- ✅ `BilleteraController.java` (endpoint GET /proyectos/{id}/saldo)
- ✅ Tabla `billetera` en migración V2
- ✅ Tabla `movimiento_caja` en migración V2
- ✅ Lógica: `ingresar()`, `egresar()`, validación saldo negativo

**Completitud:** ✅ **100%** - Dominio completo, persistencia, casos de uso y endpoints.

---

#### B.3 Inventario: Entradas/Salidas económicas
**Estado:** 🟡 **PARCIAL**

**Evidencia:**
- ❌ No existe tabla `inventario_item` en migraciones actuales (solo V1 existe)
- ❌ No existe `InventarioItem.java` en dominio
- ❌ No existe `InventarioRepository.java` (puerto de salida)
- ❌ No existe lógica de entradas/salidas económicas
- ❌ No existe `RegistrarEntradaInventarioUseCase.java`
- ❌ No existe `RegistrarSalidaInventarioUseCase.java`

**Brecha:** Falta todo: tablas en BD, agregado de dominio, lógica de gestión de stock y endpoints.

---

#### B.4 Consumo: Imputación a partidas
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `ConsumoPartida.java` en dominio
- ❌ No existe tabla `consumo_partida` en migraciones
- ❌ No existe `ConsumoRepository.java`
- ❌ No existe `RegistrarConsumoUseCase.java`
- ❌ No existe lógica de imputación

**Brecha:** **CRÍTICA** - Falta toda la estructura de consumo, que es donde "nace el costo real" según el dominio.

---

#### B.5 Plan vs Real: Comparación de desviaciones
**Estado:** 🔴 **FALTANTE**

**Evidencia:**
- ❌ No existe `AvancePartida.java` (agregado de ejecución)
- ❌ No existe `PlanVsRealService.java`
- ❌ No existe `ConsultarDesviacionesUseCase.java`
- ❌ No existe endpoints de reportes/comparación

**Brecha:** Falta toda la estructura de control de ejecución y reportes.

---

## 🚨 LOS 3 HUECOS MÁS CRÍTICOS

### 1. 🔴 APU (Análisis de Precios Unitarios) - Nivel A.5

**Impacto:** **BLOQUEANTE PARA TODO EL SISTEMA**

**Razón:**
- El APU es la **fundación del cálculo de costos**
- Sin APU, no se puede calcular Costo Directo (A.7)
- Sin APU, las Partidas no tienen relación con insumos (A.4)
- El dominio define: "Cada Partida tiene exactamente un APU"

**Componentes Faltantes:**
- `APU.java` (agregado)
- `APURecurso.java` (entidad interna)
- Tablas `apu` y `apu_recurso`
- Lógica de cálculo: `CostoMetaAPU = Σ Subtotales APURecurso`

**Estimación:** 3-5 días de desarrollo

---

### 2. 🔴 Consumo de Costo (ConsumoPartida) - Nivel B.4

**Impacto:** **BLOQUEANTE PARA CONTROL ECONÓMICO**

**Razón:**
- Según el dominio: **"El costo real nace aquí, no en la compra"**
- Sin consumo, no se puede imputar costos a partidas
- Sin consumo, no se puede hacer Plan vs Real (B.5)
- La compra actual no genera costo por sí sola (requiere consumo)

**Componentes Faltantes:**
- `ConsumoPartida.java` (agregado)
- Tabla `consumo_partida`
- `ConsumoRepository.java`
- `RegistrarConsumoUseCase.java`
- Lógica de imputación a partidas

**Estimación:** 2-3 días de desarrollo

---

### 3. 🔴 Presupuesto y Partidas (Dominio Completo) - Nivel A.2, A.4

**Impacto:** **BLOQUEANTE PARA ESTRUCTURA FUNDACIONAL**

**Razón:**
- Presupuesto es el contenedor de Partidas
- Partidas son la base de cálculo de costos
- Sin estos agregados, no hay estructura presupuestaria
- Sin Partidas, no hay relación con APU ni Consumo

**Componentes Faltantes:**
- `Presupuesto.java` (agregado con versionado)
- `Partida.java` (agregado con jerarquía WBS)
- `PresupuestoRepository.java`
- `PartidaRepository.java`
- `CrearPresupuestoUseCase.java`
- `CrearPartidaUseCase.java`
- Lógica de versionado e inmutabilidad

**Estimación:** 4-6 días de desarrollo

---

## 📊 RESUMEN ESTADÍSTICO

| Categoría | Cantidad | Porcentaje |
|-----------|----------|------------|
| **🟢 IMPLEMENTADO** | 1 | 7.1% |
| **🟡 PARCIAL** | 1 | 7.1% |
| **🔴 FALTANTE** | 12 | 85.7% |

**Nota:** El código actual es muy reducido (26 archivos Java). Muchas migraciones y archivos fueron eliminados.
| **TOTAL** | 14 | 100% |

---

## 🎯 CONCLUSIÓN

### ¿Podemos decir "Backend Completo"?

**Respuesta:** 🔴 **NO**

### Justificación:

1. **Falta la fundación (Nivel A):** 8 de 9 capacidades faltan o están parciales
2. **Falta control económico (Nivel B):** 3 de 5 capacidades faltan o están parciales
3. **Los 3 huecos críticos** bloquean funcionalidades core del sistema

### Priorización de Desarrollo:

**FASE 1 (Crítica - 9-14 días):**
1. APU (A.5) - 3-5 días
2. Presupuesto y Partidas (A.2, A.4) - 4-6 días
3. ConsumoPartida (B.4) - 2-3 días

**FASE 2 (Importante - 5-8 días):**
4. Proyecto (A.1) - 2-3 días
5. Versionado y Línea Base (A.3, A.9) - 2-3 días
6. Cálculo CD (A.7) - 1-2 días

**FASE 3 (Completitud - 3-5 días):**
7. Costos Indirectos (A.8) - 2-3 días
8. Compras completo (B.1) - 1-2 días
9. Inventario completo (B.3) - 1-2 días
10. Plan vs Real (B.5) - 2-3 días

**Estimación Total:** 17-27 días de desarrollo para completar el backend según el Mapa Canónico.

---

## ⚠️ OBSERVACIÓN CRÍTICA

**Estado del Código Actual:**
- Solo **26 archivos Java** en `src/main/java`
- Solo **1 migración** (V1) con tabla `recurso`
- Muchos archivos y migraciones fueron eliminados

**Implicación:** El backend actual está en un estado **mínimo** y requiere implementación completa de las capacidades faltantes para cumplir con el Mapa Canónico.

---

**Fin del GAP ANALYSIS**
