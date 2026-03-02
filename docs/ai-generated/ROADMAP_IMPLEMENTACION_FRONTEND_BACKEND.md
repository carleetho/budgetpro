# 🗺️ Roadmap de Implementación: Frontend + Backend (Enfoque Híbrido)

**Fecha de Creación**: 2026-02-23  
**Timeline Total**: 3-4 meses  
**Objetivo**: MVP Funcional con Backend 65-70% Maturity  
**Estrategia**: Desarrollo Paralelo Frontend + Backend

---

## 📊 Resumen Ejecutivo

| Fase | Timeline | Objetivo | Maturity Backend | Entregables |
|------|----------|----------|------------------|-------------|
| **Fase 0** | Semana 1 | Setup & Preparación | 59.4% | Infraestructura lista |
| **Fase 1** | Semanas 2-6 | Frontend Core + Backend Crítico | 62% | Módulos core funcionando |
| **Fase 2** | Semanas 7-10 | Frontend MVP + Backend Estabilización | 67% | MVP funcional |
| **Fase 3** | Semanas 11-16 | Refinamiento + Optimizaciones | 70% | MVP completo |

---

## 🎯 Fase 0: Preparación y Setup (Semana 1)

**Objetivo**: Preparar infraestructura y validar readiness

### Backend
- [x] ✅ Validar APIs REST (30 controllers)
- [x] ✅ Verificar autenticación JWT
- [x] ✅ Revisar documentación OpenAPI
- [ ] **NUEVO**: Crear endpoint de health check mejorado
- [ ] **NUEVO**: Documentar contratos de API críticos
- [ ] **NUEVO**: Setup CORS para desarrollo frontend

### Frontend
- [ ] Setup proyecto (React/Vue/Next.js)
- [ ] Configurar autenticación JWT
- [ ] Setup API client (Axios/Fetch)
- [ ] Configurar variables de entorno
- [ ] Setup routing básico

### Infraestructura
- [ ] Configurar ambiente de desarrollo compartido
- [ ] Setup CI/CD básico
- [ ] Documentar proceso de deployment

**Criterios de Éxito**:
- ✅ Backend corriendo y accesible
- ✅ Frontend puede autenticarse
- ✅ Frontend puede hacer requests básicos
- ✅ Documentación accesible

**Timeline**: 1 semana  
**Responsable**: DevOps + Backend Lead

---

## 🚀 Fase 1: Frontend Core + Backend Crítico (Semanas 2-6)

**Objetivo**: Implementar módulos core en frontend y mejorar módulos críticos en backend

### 📱 Frontend: Módulos Core (Semanas 2-6)

#### Semana 2-3: Autenticación y Layout Base
- [ ] Implementar login/logout
- [ ] Layout principal (sidebar, header, footer)
- [ ] Sistema de navegación
- [ ] Manejo de sesión y tokens
- [ ] Protección de rutas

