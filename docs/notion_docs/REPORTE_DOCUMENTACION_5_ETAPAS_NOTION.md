# Reporte de documentación en 5 etapas (Notion)

## Etapa 1 — Inventario técnico (mapa del código)

### Backend (módulos principales)
<table fit-page-width="true" header-row="true">
	<tr>
		<td>Módulo</td>
		<td>Estado actual</td>
		<td>Responsabilidad técnica</td>
		<td>Archivos clave</td>
		<td>Dependencias</td>
	</tr>
	<tr>
		<td>Auth / Seguridad</td>
		<td>Implementado</td>
		<td>JWT stateless, login/registro, guardas</td>
		<td>`backend/src/main/java/com/budgetpro/infrastructure/rest/auth/controller/AuthController.java`<br>`backend/src/main/java/com/budgetpro/infrastructure/security/*`</td>
		<td>JWT (jjwt), Spring Security</td>
	</tr>
	<tr>
		<td>Producción (RPC)</td>
		<td>Implementado</td>
		<td>Registro, aprobación/rechazo de avances</td>
		<td>`infrastructure/rest/controller/ProduccionController.java`<br>`application/produccion/*`</td>
		<td>Partida, Proyecto, ReporteProduccion</td>
	</tr>
	<tr>
		<td>Presupuesto</td>
		<td>Implementado</td>
		<td>Crear/consultar presupuestos, control costos</td>
		<td>`rest/presupuesto/controller/PresupuestoController.java`</td>
		<td>Presupuesto, ControlCostos</td>
	</tr>
	<tr>
		<td>Partidas</td>
		<td>Implementado</td>
		<td>Crear partidas y estructura jerárquica</td>
		<td>`rest/partida/controller/PartidaController.java`</td>
		<td>Presupuesto</td>
	</tr>
	<tr>
		<td>Compras / Consumo</td>
		<td>Implementado</td>
		<td>Registro de compras y consumo por partida</td>
		<td>`rest/compra/controller/CompraController.java`</td>
		<td>ConsumoPartida, Compra</td>
	</tr>
	<tr>
		<td>Inventario</td>
		<td>Implementado</td>
		<td>Consulta inventario por proyecto</td>
		<td>`rest/inventario/controller/InventarioController.java`</td>
		<td>InventarioItem</td>
	</tr>
	<tr>
		<td>Avance físico</td>
		<td>Implementado</td>
		<td>Registrar avance por partida</td>
		<td>`rest/avance/controller/AvanceController.java`</td>
		<td>AvanceFisico</td>
	</tr>
	<tr>
		<td>Estimaciones</td>
		<td>Implementado</td>
		<td>Generación de estimaciones</td>
		<td>`rest/estimacion/controller/EstimacionController.java`</td>
		<td>Estimacion</td>
	</tr>
	<tr>
		<td>Reajustes</td>
		<td>Implementado</td>
		<td>Cálculo reajustes de costos</td>
		<td>`rest/reajuste/controller/ReajusteController.java`</td>
		<td>Reajuste</td>
	</tr>
	<tr>
		<td>Alertas</td>
		<td>Implementado</td>
		<td>Analítica paramétrica</td>
		<td>`rest/alertas/controller/AnalisisController.java`</td>
		<td>AlertasParametricas</td>
	</tr>
	<tr>
		<td>Leads (Marketing)</td>
		<td>Implementado</td>
		<td>Captación pública de leads + email</td>
		<td>`rest/publico/controller/PublicController.java`<br>`notification/EmailNotificationService.java`</td>
		<td>Resend</td>
	</tr>
</table>

### Frontend (pantallas y módulos)
<table fit-page-width="true" header-row="true">
	<tr>
		<td>Módulo</td>
		<td>Estado</td>
		<td>Pantallas</td>
		<td>Archivos clave</td>
	</tr>
	<tr>
		<td>Landing</td>
		<td>Implementado</td>
		<td>Landing + demo + login + register</td>
		<td>`frontend/src/app/landing/page.tsx`<br>`frontend/src/components/landing/DemoForm.tsx`</td>
	</tr>
	<tr>
		<td>Login / Registro</td>
		<td>Implementado</td>
		<td>`/login`, `/register`</td>
		<td>`frontend/src/app/login/page.tsx`<br>`frontend/src/app/register/page.tsx`</td>
	</tr>
	<tr>
		<td>Proyectos</td>
		<td>Implementado</td>
		<td>`/proyectos/[id]`</td>
		<td>`frontend/src/app/proyectos/[id]/page.tsx`</td>
	</tr>
	<tr>
		<td>Presupuesto</td>
		<td>Implementado</td>
		<td>`/proyectos/[id]/presupuesto`</td>
		<td>`frontend/src/app/proyectos/[id]/presupuesto/page.tsx`</td>
	</tr>
	<tr>
		<td>Producción (RPC)</td>
		<td>Implementado</td>
		<td>`/proyectos/[id]/produccion/nuevo`</td>
		<td>`frontend/src/app/proyectos/[id]/produccion/nuevo/page.tsx`</td>
	</tr>
