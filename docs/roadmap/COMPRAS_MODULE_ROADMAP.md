# 🗺️ Roadmap: Módulo de Compras (COMPRAS)

**Estado Actual**: 60% (Purchase Orders implementados)  
**Última Actualización**: 2026-02-15  
**Owner**: Logistica Team

---

## 📊 Estado Actual (60%)

### ✅ Completado

- ✅ **Direct Purchase** (Compra directa)
- ✅ **Stock Ingress** (Entrada a inventario)
- ✅ **Purchase Orders** (Órdenes de compra con state machine completa)
- ✅ **Provider Entity** (Entidad Proveedor con validaciones)
- ✅ **Business Rules**: L-01, L-02, L-03, L-04 implementadas
- ✅ **Domain Events**: CompraRegistradaEvent, OrdenCompraEnviadaEvent, OrdenCompraRecibidaEvent
- ✅ **REST API**: 9 endpoints para órdenes de compra documentados
- ✅ **API Documentation**: OpenAPI/Swagger completo
- ✅ **Migration Guide**: Guía de migración de proveedores

---

## 🎯 Fase 1: Mejoras y Deuda Técnica (60% → 70%)

**Timeline**: 2-3 semanas  
**Prioridad**: Alta (mejora calidad y usabilidad)

### Task 11: Provider CRUD API

**Objetivo**: Exponer API REST completa para gestión de proveedores

**Entregables**:
- `ProveedorController.java` con endpoints CRUD
- DTOs: `ProveedorRequest`, `ProveedorResponse`
- Use cases: `CrearProveedorUseCase`, `ActualizarProveedorUseCase`, `ConsultarProveedorUseCase`, `EliminarProveedorUseCase`
- Validaciones: RUC único, estado válido
- Documentación OpenAPI

**Endpoints**:
- `POST /api/v1/proveedores` - Crear proveedor
- `GET /api/v1/proveedores` - Listar proveedores (con filtros)
- `GET /api/v1/proveedores/{id}` - Obtener por ID
- `PUT /api/v1/proveedores/{id}` - Actualizar proveedor
- `DELETE /api/v1/proveedores/{id}` - Eliminar proveedor (con validación de referencias)
- `POST /api/v1/proveedores/{id}/activar` - Activar proveedor
- `POST /api/v1/proveedores/{id}/inactivar` - Inactivar proveedor
- `POST /api/v1/proveedores/{id}/bloquear` - Bloquear proveedor

**Dependencias**: Ninguna (Proveedor entity ya existe)

---

### Task 12: Paginación en Endpoints de Listado

**Objetivo**: Implementar paginación estándar en endpoints de listado

**Entregables**:
- Actualizar `OrdenCompraController.listar()` con `Pageable`
- Actualizar `ProveedorController.listar()` con `Pageable`
- DTOs de respuesta paginada: `PageResponse<T>`
- Documentación OpenAPI actualizada

**Cambios**:
- `GET /api/v1/ordenes-compra?page=0&size=20&sort=fecha,desc`
- `GET /api/v1/proveedores?page=0&size=20&sort=razonSocial,asc`

**Dependencias**: Task 11 (para ProveedorController)

---

### Task 13: Búsqueda y Filtros Avanzados

**Objetivo**: Mejorar capacidades de búsqueda y filtrado

**Entregables**:
- Búsqueda por texto en `OrdenCompraController` (número, proveedor)
- Búsqueda por texto en `ProveedorController` (razón social, RUC)
- Filtros combinados (proyecto + estado + fecha)
- Especificación de filtros en OpenAPI

**Ejemplos**:
- `GET /api/v1/ordenes-compra?search=PO-2024&proyectoId=xxx&estado=APROBADA`
- `GET /api/v1/proveedores?search=CEMEX&estado=ACTIVO`

**Dependencias**: Task 11, Task 12

---

## 🚀 Fase 2: Comparativo de Precios (70% → 80%)

**Timeline**: 3-4 semanas  
**Prioridad**: Media-Alta (funcionalidad de negocio importante)

### Task 14: Comparativo de Precios (Price Comparison)

**Objetivo**: Permitir comparar precios de múltiples proveedores para el mismo item