**APIs Backend Usadas**:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me`

#### Semana 3-4: Módulo Presupuesto (80% maturity)
- [ ] Listar presupuestos por proyecto
- [ ] Ver detalle de presupuesto
- [ ] Visualizar WBS (Work Breakdown Structure)
- [ ] Ver partidas y APUs
- [ ] Dashboard de ejecución presupuestaria

**APIs Backend Usadas**:
- `GET /api/v1/presupuestos`
- `GET /api/v1/presupuestos/{id}`
- `GET /api/v1/presupuestos/{id}/partidas`
- `GET /api/v1/presupuestos/{id}/ejecucion`

**Dependencias Backend**: Ninguna (ya está al 80%)

#### Semana 4-5: Módulo EVM (65% maturity)
- [ ] Dashboard de métricas EVM
- [ ] Visualización CPI/SPI
- [ ] Gráficos de EAC/ETC/VAC
- [ ] Tabla de snapshots EVM
- [ ] Comparativa presupuesto vs real

**APIs Backend Usadas**:
- `GET /api/v1/evm/proyectos/{id}/snapshots`
- `GET /api/v1/evm/proyectos/{id}/metricas`
- `GET /api/v1/evm/proyectos/{id}/indices`

**Dependencias Backend**: Ninguna (ya está al 65%)

#### Semana 5-6: Módulo Cronograma (60% maturity)
- [ ] Visualización de cronograma (Gantt)
- [ ] Ver actividades y dependencias
- [ ] Critical Path visualization
- [ ] Filtros por proyecto/actividad
- [ ] Actualización de fechas

**APIs Backend Usadas**:
- `GET /api/v1/cronograma/proyectos/{id}`
- `GET /api/v1/cronograma/proyectos/{id}/actividades`
- `GET /api/v1/cronograma/proyectos/{id}/critical-path`

**Dependencias Backend**: Ninguna (ya está al 60%)

### 🔧 Backend: Mejoras Críticas (Semanas 2-6)

#### Semana 2-4: Billetera 50% → 70%

**Task B-01: Multi-Currency Básico**
- [ ] Agregar soporte para múltiples monedas (USD, PEN, EUR)
- [ ] Endpoint para conversión de monedas
- [ ] Actualizar entidades para almacenar moneda
- [ ] Validaciones de currency mix
- [ ] Tests de integración

**Entregables**:
- `BilleteraEntity` con campo `moneda`
- `CurrencyConverterService`
- `GET /api/v1/billetera/conversiones`
- Migración: `V24__add_currency_to_billetera.sql`

**Estimación**: 1.5 semanas

**Task B-02: Mejoras en Flujos de Pago**
- [ ] Endpoint para listar transacciones con filtros
- [ ] Mejorar validaciones de saldo
- [ ] Agregar paginación a listados
- [ ] Endpoint para resumen de flujos de caja

**Entregables**:
- `GET /api/v1/billetera/transacciones?page=0&size=20`
- `GET /api/v1/billetera/resumen?proyectoId=xxx&fechaDesde=xxx`
- Tests de integración

**Estimación**: 1 semana

**Resultado**: Billetera 50% → 70% ✅

---

#### Semana 4-6: Inventario 50% → 70%

**Task I-01: Transfers Complejos**
- [ ] Implementar transferencias entre almacenes
- [ ] Validar disponibilidad antes de transferir
- [ ] Registrar movimientos de transferencia
- [ ] Endpoint para historial de transfers

**Entregables**:
- `TransferenciaAlmacen` domain aggregate
- `TransferenciaAlmacenController` con endpoints
- `POST /api/v1/inventario/transfers`
- `GET /api/v1/inventario/transfers/{id}/historial`
- Migración: `V25__add_transferencia_almacen.sql`

**Estimación**: 1.5 semanas

**Task I-02: Mejor Tracking y Auditoría**
- [ ] Endpoint para auditoría de inventario
- [ ] Mejorar queries de consulta de stock
- [ ] Agregar filtros avanzados
- [ ] Endpoint para reporte de movimientos

**Entregables**:
- `GET /api/v1/inventario/auditoria?almacenId=xxx&fechaDesde=xxx`
- `GET /api/v1/inventario/movimientos?recursoId=xxx`
- Tests de integración

**Estimación**: 1 semana

**Resultado**: Inventario 50% → 70% ✅

---

### 📊 Métricas Fase 1

**Backend**:
- Maturity promedio: 59.4% → 62%
- Nuevos endpoints: +8 endpoints
- Migraciones: +2 (V24, V25)

**Frontend**:
- Módulos implementados: 3 (Auth, Presupuesto, EVM, Cronograma)
- Páginas creadas: ~15-20
- Componentes reutilizables: ~10-15

**Criterios de Éxito**:
- ✅ Frontend puede autenticarse y navegar
- ✅ Módulos core funcionando (Presupuesto, EVM, Cronograma)
- ✅ Billetera e Inventario mejorados a 70%
- ✅ Sin bloqueantes críticos

---

## 🎯 Fase 2: Frontend MVP + Backend Estabilización (Semanas 7-10)

**Objetivo**: Completar MVP frontend y estabilizar backend basado en feedback

### 📱 Frontend: Completar MVP (Semanas 7-10)

#### Semana 7-8: Módulo Compras (60% maturity)
- [ ] Listar órdenes de compra
- [ ] Crear nueva orden de compra
- [ ] Flujo de aprobación (solicitar → aprobar → enviar)
- [ ] Recepción de órdenes (nuevo desde PR #38)
- [ ] Visualizar estado de órdenes

**APIs Backend Usadas**:
- `GET /api/v1/ordenes-compra`
- `POST /api/v1/ordenes-compra`
- `POST /api/v1/ordenes-compra/{id}/solicitar`
- `POST /api/v1/ordenes-compra/{id}/aprobar`
- `POST /api/v1/compras/{compraId}/recepciones` ← **NUEVO**

**Dependencias Backend**: Ninguna (ya está al 60%)

#### Semana 8-9: Módulo Estimación (60% maturity)
- [ ] Listar estimaciones
- [ ] Crear nueva estimación
- [ ] Flujo de aprobación secuencial
- [ ] Visualizar estado de pagos
- [ ] Dashboard de estimaciones pendientes

**APIs Backend Usadas**:
- `GET /api/v1/estimaciones`
- `POST /api/v1/estimaciones`
- `POST /api/v1/estimaciones/{id}/aprobar`
- `GET /api/v1/estimaciones/{id}/pagos`

**Dependencias Backend**: Ninguna (ya está al 60%)

#### Semana 9-10: Módulos Billetera e Inventario (70% maturity)
- [ ] Dashboard de flujos de caja
- [ ] Listar transacciones con filtros
- [ ] Crear ingresos/egresos
- [ ] Visualizar inventario por almacén
- [ ] Transferencias entre almacenes (nuevo)
- [ ] Auditoría de inventario (nuevo)

**APIs Backend Usadas**:
- `GET /api/v1/billetera/transacciones`
- `POST /api/v1/billetera/ingresos`
- `GET /api/v1/inventario/almacenes/{id}/items`
- `POST /api/v1/inventario/transfers` ← **NUEVO**

**Dependencias Backend**: Completadas en Fase 1

#### Semana 10: Integración y Testing
- [ ] Integración end-to-end de flujos principales
- [ ] Testing de integración frontend-backend
- [ ] Fixes de bugs encontrados
- [ ] Mejoras de UX basadas en testing

---

### 🔧 Backend: Estabilización y Fixes (Semanas 7-10)

#### Semana 7-8: Fixes Basados en Feedback Frontend
- [ ] Ajustar DTOs según necesidades frontend
- [ ] Agregar campos faltantes en responses
- [ ] Mejorar manejo de errores
- [ ] Optimizar queries lentas identificadas
- [ ] Agregar validaciones faltantes

**Prioridad**: Alta (bloqueantes para frontend)

#### Semana 9-10: RRHH 20% → 50% (Baja Prioridad)

**Task R-01: Personnel Tracking Básico**
- [ ] Mejorar endpoints de empleados
- [ ] Agregar tracking de asistencia básico
- [ ] Endpoint para listar cuadrillas
- [ ] Mejorar consultas de costos laborales

**Entregables**:
- `GET /api/v1/rrhh/empleados` mejorado
- `GET /api/v1/rrhh/cuadrillas`
- `POST /api/v1/rrhh/asistencia/registrar`
- Tests de integración

**Estimación**: 2 semanas

**Resultado**: RRHH 20% → 50% ✅

---

### 📊 Métricas Fase 2

**Backend**:
- Maturity promedio: 62% → 67%
- Fixes implementados: ~15-20
- Nuevos endpoints: +5
- RRHH mejorado: 20% → 50%

**Frontend**:
- Módulos implementados: 7 (todos los core)
- Páginas creadas: ~40-50
- Flujos completos: 5-6 flujos principales
- Testing: Coverage 60%+

**Criterios de Éxito**:
- ✅ MVP frontend funcional
- ✅ Todos los flujos principales funcionando
- ✅ Backend estabilizado (67% maturity)
- ✅ Sin bugs críticos

---

## 🎨 Fase 3: Refinamiento y Optimizaciones (Semanas 11-16)

**Objetivo**: Refinar MVP y optimizar basado en feedback real

### 📱 Frontend: Refinamiento (Semanas 11-14)

#### Semana 11-12: Mejoras de UX
- [ ] Optimizar performance de componentes
- [ ] Mejorar feedback visual (loading states, errors)
- [ ] Refinar diseño y estilos
- [ ] Mejorar accesibilidad
- [ ] Optimizar bundle size

#### Semana 13-14: Features Adicionales
- [ ] Búsqueda global
- [ ] Filtros avanzados
- [ ] Exportación de datos (PDF/Excel)
- [ ] Notificaciones en tiempo real
- [ ] Dashboard personalizado

### 🔧 Backend: Optimizaciones (Semanas 11-16)

#### Semana 11-12: Performance
- [ ] Optimizar queries N+1
- [ ] Agregar índices faltantes
- [ ] Implementar caching donde sea necesario
- [ ] Optimizar endpoints lentos

#### Semana 13-14: Features Incrementales
- [ ] Paginación en todos los listados
- [ ] Búsqueda avanzada en endpoints principales
- [ ] Mejoras en validaciones
- [ ] Mejor observabilidad (métricas, logs)

#### Semana 15-16: Preparación para Producción
- [ ] Security audit
- [ ] Performance testing
- [ ] Load testing
- [ ] Documentación final
- [ ] Deployment guide

---

### 📊 Métricas Fase 3

**Backend**:
- Maturity promedio: 67% → 70%
- Performance mejorado: 30-40%
- Nuevas features: 5-8

**Frontend**:
- Performance optimizado
- UX refinado
- Testing coverage: 70%+
- Bundle size optimizado

**Criterios de Éxito**:
- ✅ MVP completo y refinado
- ✅ Performance aceptable
- ✅ Listo para usuarios beta
- ✅ Backend 70% maturity

---

## 📋 Detalle de Tareas por Módulo Backend

### 🔴 Prioridad ALTA (Bloqueantes)

#### Billetera: 50% → 70%

**Task B-01: Multi-Currency Básico** (1.5 semanas)
- [ ] Agregar enum `Moneda` (USD, PEN, EUR)
- [ ] Actualizar `BilleteraEntity` con campo `moneda`
- [ ] Crear `CurrencyConverterService`
- [ ] Endpoint: `GET /api/v1/billetera/conversiones?from=USD&to=PEN&amount=1000`
- [ ] Migración: `V24__add_currency_to_billetera.sql`
- [ ] Tests unitarios e integración

**Task B-02: Mejoras en Flujos** (1 semana)
- [ ] Agregar paginación a `GET /api/v1/billetera/transacciones`
- [ ] Endpoint: `GET /api/v1/billetera/resumen?proyectoId=xxx`
- [ ] Mejorar validaciones de saldo
- [ ] Tests de integración

**Dependencias**: Ninguna  
**Timeline**: Semanas 2-4  
**Resultado**: 50% → 70%

---

#### Inventario: 50% → 70%

**Task I-01: Transfers Complejos** (1.5 semanas)
- [ ] Crear `TransferenciaAlmacen` domain aggregate
- [ ] `TransferenciaAlmacenEntity` y repository
- [ ] `TransferenciaAlmacenController`
- [ ] Endpoints:
  - `POST /api/v1/inventario/transfers`
  - `GET /api/v1/inventario/transfers/{id}`
  - `GET /api/v1/inventario/transfers?almacenOrigen=xxx`
- [ ] Migración: `V25__add_transferencia_almacen.sql`
- [ ] Tests comprehensivos

**Task I-02: Tracking y Auditoría** (1 semana)
- [ ] Endpoint: `GET /api/v1/inventario/auditoria?almacenId=xxx`
- [ ] Endpoint: `GET /api/v1/inventario/movimientos?recursoId=xxx`
- [ ] Mejorar queries de consulta
- [ ] Tests de integración

**Dependencias**: Ninguna  
**Timeline**: Semanas 4-6  
**Resultado**: 50% → 70%

---

### 🟡 Prioridad MEDIA (No Bloqueante)

#### RRHH: 20% → 50%

**Task R-01: Personnel Tracking Básico** (2 semanas)
- [ ] Mejorar `EmpleadoController` con endpoints completos
- [ ] Endpoint: `GET /api/v1/rrhh/cuadrillas`
- [ ] Endpoint: `POST /api/v1/rrhh/asistencia/registrar`
- [ ] Mejorar `CostosLaboralesController`
- [ ] Tests de integración

**Dependencias**: Ninguna  
**Timeline**: Semanas 9-10  
**Resultado**: 20% → 50%

---

### 🟢 Prioridad BAJA (Post-MVP)

#### Optimizaciones Generales (Semanas 11-16)
- [ ] Paginación en todos los listados
- [ ] Búsqueda avanzada
- [ ] Performance optimizations
- [ ] Caching estratégico

---

## 📱 Detalle de Tareas Frontend

### Semana 2-3: Setup y Autenticación

**Task F-01: Setup Proyecto** (3 días)
- [ ] Inicializar proyecto (React/Vue/Next.js)
- [ ] Configurar build tools
- [ ] Setup routing (React Router/Vue Router)
- [ ] Configurar state management (Redux/Pinia/Zustand)
- [ ] Setup API client

**Task F-02: Autenticación** (4 días)
- [ ] Página de login
- [ ] Manejo de tokens JWT
- [ ] Refresh token automático
- [ ] Protección de rutas
- [ ] Logout

**Task F-03: Layout Base** (3 días)
- [ ] Layout principal (sidebar, header)
- [ ] Sistema de navegación
- [ ] Menú contextual
- [ ] Footer

---

### Semana 3-4: Módulo Presupuesto

**Task F-04: Listado y Detalle** (3 días)
- [ ] Listar presupuestos por proyecto
- [ ] Ver detalle de presupuesto
- [ ] Filtros básicos
- [ ] Paginación

**Task F-05: Visualización WBS** (4 días)
- [ ] Componente de árbol WBS
- [ ] Expandir/colapsar nodos
- [ ] Visualización de partidas
- [ ] Detalle de APUs

**Task F-06: Dashboard Ejecución** (3 días)
- [ ] Gráficos de ejecución presupuestaria
- [ ] Métricas clave (ejecutado, disponible, comprometido)
- [ ] Comparativa presupuesto vs real

---

### Semana 4-5: Módulo EVM

**Task F-07: Dashboard EVM** (5 días)
- [ ] Visualización CPI/SPI
- [ ] Gráficos de EAC/ETC/VAC
- [ ] Tabla de snapshots
- [ ] Filtros por fecha/proyecto

---

### Semana 5-6: Módulo Cronograma

**Task F-08: Visualización Gantt** (5 días)
- [ ] Componente Gantt chart
- [ ] Visualización de actividades
- [ ] Dependencias visuales
- [ ] Critical Path destacado

**Task F-09: Gestión Actividades** (3 días)
- [ ] Crear/editar actividades
- [ ] Actualizar fechas
- [ ] Asignar recursos

---

### Semana 7-8: Módulo Compras

**Task F-10: Gestión Órdenes de Compra** (5 días)
- [ ] Listar órdenes
- [ ] Crear nueva orden
- [ ] Flujo de aprobación
- [ ] Recepción de órdenes (nuevo)

**Task F-11: Estado y Tracking** (3 días)
- [ ] Visualizar estado de órdenes
- [ ] Timeline de estados
- [ ] Notificaciones de cambios

---

### Semana 8-9: Módulo Estimación

**Task F-12: Gestión Estimaciones** (5 días)
- [ ] Listar estimaciones
- [ ] Crear nueva estimación
- [ ] Flujo de aprobación secuencial
- [ ] Tracking de pagos

---

### Semana 9-10: Módulos Billetera e Inventario

**Task F-13: Billetera** (3 días)
- [ ] Dashboard de flujos de caja
- [ ] Listar transacciones
- [ ] Crear ingresos/egresos
- [ ] Conversión de monedas

**Task F-14: Inventario** (4 días)
- [ ] Visualizar inventario por almacén
- [ ] Transferencias entre almacenes
- [ ] Auditoría de inventario
- [ ] Reportes de movimientos

---

## 📊 Métricas y Criterios de Éxito

### Backend

| Métrica | Inicio | Fase 1 | Fase 2 | Fase 3 |
|---------|--------|--------|--------|--------|
| **Maturity Promedio** | 59.4% | 62% | 67% | 70% |
| **Endpoints REST** | 30 | 38 | 43 | 48 |
| **Migraciones** | 23 | 25 | 25 | 25 |
| **Tests Coverage** | ~60% | ~65% | ~70% | ~75% |
| **Performance (avg response)** | ~200ms | ~180ms | ~150ms | ~120ms |

### Frontend

| Métrica | Fase 1 | Fase 2 | Fase 3 |
|---------|--------|--------|--------|
| **Módulos Implementados** | 3 | 7 | 7 |
| **Páginas Creadas** | 20 | 50 | 60 |
| **Componentes** | 15 | 40 | 50 |
| **Tests Coverage** | 40% | 60% | 70% |
| **Bundle Size** | - | ~2MB | ~1.5MB |
| **Lighthouse Score** | - | 70 | 85+ |

---

## 🎯 Criterios de Éxito por Fase

### Fase 1 (Semanas 2-6)
- [ ] Frontend puede autenticarse y navegar
- [ ] 3 módulos core funcionando (Presupuesto, EVM, Cronograma)
- [ ] Billetera e Inventario mejorados a 70%
- [ ] Sin bloqueantes críticos
- [ ] APIs documentadas y accesibles

### Fase 2 (Semanas 7-10)
- [ ] MVP frontend funcional
- [ ] Todos los flujos principales funcionando
- [ ] Backend estabilizado (67% maturity)
- [ ] Sin bugs críticos
- [ ] Testing básico completado

### Fase 3 (Semanas 11-16)
- [ ] MVP completo y refinado
- [ ] Performance aceptable (<200ms avg)
- [ ] Listo para usuarios beta
- [ ] Backend 70% maturity
- [ ] Documentación completa

---

## ⚠️ Gestión de Riesgos

### Riesgos Identificados

#### 🔴 Alto Riesgo
1. **APIs Faltantes para Frontend**
   - **Mitigación**: Revisar necesidades frontend en Fase 0
   - **Contingencia**: Implementar endpoints faltantes en Fase 1

2. **Performance Issues**
   - **Mitigación**: Monitoring desde inicio
   - **Contingencia**: Optimizaciones en Fase 3

#### 🟡 Medio Riesgo
3. **Cambios en Contratos de API**
   - **Mitigación**: Versionado de APIs desde inicio
   - **Contingencia**: Mantener versiones anteriores

4. **Integración Frontend-Backend**
   - **Mitigación**: Testing de integración continuo
   - **Contingencia**: Mocks para desarrollo frontend

#### 🟢 Bajo Riesgo
5. **Features Faltantes**
   - **Mitigación**: Priorización basada en feedback
   - **Contingencia**: Implementación incremental

---

## 📅 Timeline Visual

```
Semana:  1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16
         │    │    │    │    │    │    │    │    │    │    │    │    │    │    │    │
