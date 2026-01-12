# 📋 REPORTE DE READINESS FRONTEND - WIREFLOW 1: COMPRA DIRECTA

**Fecha:** 2026-01-12  
**Auditor:** Lead Frontend Architect & UX Auditor  
**Objetivo:** Determinar si el Backend expone los endpoints necesarios para implementar el Wireflow 1 (Compra Directa) sin necesidad de mocks o "hacks"

---

## 🎯 RESUMEN EJECUTIVO

**VEREDICTO:** 🔴 **NO READY - FALTAN ENDPOINTS CRÍTICOS**

El backend actual tiene **3 endpoints bloqueantes faltantes** y **1 mejora necesaria** en la respuesta de compra para completar el Wireflow 1.

**Recomendación:** **NO iniciar desarrollo del Frontend** hasta completar los endpoints faltantes.

---

## 📊 MATRIZ DE REQUISITOS vs ENDPOINTS

### Wireflow 1: Compra Directa de Recursos

| # | Requisito del Wireflow | Endpoint Backend Detectado | ¿Datos Completos? | ¿Bloqueante? | Estado |
|---|------------------------|---------------------------|-------------------|--------------|--------|
| 1 | **Cargar Proyectos**<br/>*El usuario debe seleccionar un proyecto* | ❌ **NO EXISTE**<br/>Falta: `GET /api/v1/proyectos` | N/A | 🔴 **SÍ** | **BLOQUEANTE** |
| 2 | **Cargar Presupuesto/Partidas**<br/>*Seleccionar partida del presupuesto del proyecto* | ⚠️ **PARCIAL**<br/>Existe: `GET /api/v1/presupuestos/{presupuestoId}/partidas`<br/>❌ Falta: `GET /api/v1/proyectos/{proyectoId}/presupuestos` | ✅ Sí (partidas)<br/>❌ No (presupuestos) | 🔴 **SÍ** | **BLOQUEANTE** |
| 3 | **Buscar Recurso (Autocomplete)**<br/>*Búsqueda difusa de recursos para agregar a la compra* | ❌ **NO EXISTE**<br/>Solo existe: `POST /api/v1/recursos` (crear)<br/>Falta: `GET /api/v1/recursos?search=...` | N/A | 🔴 **SÍ** | **BLOQUEANTE** |
| 4 | **Enviar Compra (Payload)**<br/>*POST con proyectoId, presupuestoId, detalles* | ✅ **EXISTE**<br/>`POST /api/v1/compras/directa` | ✅ Sí<br/>Payload coincide con wireflow | 🟢 No | ✅ **READY** |
| 5 | **Feedback (Saldo/Stock)**<br/>*Mostrar nuevo saldo y stock después de la compra* | ⚠️ **INCOMPLETO**<br/>Respuesta actual: `{compraId, estado, mensajeUsuario}`<br/>❌ Falta: `saldoActual`, `stockActualizado` | ❌ No | 🔴 **SÍ** | **BLOQUEANTE** |

---

## 🔍 ANÁLISIS DETALLADO

### ✅ 1. Endpoint de Compra Directa (READY)

**Endpoint:** `POST /api/v1/compras/directa`

**Request Body:**
```json
{
  "proyectoId": "uuid",
  "presupuestoId": "uuid",
  "detalles": [
    {
      "recursoId": "uuid",
      "cantidad": 100.00,
      "precioUnitario": 15.50
    }
  ]
}
```

**Response Actual:**
```json
{
  "compraId": "uuid",
  "estado": "CONFIRMADA",
  "mensajeUsuario": null
}
```

**Estado:** ✅ **COMPLETO** - El payload coincide exactamente con los requisitos del wireflow.

---

### 🔴 2. Endpoint de Proyectos (BLOQUEANTE)

**Requisito del Wireflow:**
> "Selecciona Proyecto y Partida" - El usuario debe poder listar y seleccionar proyectos.

**Endpoint Faltante:**
```
GET /api/v1/proyectos
```

**Respuesta Esperada:**
```json
[
  {
    "id": "uuid",
    "nombre": "Proyecto X",
    "cliente": "Cliente Y",
    "estado": "EN_EJECUCION"
  }
]
```

**Impacto:** Sin este endpoint, el frontend no puede mostrar la lista de proyectos para que el usuario seleccione uno. **BLOQUEANTE para iniciar desarrollo.**

---

### 🔴 3. Endpoint de Presupuestos por Proyecto (BLOQUEANTE)