**Entregables**:
- `ComparativoPrecios` domain aggregate
- `ComparativoPreciosController` con endpoints
- Use cases:
  - `CrearComparativoUseCase` - Crear solicitud de cotización
  - `AgregarCotizacionUseCase` - Agregar cotización de proveedor
  - `CerrarComparativoUseCase` - Cerrar comparativo y seleccionar ganador
  - `GenerarOrdenDesdeComparativoUseCase` - Generar orden de compra desde comparativo
- Persistence layer (tabla `comparativo_precios`, `cotizacion_proveedor`)
- Domain events: `ComparativoCreadoEvent`, `CotizacionAgregadaEvent`, `ComparativoCerradoEvent`

**Reglas de Negocio**:
- Un comparativo puede tener múltiples cotizaciones
- Solo proveedores ACTIVOS pueden cotizar
- El comparativo debe estar ABIERTO para agregar cotizaciones
- Al cerrar, se debe seleccionar un proveedor ganador
- La orden de compra generada usa el precio de la cotización ganadora

**Endpoints**:
- `POST /api/v1/comparativos` - Crear comparativo
- `GET /api/v1/comparativos/{id}` - Obtener comparativo
- `POST /api/v1/comparativos/{id}/cotizaciones` - Agregar cotización
- `POST /api/v1/comparativos/{id}/cerrar` - Cerrar comparativo
- `POST /api/v1/comparativos/{id}/generar-orden` - Generar orden de compra

**Dependencias**: Task 11 (Provider API)

---

### Task 15: Approval Workflow Avanzado

**Objetivo**: Implementar workflow de aprobación con múltiples niveles y notificaciones

**Entregables**:
- `AprobacionOrdenCompra` domain aggregate
- Workflow de aprobación multi-nivel (configurable por monto)
- Notificaciones por email cuando se requiere aprobación
- Historial de aprobaciones (quién aprobó, cuándo, comentarios)
- Use cases:
  - `SolicitarAprobacionNivelUseCase` - Solicitar aprobación en nivel específico
  - `AprobarNivelUseCase` - Aprobar en nivel específico
  - `RechazarAprobacionUseCase` - Rechazar con comentarios
- Persistence layer (tabla `aprobacion_orden_compra`)
- Domain events: `AprobacionSolicitadaEvent`, `AprobacionAprobadaEvent`, `AprobacionRechazadaEvent`

**Reglas de Negocio**:
- Niveles de aprobación basados en monto:
  - Nivel 1: $0 - $1,000 (Aprobador: Supervisor)
  - Nivel 2: $1,001 - $10,000 (Aprobador: Gerente)
  - Nivel 3: $10,001+ (Aprobador: Director)
- Cada nivel debe aprobarse secuencialmente
- Notificación automática al siguiente aprobador
- Timeout de aprobación (escalación automática)

**Endpoints**:
- `GET /api/v1/ordenes-compra/{id}/aprobaciones` - Ver historial de aprobaciones
- `POST /api/v1/ordenes-compra/{id}/aprobaciones/{nivel}/aprobar` - Aprobar nivel
- `POST /api/v1/ordenes-compra/{id}/aprobaciones/{nivel}/rechazar` - Rechazar nivel

**Dependencias**: Task 11, Sistema de notificaciones existente

---

## 🎯 Fase 3: Integración y Reportes (80% → 100%)

**Timeline**: 4-6 semanas  
**Prioridad**: Media (mejora operativa y analítica)

### Task 16: Reportes de Compras

**Objetivo**: Generar reportes analíticos de compras y órdenes

**Entregables**:
- `ReporteComprasController` con endpoints de reportes
- Use cases:
  - `GenerarReporteComprasPorProyectoUseCase` - Compras por proyecto
  - `GenerarReporteComprasPorProveedorUseCase` - Compras por proveedor
  - `GenerarReporteComprasPorPartidaUseCase` - Compras por partida presupuestaria
  - `GenerarReporteOrdenesCompraPorEstadoUseCase` - Órdenes por estado
  - `GenerarReporteComparativoPreciosUseCase` - Análisis de comparativos
- DTOs de reporte con agregaciones (totales, promedios, tendencias)
- Exportación a Excel/PDF (opcional)