Fase 0:  ████
Fase 1:       ████████████████████████
Fase 2:                                    ████████████████
Fase 3:                                                          ████████████████████

Backend: Setup → Billetera/Inventario → Estabilización → Optimizaciones
Frontend:        Setup/Auth → Core Modules → MVP → Refinamiento
```

---

## 🎯 Entregables Finales

### Backend
- ✅ Maturity promedio: 70%
- ✅ 48+ endpoints REST
- ✅ Performance optimizado
- ✅ Documentación completa
- ✅ Tests coverage: 75%+

### Frontend
- ✅ MVP funcional completo
- ✅ 7 módulos implementados
- ✅ 60+ páginas
- ✅ Performance optimizado
- ✅ Tests coverage: 70%+

### Documentación
- ✅ API documentation actualizada
- ✅ Guía de deployment
- ✅ Manual de usuario (básico)
- ✅ Changelog completo

---

## 📝 Notas de Implementación

### Mejores Prácticas
1. **Desarrollo Paralelo**: Frontend y Backend trabajan en paralelo
2. **Comunicación Continua**: Daily sync entre equipos
3. **Testing Continuo**: Tests de integración desde Fase 1
4. **Feedback Loop**: Ajustes basados en necesidades reales
5. **Versionado**: APIs versionadas para evitar breaking changes

### Herramientas Recomendadas
- **API Testing**: Postman/Insomnia collections
- **Monitoring**: Application performance monitoring
- **CI/CD**: GitHub Actions / GitLab CI
- **Documentation**: Swagger/OpenAPI mantenido actualizado

---

## 🚀 Próximos Pasos Inmediatos

1. **Validar Roadmap** con equipo
2. **Asignar Responsables** por fase
3. **Setup Infraestructura** (Fase 0)
4. **Iniciar Fase 1** (Semanas 2-6)

---

**Generado por**: AI Assistant  
**Fecha**: 2026-02-23  
**Versión**: 1.0.0  
**Próxima Revisión**: Al finalizar Fase 1
