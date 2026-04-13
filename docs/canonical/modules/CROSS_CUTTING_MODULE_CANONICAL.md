# CROSS CUTTING Module - Canonical Specification

> **Status**: Completed (90%)
> **Owner**: Architecture Team
> **Last Updated**: 2026-04-12

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State     | Deliverables                                |
| ----------- | --------- | ---------------- | ------------------------------------------- |
| **Current** | Now       | 90% (Foundation) | Auth, Validation, Hexagonal Core, Logging   |
| **Next**    | +1 Month  | 95%              | Advanced Audit, RBAC Granularity            |
| **Target**  | +3 Months | 100%             | Tenant Isolation (Multitenancy preparation) |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                | Status         |
| ---- | ------------------------------------------------------------------- | -------------- |
| X-01 | **Hexagonal Purity**: Domain must not depend on Infrastructure.     | ✅ Implemented |
| X-02 | **Fail-Fast**: Validation must occur at Boundary and Inside Domain. | ✅ Implemented |
| X-03 | **Audit**: Critical operations (Freeze, Approve) must be traceable. | ✅ Implemented |


### 2.2 Extended Rule Inventory (Phase 1 Alignment)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-001 | **Un reporte aprobado o rechazado es inmutable; no se puede editar ni eliminar.** | ✅ Implemented |
| REGLA-002 | **La fecha del reporte es obligatoria y no puede ser futura.** | ✅ Implemented |
| REGLA-003 | **Solo se puede reportar avance en proyectos en estado EJECUCION.** | ✅ Implemented |
| REGLA-004 | **La cantidad reportada es obligatoria y el reporte no puede exceder el metrado vigente de la partida.** | ✅ Implemented |
| REGLA-005 | **Un reporte de producción debe contener al menos un detalle.** | ✅ Implemented |
| REGLA-006 | **Solo se puede aprobar un reporte en estado PENDIENTE.** | ✅ Implemented |
| REGLA-007 | **Solo se puede rechazar un reporte en estado PENDIENTE y el motivo es obligatorio.** | ✅ Implemented |
| REGLA-008 | **Si el estado del reporte es nulo, se asigna PENDIENTE.** | ✅ Implemented |
| REGLA-009 | **El reporte de producción no puede ser nulo.** | ✅ Implemented |
| REGLA-024 | **Los días de aguinaldo, vacaciones y no trabajados no pueden ser negativos; los días laborables al año deben ser positivos; el porcentaje de seguridad social debe estar entre 0 y 100.** | ✅ Implemented |
| REGLA-026 | **Si un recurso EQUIPO tiene costo_horario = 0, se genera ALERTA CRÍTICA.** | ✅ Implemented |
| REGLA-027 | **El ratio Kg Acero / m3 Concreto debe estar entre 80 y 150; fuera de rango genera WARNING.** | ✅ Implemented |
| REGLA-028 | **El tamaño del agregado no debe exceder 1/5 del ancho del elemento; si excede, genera ALERTA TÉCNICA.** | ✅ Implemented |
| REGLA-030 | **Valor inicial, vida útil y horas anuales de uso deben ser positivos para calcular costo horario.** | ✅ Implemented |
| REGLA-034 | **El consumo de partida requiere partidaId, fecha y tipo; el monto no puede ser negativo.** | ✅ Implemented |
| REGLA-039 | **En Recurso: nombre no vacío, tipo no nulo y unidadBase no vacía.** | ✅ Implemented |
| REGLA-040 | **El nombre del recurso se normaliza con trim, uppercase y espacios simples.** | ✅ Implemented |
| REGLA-041 | **Un recurso provisional se crea con estado EN_REVISION.** | ✅ Implemented |
| REGLA-043 | **El nombre del proyecto no puede estar vacío; el estado del proyecto no puede ser nulo.** | ✅ Implemented |
| REGLA-051 | **JWT_SECRET es obligatorio y debe tener al menos 32 caracteres.** | ✅ Implemented |
| REGLA-052 | **Las rutas `/api/public/**` y `/api/v1/auth/**` son públicas; el resto requiere autenticación.** | ✅ Implemented |
| REGLA-053 | **CORS permite origen `http://localhost:3000` y métodos GET, POST, PUT, DELETE, PATCH, OPTIONS.** | ✅ Implemented |
| REGLA-054 | **Las variables RESEND_API_KEY, ADMIN_EMAIL y JWT_SECRET se requieren por configuración.** | 🟡 Implemented |
| REGLA-055 | **La entidad Usuario exige email único.** | ✅ Implemented |
| REGLA-056 | **El rol de usuario debe estar en {ADMIN, RESIDENTE, GERENTE, AUDITOR}.** | ✅ Implemented |
| REGLA-057 | **El estado de Lead debe estar en {NUEVO, CONTACTADO, CONVERTIDO}.** | ✅ Implemented |
| REGLA-058 | **El estado de reporte de producción debe estar en {PENDIENTE, APROBADO, RECHAZADO}.** | ✅ Implemented |
| REGLA-059 | **En detalle RPC, cantidad_reportada debe ser >= 0.** | ✅ Implemented |
| REGLA-060 | **En proyecto, el estado está restringido por CHECK en migraciones.** | ✅ Implemented |
| REGLA-063 | **En recurso, el tipo debe estar en {MATERIAL, MANO_OBRA, EQUIPO, SUBCONTRATO}; el estado en {ACTIVO, EN_REVISION, DEPRECADO}; costo_referencia >= 0.** | ✅ Implemented |
| REGLA-069 | **En configuracion_laboral: días no negativos; porcentaje_seguridad_social entre 0 y 100; dias_laborables_ano > 0.** | ✅ Implemented |
| REGLA-071 | **El proyecto tiene moneda obligatoria de longitud 3 y presupuesto_total no nulo.** | ✅ Implemented |
| REGLA-072 | **La orden de cambio requiere proyecto, código y tipo/estado; impacto_plazo no puede ser negativo.** | ✅ Implemented |
| REGLA-073 | **El created_by es obligatorio en entidades auditables.** | ✅ Implemented |
| REGLA-074 | **En Lead: nombre_contacto obligatorio; email debe ser válido; fecha_solicitud no nula.** | ✅ Implemented |
| REGLA-075 | **En usuario: nombre, email y password no pueden estar vacíos; email debe ser válido; rol y activo no nulos.** | ✅ Implemented |
| REGLA-076 | **En ReporteProduccion: fecha_reporte, responsable_id y estado son obligatorios.** | ✅ Implemented |
| REGLA-077 | **En DetalleRPC: reporte_id, partida_id y cantidad_reportada son obligatorios.** | ✅ Implemented |
| REGLA-078 | **Para login: email debe ser válido y no vacío; password es obligatoria.** | ✅ Implemented |
| REGLA-079 | **Para registro: nombreCompleto obligatorio; email válido y obligatorio; password obligatoria con tamaño 6-72.** | ✅ Implemented |
| REGLA-080 | **Para crear lead público: nombreContacto obligatorio; email válido si se provee; límites de tamaño.** | ✅ Implemented |
| REGLA-081 | **En RPC request: fechaReporte y responsableId obligatorios; debe incluir al menos un detalle.** | ✅ Implemented |
| REGLA-082 | **En detalle RPC request: partidaId y cantidad reportada obligatorias; cantidad positiva.** | ✅ Implemented |
| REGLA-083 | **En aprobación RPC request: aprobadorId es obligatorio.** | ✅ Implemented |
| REGLA-084 | **En rechazo RPC request: aprobadorId y motivo son obligatorios.** | ✅ Implemented |
| REGLA-086 | **Para calcular reajuste: proyectoId, presupuestoId, fechaCorte, códigos y fechas de índice base/actual son obligatorios.** | ✅ Implemented |
| REGLA-090 | **En configuración laboral request: días no negativos; porcentaje seguridad social entre 0 y 100; días laborables obligatorios y positivos.** | ✅ Implemented |
| REGLA-097 | **Para crear proyecto: nombre obligatorio.** | ✅ Implemented |
| REGLA-099 | **Para crear recurso: nombre, tipo y unidadBase obligatorios.** | ✅ Implemented |
| REGLA-100 | **El sistema opera exclusivamente en modalidad online.** | 🟡 Implemented |
| REGLA-102 | **Ningún proceso operativo puede existir fuera del presupuesto (compras, inventarios, mano de obra, avances físicos, pagos).** | 🟡 Implemented |
| REGLA-103 | **Los datos históricos no se corrigen; se explican mediante eventos formales.** | 🟡 Implemented |
| REGLA-104 | **Toda excepción debe clasificarse, requerir autorización explícita y quedar registrada de forma permanente.** | 🟡 Implemented |
| REGLA-105 | **Un Proyecto en BORRADOR no puede ejecutar compras, inventarios, mano de obra ni avances físicos.** | 🟡 Implemented |
| REGLA-106 | **Un Proyecto solo puede activarse si existe Presupuesto congelado y Snapshot inmutable.** | 🟡 Implemented |
| REGLA-107 | **La Línea Base requiere Presupuesto CONGELADO y Cronograma CONGELADO; la ausencia invalida ejecución.** | 🟡 Implemented |
| REGLA-108 | **Estados del Proyecto: BORRADOR, ACTIVO, SUSPENDIDO, CERRADO con semántica definida.** | 🟡 Implemented |
| REGLA-109 | **Transiciones permitidas: BORRADOR→ACTIVO, ACTIVO→SUSPENDIDO, ACTIVO→CERRADO, SUSPENDIDO→ACTIVO; prohibidas: BORRADOR→CERRADO, CERRADO→otros.** | 🟡 Implemented |
| REGLA-113 | **Las Órdenes de Cambio no sobrescriben la Línea Base; ajustan el BAC y mantienen el Presupuesto original visible.** | 🟡 Implemented |
| REGLA-114 | **El monto acumulado de Órdenes de Cambio no puede exceder ±20% del monto contractual original congelado.** | 🟡 Implemented |
| REGLA-121 | **Diferencias entre inventario físico y sistema deben registrarse como Excepción.** | 🟡 Implemented |
| REGLA-124 | **No se permite que un trabajador esté asignado a dos proyectos ACTIVO el mismo día y horario.** | 🟡 Implemented |
| REGLA-128 | **No se puede crear un recurso con el mismo nombre normalizado si ya existe.** | ✅ Implemented |
| REGLA-129 | **El tipo de recurso debe ser un valor válido del enum TipoRecurso.** | ✅ Implemented |
| REGLA-135 | **En orden_cambio, tipo debe estar en {ADICIONAL, DEDUCTIVO, PRECIO, PLAZO} y estado en {SOLICITADO, APROBADO, RECHAZADO}.** | ✅ Implemented |
| REGLA-138 | **JWT expira en 24 horas por defecto (jwt.expiration-hours: 24).** | 🟡 Implemented |
| REGLA-139 | **En RPC legacy request: fechaReporte obligatoria y al menos un item.** | ✅ Implemented |
| REGLA-140 | **En item RPC legacy: partidaId y cantidad obligatorias; cantidad positiva.** | ✅ Implemented |
| REGLA-141 | **En rechazo RPC legacy: motivo obligatorio.** | ✅ Implemented |
| REGLA-144 | **En DTO de recurso, atributos se inicializan como mapa vacío si es nulo.** | ✅ Implemented |
| REGLA-145 | **El Proyecto es una entidad contractual que habilita o bloquea la ejecución según el estado del presupuesto asociado.** | 🟡 Implemented |
| REGLA-146 | **Si no hay Presupuesto congelado, la activación del Proyecto debe bloquearse con el mensaje "Este proyecto no puede activarse sin un presupuesto congelado."** | 🟡 Implemented |
| REGLA-147 | **Un Proyecto solo puede activarse si existe Cronograma congelado del mismo Proyecto; si no, se bloquea con el mensaje "Este proyecto no puede activarse sin un cronograma congelado."** | 🟡 Implemented |
| REGLA-148 | **Un Snapshot de Presupuesto sin Cronograma no constituye una Línea Base válida.** | 🟡 Implemented |
| REGLA-149 | **Si el Presupuesto principal se invalida, el Proyecto debe pasar a SUSPENDIDO automáticamente.** | 🟡 Implemented |
| REGLA-150 | **Ningún módulo operativo puede ejecutar acciones si el Proyecto no está en estado ACTIVO.** | 🟡 Implemented |
| REGLA-151 | **Todo cambio de estado del Proyecto debe registrar estado anterior, nuevo, usuario, fecha/hora y motivo.** | 🟡 Implemented |
| REGLA-152 | **Un Presupuesto CONGELADO no permite modificación directa; cambios solo mediante Órdenes de Cambio o Excepciones formales.** | 🟡 Implemented |
| REGLA-155 | **Las Órdenes de Cambio ajustan el BAC y las métricas de control; el Presupuesto original permanece visible.** | 🟡 Implemented |
| REGLA-156 | **Toda Orden de Cambio que afecte plazo debe generar ajuste formal del Cronograma contractual.** | 🟡 Implemented |
| REGLA-157 | **El exceso de consumo debe registrarse como Excepción de consumo o Insumo asociado a Orden de Cambio.** | 🟡 Implemented |
| REGLA-158 | **Una Orden de Cambio o Excepción debe registrar tipo, partida afectada, monto, motivo, usuario, fecha y autorización; si falta, se rechaza.** | 🟡 Implemented |
| REGLA-159 | **Las Excepciones no modifican la Línea Base ni ajustan el BAC; incrementan AC.** | 🟡 Implemented |
| REGLA-160 | **Una desviación es permanente; no puede eliminarse ni editarse, solo anularse mediante evento correctivo trazado.** | 🟡 Implemented |
| REGLA-161 | **Las Órdenes de Cambio pueden registrarse en estado PROPUESTA, APROBADA CON EVIDENCIA DIFERIDA o RECHAZADA.** | 🟡 Implemented |

