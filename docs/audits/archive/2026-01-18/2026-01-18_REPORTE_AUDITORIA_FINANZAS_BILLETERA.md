# Reporte de Auditoría — FINANZAS / BILLETERA

Fecha: 2026-01-18  
Alcance: `backend/src/main/java/**/finanzas/**` y casos de uso con ingreso/egreso de dinero.

## Reglas detectadas (en código)
- **No existe saldo negativo**: validación explícita en `Billetera.egresar` con excepción de dominio.
- **Todo cambio de saldo genera movimiento**: `ingresar/egresar` crean `MovimientoCaja` y lo registran en `movimientosNuevos`.
- **No hay dinero sin movimiento**: el saldo se actualiza únicamente en `ingresar/egresar`.
- **Movimiento válido**: `MovimientoCaja` exige monto positivo y referencia no vacía.

## Reglas faltantes (no implementadas o no exigidas)
- **Evento formal para ingreso/egreso**: no existe modelado ni manejo de eventos `CobroAprobado`, `PagoAutorizado`, `MovimientoBilletera` o `AlertaFaltaLiquidez`.
- **Ingreso con evidencia contractual**: el campo `evidenciaUrl` es opcional y no es validado.
- **Separación explícita** `Billetera ≠ Presupuesto ≠ Contabilidad`: no hay restricciones ni salvaguardas explícitas en código.

## Violaciones detectadas
- **Ingreso en billetera al aprobar estimación** sin evento formal y sin evidencia contractual: se registra ingreso directo con `evidenciaUrl = null`.
- **Creación implícita de billetera** al aprobar estimación cuando no existe.
- **Egreso en registro de compra**: la compra descuenta billetera durante el registro, contradiciendo “comprar no es pagar”.

## Severidad
- **Alta**:
  - Ingreso en billetera al aprobar estimación sin evento formal ni evidencia contractual.
  - Egreso al registrar compra (compra ≠ pago).
- **Media/Alta**:
  - Creación implícita de billetera en aprobación de estimación.
- **Media**:
  - Ausencia de mecanismos explícitos para separar billetera de presupuesto/contabilidad.

