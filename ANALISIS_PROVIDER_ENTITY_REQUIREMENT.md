# 🔍 ANÁLISIS: ¿Debe Provider Entity ser parte del Requerimiento?

**Fecha**: 2026-02-15  
**Objetivo**: Determinar si implementar entidad `Proveedor` debe ser parte de un requerimiento o es deuda técnica separada

---

## 📊 ESTADO ACTUAL

### Implementación Actual

#### 1. **Domain Model** (`Compra.java`)
```java
// Línea 38: Proveedor como String libre
private String proveedor;

// Línea 151-153: Método para actualizar proveedor (solo normaliza trim)
public void actualizarProveedor(String nuevoProveedor) {
    this.proveedor = normalizarProveedor(nuevoProveedor);
}

// Línea 106-111: Normalización mínima (solo trim)
private String normalizarProveedor(String proveedor) {
    if (proveedor == null || proveedor.isBlank()) {
        throw new IllegalArgumentException("El proveedor no puede estar vacío");
    }
    return proveedor.trim();
}
```

#### 2. **Database Schema** (`CompraEntity.java`)
```java
// Línea 44-45: Columna VARCHAR(200) sin foreign key
@Column(name = "proveedor", nullable = false, length = 200)
private String proveedor;
```

#### 3. **Validación Actual**
- ✅ **Obligatorio**: No puede ser null o vacío
- ✅ **Normalización**: Trim de espacios
- ❌ **Sin validación de existencia**: Cualquier string es válido
- ❌ **Sin validación de estado activo**: No verifica si el proveedor está activo
- ❌ **Sin integridad referencial**: No hay foreign key

---

## 🎯 IMPACTO DE LA DEUDA TÉCNICA

### Problemas Actuales

1. **Duplicación de Nombres**
   - "CEMEX S.A." vs "Cemex SA" vs "CEMEX" → Se tratan como proveedores diferentes
   - No hay normalización de nombres
   - Dificulta reportes y análisis

2. **Regla de Negocio No Implementada**
   - **L-04**: "Must purchase from active providers only" → 🔴 Missing
   - No se puede validar si un proveedor está activo
   - No se puede bloquear compras a proveedores inactivos

3. **Falta de Metadatos**
   - No hay RUC/NIT del proveedor
   - No hay información de contacto
   - No hay historial de compras por proveedor
   - No hay comparativo de precios entre proveedores

4. **Purchase Orders (UC-L03) Bloqueado**
   - El roadmap indica "Purchase Orders, Provider Mgmt" como siguiente fase
   - Sin entidad Provider, no se puede implementar:
     - Orden de Compra con proveedor específico
     - Validación de proveedor activo antes de enviar PO
     - Tracking de POs por proveedor

---

## 📋 ANÁLISIS: ¿Parte del Requerimiento o Deuda Técnica Separada?

### Argumentos para INCLUIRLO en el Requerimiento

#### ✅ **Bloquea Funcionalidades Críticas**

1. **Purchase Orders (UC-L03, P1)**
   - **Estado**: 🔴 Missing
   - **Dependencia**: Requiere entidad Provider para validar proveedor activo
   - **Impacto**: Sin Provider, no se puede implementar POs correctamente

2. **Regla de Negocio L-04**
   - **Estado**: 🔴 Missing
   - **Dependencia**: Requiere entidad Provider con campo `activo`
   - **Impacto**: No se puede validar "solo proveedores activos"

3. **Comparativo de Precios (Target 80%)**
   - **Dependencia**: Requiere entidad Provider para agrupar compras
   - **Impacto**: No se puede comparar precios entre proveedores

#### ✅ **Roadmap del Módulo**

```
**Next** | +1 Month | 60% | Purchase Orders (Orden de Compra), Provider Mgmt
```

El roadmap **explícitamente** incluye "Provider Mgmt" como parte de la siguiente fase.

#### ✅ **Technical Debt Documentado**