## 3. Domain Events

| Event Name            | Trigger       | Content (Payload) | Status |
| --------------------- | ------------- | ----------------- | ------ |
| `UserRegisteredEvent` | New user      | `userId`, `email` | ✅     |
| `SystemErrorEvent`    | 500 Exception | `stacktrace`      | ✅     |

## 4. State Constraints

- N/A (Stateless util services)

## 5. Data Contracts

### Entity: User

- `id`: UUID
- `email`: String
- `roles`: List<String>

## 6. Use Cases

| ID     | Use Case                | Priority | Status |
| ------ | ----------------------- | -------- | ------ |
| UC-X01 | User Login/Register     | P0       | ✅     |
| UC-X02 | Permission Check        | P0       | ✅     |
| UC-X03 | Send Email Notification | P1       | ✅     |

## 7. Domain Services

- **Service**: `AuthService`, `NotificationService`
- **Responsibility**: Security and Comms.

### 7.1 Boundary errors (REST)

- **`GlobalExceptionHandler`** centraliza excepciones comunes; varios handlers devuelven **`ErrorResponses.ErrorResponse`** (`status`, código de negocio, `message`) — ver `com.budgetpro.infrastructure.rest.error.ErrorResponses`.
- **Excepción:** `ProyectoNotFoundException` (EVM) aún puede responder con cuerpo `Map` legacy (`error`, `proyectoId`); alinear a `ErrorResponses` en evolución futura.
- **`IllegalArgumentException`:** tratado como **400** con cuerpo unificado (p. ej. validación de paginación, rango de fechas UC-E04).

## 8. REST Endpoints

| Method | Path                    | Description | Status |
| ------ | ----------------------- | ----------- | ------ |
| POST   | `/api/v1/auth/login`    | JWT Login   | ✅     |
| POST   | `/api/v1/auth/register` | Sign up     | ✅     |

**Estudio de gaps (Ola 1b):** [CROSS_CUTTING_GAP_STUDY.md](../radiography/gaps/CROSS_CUTTING_GAP_STUDY.md) — **O-02**, **O-04** en [CODE_DOC_REVIEW_LOG.md](../radiography/CODE_DOC_REVIEW_LOG.md).

## 9. Observability

- **Metrics**: `http.request.count`, `p99.latency`
- **Logs**: Structured JSON logging.

## 10. Integration Points

- **Consumes**: N/A
- **Exposes**: `SecurityContext` to ALL modules.

## 11. Technical Debt & Risks

- [ ] **RBAC**: Current Roles are simple strings. Need dynamic Permission sets. (Medium)