</table>

---

## Etapa 2 — Comportamiento real (reglas y límites)

### Reglas identificadas (extracto)
<table fit-page-width="true" header-row="true">
	<tr>
		<td>Regla</td>
		<td>Ubicación</td>
		<td>Estado</td>
		<td>Error/Mensaje</td>
	</tr>
	<tr>
		<td>No se permite fecha futura en RPC</td>
		<td>`application/produccion/validation/ProduccionValidator.java`</td>
		<td>Cumple</td>
		<td>"La fecha del reporte no puede ser futura."</td>
	</tr>
	<tr>
		<td>No exceder metrado vigente</td>
		<td>`application/produccion/validation/ProduccionValidator.java`</td>
		<td>Cumple</td>
		<td>"La cantidad reportada excede el saldo disponible de la partida. Requiere Orden de Cambio."</td>
	</tr>
	<tr>
		<td>Proyecto en EJECUCIÓN para reportar</td>
		<td>`application/produccion/validation/ProduccionValidator.java`</td>
		<td>Cumple</td>
		<td>"No se puede reportar avance en un proyecto que no está en EJECUCION."</td>
	</tr>
	<tr>
		<td>Solo aprobar/rechazar si PENDIENTE</td>
		<td>`application/produccion/service/ProduccionServiceImpl.java`</td>
		<td>Cumple</td>
		<td>"Solo se puede aprobar/rechazar un reporte en estado PENDIENTE."</td>
	</tr>
	<tr>
		<td>Email único en registro</td>
		<td>`rest/auth/controller/AuthController.java`</td>
		<td>Cumple</td>
		<td>"El email ya está registrado."</td>
	</tr>
</table>

### Estados y transiciones (ejemplo)
- RPC: **PENDIENTE → APROBADO / RECHAZADO**  
- Usuario: **activo true/false** (sin transición explícita en UI)

---

## Etapa 3 — Flujo de usuario (lo que el usuario puede hacer)

### Flujos críticos
1) **Demo pública** → `/demo` → POST `/api/public/v1/demo-request` → email Resend.  
2) **Registro** → `/register` → POST `/api/v1/auth/register` → token.  
3) **Login** → `/login` → POST `/api/v1/auth/login` → token.  
4) **RPC** → `/proyectos/[id]/produccion/nuevo` → POST `/api/v1/proyectos/{id}/produccion`.

### Consistencia UI vs backend
- **RPC**: UI muestra error 409 con mensaje exacto del backend (OK).  
- **Login**: UI depende de token local; backend exige JWT para endpoints protegidos (OK).  
- **Presupuesto**: UI usa endpoints reales, pero algunas estructuras (árbol) aún contienen mock en servicio (revisar).

---

## Etapa 4 — Dolor resuelto (valor de negocio actual)

### Tabla Dolor → Módulo → Evidencia
<table fit-page-width="true" header-row="true">
	<tr>
		<td>Dolor</td>
		<td>Módulo</td>
		<td>Evidencia en código</td>
	</tr>
	<tr>
		<td>Avance físico inflado / inconsistente</td>
		<td>Producción (RPC)</td>
		<td>Validación de metrado y estado de proyecto</td>
	</tr>
	<tr>
		<td>Falta trazabilidad en costos</td>
		<td>Control costos</td>
		<td>Reporte `/presupuestos/{id}/control-costos`</td>
	</tr>
	<tr>
		<td>Captación de prospectos</td>
		<td>Leads</td>
		<td>POST `/api/public/v1/demo-request` + Resend</td>
	</tr>
</table>

---

## Etapa 5 — Brecha con plan de negocio

### Brechas prioritarias (inicial)
<table fit-page-width="true" header-row="true">
	<tr>
		<td>Brecha</td>
		<td>Impacto</td>
		<td>Módulo afectado</td>
		<td>Acción recomendada</td>
	</tr>
	<tr>
		<td>Orden de Cambio no implementada (solo mensaje)</td>
		<td>Alto</td>
		<td>Producción (RPC)</td>
		<td>Crear flujo completo de orden de cambio</td>
	</tr>
	<tr>
		<td>Árbol de partidas en frontend usa mock</td>
		<td>Medio</td>
		<td>Presupuesto</td>
		<td>Conectar a endpoint real y remover mock</td>
	</tr>
	<tr>
		<td>Estados UI no reflejan estados de negocio para todos los módulos</td>
		<td>Medio</td>
		<td>Frontend general</td>
		<td>Mapear estados y estandarizar enums</td>
	</tr>
</table>

---

## Pendientes para completar el reporte
- Revisar reglas específicas de Compras, Inventario, Estimaciones y Reajustes.  
- Completar tabla de estados y transiciones para Presupuesto/Proyecto.  
- Validar dependencia real de la UI vs datos reales en todas las pantallas.