La documentación ya identifica esto como deuda técnica de **Alta Prioridad**:
```markdown
- [ ] **Free Text Providers**: Currently providers are just strings. Need `Proveedor` entity. (High)
```

---

### Argumentos para SEPARARLO como Deuda Técnica

#### ⚠️ **Funcionalidad Actual Funciona**

- ✅ Las compras directas funcionan con texto libre
- ✅ No bloquea el flujo core de compras (UC-L01, UC-L02)
- ✅ El módulo está en 40% (funcional para MVP)

#### ⚠️ **Scope Creep**

- Si el requerimiento es solo "mejorar compras", Provider podría ser scope creep
- Podría ser un requerimiento separado: "REQ-XX: Provider Management Module"

---

## 🎯 RECOMENDACIÓN

### ✅ **SÍ, debe ser parte del requerimiento si:**

1. **El requerimiento incluye Purchase Orders (UC-L03)**
   - Provider es **prerequisito** para POs
   - Sin Provider, no se puede validar proveedor activo antes de enviar PO

2. **El requerimiento busca alcanzar 60%+ maturity**
   - El roadmap indica "Provider Mgmt" como parte de la fase Next (60%)
   - Es necesario para cumplir la regla L-04

3. **El requerimiento busca mejorar integridad de datos**
   - Provider entity elimina duplicación de nombres
   - Permite validación de proveedores activos

### ❌ **NO, debe ser deuda técnica separada si:**

1. **El requerimiento es solo "mejorar compras directas"**
   - Las compras directas funcionan con texto libre
   - Provider no es crítico para el flujo actual

2. **El requerimiento tiene scope limitado**
   - Si el requerimiento es pequeño, Provider podría ser demasiado

---

## 📊 PROPUESTA DE IMPLEMENTACIÓN

### Si se incluye en el Requerimiento:

#### **Fase 1: Provider Entity (Prerequisito)**
1. Crear `Proveedor` domain entity
   - `ProveedorId`, `nombre`, `ruc`, `activo`, `contacto`
2. Crear `ProveedorRepository` port
3. Crear `ProveedorEntity` JPA
4. Migración: `VXX__create_proveedor_table.sql`
5. Use Cases: `CrearProveedor`, `ConsultarProveedores`, `Activar/DesactivarProveedor`

#### **Fase 2: Integración con Compra**
1. Modificar `Compra` para usar `ProveedorId` en lugar de `String`
2. Migración: Agregar `proveedor_id` FK a `compra` table
3. Migración de datos: Normalizar proveedores existentes
4. Validación: Solo permitir proveedores activos (L-04)

#### **Fase 3: Purchase Orders**
1. Implementar `OrdenCompra` con referencia a `Proveedor`
2. Validar proveedor activo antes de enviar PO

---

## 📈 IMPACTO EN MATURITY

### Sin Provider Entity:
- **Maturity actual**: 40% (Direct Purchase)
- **Maturity máxima alcanzable**: ~45% (mejoras menores)
- **Bloqueado**: Purchase Orders, Provider validation

### Con Provider Entity:
- **Maturity alcanzable**: 60%+ (Purchase Orders + Provider Mgmt)
- **Habilita**: Regla L-04, UC-L03, Comparativo de precios

---

## ✅ CONCLUSIÓN

### **Recomendación: SÍ, incluir Provider Entity si el requerimiento incluye:**

1. ✅ **Purchase Orders (UC-L03)** - Provider es prerequisito
2. ✅ **Alcanzar 60%+ maturity** - Provider Mgmt está en el roadmap
3. ✅ **Implementar regla L-04** - Requiere entidad Provider

### **Alternativa: Separar en dos requerimientos**

1. **REQ-A**: Provider Management Module (entidad base)
2. **REQ-B**: Purchase Orders + Integración Provider (depende de REQ-A)

---

**Generado por**: Análisis de deuda técnica vs. requerimiento  
**Fecha**: 2026-02-15