**Reportes**:
- Compras por proyecto (mensual, trimestral, anual)
- Top 10 proveedores por volumen
- Análisis de desviaciones de presupuesto
- Tiempo promedio de aprobación de órdenes
- Comparativo de precios históricos

**Endpoints**:
- `GET /api/v1/reportes/compras/proyecto/{proyectoId}?periodo=2024-01`
- `GET /api/v1/reportes/compras/proveedor/{proveedorId}?periodo=2024-01`
- `GET /api/v1/reportes/ordenes-compra/estado?fechaDesde=2024-01-01&fechaHasta=2024-12-31`

**Dependencias**: Task 14, Task 15

---

### Task 17: Integración con Sistemas Externos

**Objetivo**: Integrar con sistemas ERP, contabilidad y facturación

**Entregables**:
- `IntegracionERPAdapter` - Sincronización con sistema ERP
- `IntegracionContabilidadAdapter` - Envío de comprobantes contables
- `IntegracionFacturacionAdapter` - Integración con sistema de facturación
- Domain events para integración:
  - `OrdenCompraEnviadaEvent` → Trigger sincronización ERP
  - `OrdenCompraRecibidaEvent` → Trigger registro contable
- Retry mechanism para integraciones fallidas
- Circuit breaker para resiliencia

**Integraciones**:
- Sincronización de proveedores con ERP
- Envío de órdenes de compra a ERP
- Recepción de facturas desde sistema de facturación
- Registro contable automático

**Dependencias**: Task 14, Task 15

---

### Task 18: Dashboard y Analytics

**Objetivo**: Dashboard en tiempo real de métricas de compras

**Entregables**:
- `DashboardComprasController` con endpoints de métricas
- Métricas en tiempo real:
  - Órdenes pendientes de aprobación
  - Monto total comprometido vs disponible
  - Tiempo promedio de ciclo de orden
  - Proveedores más utilizados
- Agregaciones pre-calculadas (caché)
- WebSocket para actualizaciones en tiempo real (opcional)

**Métricas**:
- KPIs de compras
- Gráficos de tendencias
- Alertas de presupuesto
- Estado de órdenes en tiempo real

**Endpoints**:
- `GET /api/v1/dashboard/compras/metricas`
- `GET /api/v1/dashboard/compras/kpis`
- `GET /api/v1/dashboard/compras/alertas`

**Dependencias**: Task 16

---

## 📋 Resumen de Fases

| Fase | Timeline | Estado Objetivo | Tareas | Prioridad |
|------|----------|-----------------|--------|-----------|
| **Fase 1** | 2-3 semanas | 70% | Task 11, 12, 13 | Alta |
| **Fase 2** | 3-4 semanas | 80% | Task 14, 15 | Media-Alta |
| **Fase 3** | 4-6 semanas | 100% | Task 16, 17, 18 | Media |

---

## 🎯 Próxima Tarea Recomendada: Task 11

**Razón**: 
- Es deuda técnica identificada (Provider API faltante)
- Bajo riesgo (Proveedor entity ya implementada)
- Alto valor (completa el CRUD básico)
- Dependencia para otras tareas (Task 12, Task 13, Task 14)

**Estimación**: 1 semana  
**Complejidad**: Baja-Mediana

---

## 📊 Métricas de Progreso

### Estado Actual (60%)
- ✅ Core functionality: Purchase Orders
- ✅ Business rules: L-01, L-02, L-03, L-04
- ✅ State machine: Completa
- ✅ Domain events: Implementados
- ✅ API documentation: Completa

### Pendiente (40%)
- ⏳ Provider CRUD API
- ⏳ Paginación
- ⏳ Comparativo de precios
- ⏳ Approval workflow avanzado
- ⏳ Reportes y analytics
- ⏳ Integraciones externas

---

## 🔗 Referencias

- **Documentación Canónica**: `docs/canonical/modules/COMPRAS_MODULE_CANONICAL.md`
- **API Documentation**: `backend/src/main/resources/api-docs/orden-compra-api.yaml`
- **Migration Guide**: `docs/migration/PROVIDER_MIGRATION_GUIDE.md`

---

**Última Actualización**: 2026-02-15  
**Versión**: 1.0.0