**Requisito del Wireflow:**
> "Selecciona Proyecto y Partida" - Después de seleccionar proyecto, debe mostrar presupuestos y partidas.

**Endpoints Existentes:**
- ✅ `GET /api/v1/presupuestos/{presupuestoId}/partidas` - Lista partidas de un presupuesto

**Endpoint Faltante:**
```
GET /api/v1/proyectos/{proyectoId}/presupuestos
```

**Respuesta Esperada:**
```json
[
  {
    "id": "uuid",
    "proyectoId": "uuid",
    "esContractual": true,
    "version": 1
  }
]
```

**Impacto:** Sin este endpoint, el frontend no puede:
1. Listar los presupuestos del proyecto seleccionado
2. Permitir al usuario elegir un presupuesto
3. Luego listar las partidas de ese presupuesto

**BLOQUEANTE para completar el flujo de selección.**

---

### 🔴 4. Endpoint de Búsqueda de Recursos (BLOQUEANTE)

**Requisito del Wireflow:**
> "Clasifica líneas" - El usuario debe buscar recursos mediante autocomplete para agregar a la compra.

**Endpoint Existente:**
- ✅ `POST /api/v1/recursos` - Crear recurso (solo creación)

**Endpoint Faltante:**
```
GET /api/v1/recursos?search={query}&tipo={tipo}&limit={limit}
```

**Respuesta Esperada:**
```json
[
  {
    "id": "uuid",
    "nombre": "CEMENTO GRIS",
    "tipo": "MATERIAL",
    "unidadBase": "KG",
    "estado": "ACTIVO"
  }
]
```

**Requisitos Técnicos (según wireflow):**
- Búsqueda difusa (similarity search)
- Filtro por tipo (opcional)
- Límite de resultados para autocomplete
- Índice `GIN + pg_trgm` recomendado (backend)

**Impacto:** Sin este endpoint, el frontend no puede:
1. Implementar autocomplete de recursos
2. Permitir al usuario buscar y seleccionar recursos para la compra
3. Validar si un recurso existe antes de agregarlo

**BLOQUEANTE para la funcionalidad core del wireflow.**

---

### 🔴 5. Feedback de Saldo/Stock en Respuesta (BLOQUEANTE)

**Requisito del Wireflow:**
> "Feedback (Saldo/Stock)" - Después de confirmar la compra, mostrar el nuevo saldo y stock actualizado.

**Respuesta Actual:**
```json
{
  "compraId": "uuid",
  "estado": "CONFIRMADA",
  "mensajeUsuario": null
}
```

**Respuesta Esperada (Mejora):**
```json
{
  "compraId": "uuid",
  "estado": "CONFIRMADA",
  "mensajeUsuario": null,
  "saldoActual": 50000.00,
  "stockActualizado": [
    {
      "recursoId": "uuid",
      "recursoNombre": "CEMENTO GRIS",
      "stockAnterior": 1000.00,
      "stockActual": 1100.00,
      "unidad": "KG"
    }
  ]
}
```

**Alternativa (si no se incluye en respuesta):**
El frontend podría hacer una llamada adicional a:
- `GET /api/v1/proyectos/{proyectoId}/saldo` (existe ✅)
- `GET /api/v1/proyectos/{proyectoId}/inventario?recursoIds=...` (❌ no existe)

**Impacto:** 
- **Opción 1 (Recomendada):** Incluir saldo y stock en la respuesta de compra → **1 llamada HTTP**
- **Opción 2:** Frontend hace 2 llamadas adicionales después de la compra → **3 llamadas HTTP totales** (menos eficiente, pero funcional)

**Estado:** 🔴 **BLOQUEANTE** si se requiere mostrar feedback inmediato sin llamadas adicionales.

---

## 📋 ENDPOINTS EXISTENTES (Inventario)

### ✅ Endpoints Disponibles

| Endpoint | Método | Descripción | Estado |
|----------|--------|-------------|--------|
| `/api/v1/compras/directa` | POST | Registrar compra directa | ✅ READY |
| `/api/v1/proyectos/{proyectoId}/saldo` | GET | Consultar saldo de billetera | ✅ READY |
| `/api/v1/presupuestos/{presupuestoId}/partidas` | GET | Listar partidas de presupuesto | ✅ READY |
| `/api/v1/presupuestos/{presupuestoId}/partidas` | POST | Crear partida | ✅ READY |
| `/api/v1/recursos` | POST | Crear recurso | ✅ READY |

---

## 🚨 ENDPOINTS FALTANTES (CRÍTICOS)

### 1. Listar Proyectos
```
GET /api/v1/proyectos
GET /api/v1/proyectos?estado={estado}
```
**Prioridad:** 🔴 **ALTA** - Bloqueante para inicio del wireflow

### 2. Listar Presupuestos por Proyecto
```
GET /api/v1/proyectos/{proyectoId}/presupuestos
```
**Prioridad:** 🔴 **ALTA** - Bloqueante para selección de presupuesto

### 3. Buscar Recursos (Autocomplete)
```
GET /api/v1/recursos?search={query}
GET /api/v1/recursos?search={query}&tipo={tipo}
GET /api/v1/recursos?search={query}&limit={limit}
```
**Prioridad:** 🔴 **ALTA** - Bloqueante para funcionalidad core

### 4. Consultar Inventario por Recursos
```
GET /api/v1/proyectos/{proyectoId}/inventario?recursoIds={uuid1,uuid2,...}
```
**Prioridad:** 🟡 **MEDIA** - Necesario si no se incluye en respuesta de compra

---

## 💡 RECOMENDACIONES

### Opción A: Completar Endpoints Faltantes (RECOMENDADA)

**Sprint Backend (Estimado: 2-3 días):**

1. **Día 1:**
   - `GET /api/v1/proyectos` - Listar proyectos
   - `GET /api/v1/proyectos/{proyectoId}/presupuestos` - Listar presupuestos

2. **Día 2:**
   - `GET /api/v1/recursos?search={query}` - Búsqueda de recursos con autocomplete
   - Implementar índice `GIN + pg_trgm` para búsqueda difusa (según wireflow)

3. **Día 3:**
   - Mejorar `POST /api/v1/compras/directa` response para incluir `saldoActual` y `stockActualizado`
   - O crear `GET /api/v1/proyectos/{proyectoId}/inventario?recursoIds=...`

**Resultado:** Frontend puede iniciar desarrollo sin mocks.

---

### Opción B: Desarrollo Paralelo con Mocks (NO RECOMENDADA)

**Riesgos:**
- Desalineación entre mocks y implementación real
- Retrabajo cuando se implementen endpoints reales
- Posibles inconsistencias en contratos

**Solo viable si:**
- Los contratos de los endpoints faltantes están **completamente definidos** (OpenAPI/Swagger)
- El equipo frontend tiene experiencia con mocks
- Hay presión de tiempo crítica

---

## ✅ CHECKLIST DE READINESS

- [ ] `GET /api/v1/proyectos` implementado
- [ ] `GET /api/v1/proyectos/{proyectoId}/presupuestos` implementado
- [ ] `GET /api/v1/recursos?search={query}` implementado con búsqueda difusa
- [ ] Respuesta de compra incluye `saldoActual` y `stockActualizado` O existe endpoint de inventario
- [ ] Documentación OpenAPI/Swagger actualizada
- [ ] Tests de integración para nuevos endpoints

---

## 🎯 VEREDICTO FINAL

### ¿Podemos iniciar desarrollo del Frontend HOY?

**Respuesta:** 🔴 **NO**

**Razones:**
1. **3 endpoints bloqueantes faltantes** (proyectos, presupuestos, búsqueda recursos)
2. **1 mejora necesaria** en respuesta de compra (saldo/stock)
3. Sin estos endpoints, el frontend no puede implementar el flujo completo del Wireflow 1

**Recomendación:**
> **Completar los endpoints faltantes en el backend (2-3 días) antes de iniciar desarrollo del frontend.** Esto evitará retrabajo, mocks innecesarios y asegurará un desarrollo fluido.

---

## 📝 NOTAS TÉCNICAS

### Endpoints Parcialmente Implementados

1. **Partidas:** ✅ Existe `GET /api/v1/presupuestos/{presupuestoId}/partidas`
   - **Problema:** Requiere conocer el `presupuestoId` de antemano
   - **Solución:** Necesario `GET /api/v1/proyectos/{proyectoId}/presupuestos` primero

2. **Saldo:** ✅ Existe `GET /api/v1/proyectos/{proyectoId}/saldo`
   - **Problema:** Requiere llamada adicional después de compra
   - **Solución:** Incluir en respuesta de compra O crear endpoint de inventario

### Consideraciones de Performance

Según el wireflow, la búsqueda de recursos debe usar:
- Índice `GIN + pg_trgm` para búsqueda difusa
- Caché de lectura para búsquedas frecuentes
- Límite de resultados para autocomplete (ej: 10-20 resultados)

---

**Fin del Reporte**
