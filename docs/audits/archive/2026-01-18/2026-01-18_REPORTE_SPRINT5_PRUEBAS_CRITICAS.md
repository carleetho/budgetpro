# Reporte — Sprint 5 (Pruebas Críticas)

## Responsable
- QA Lead

## Alcance
- Estados canónicos (Proyecto / Presupuesto)
- Evidencia obligatoria en estimaciones y billetera
- Compra sin partida con clasificación obligatoria
- Anticipo y amortización con saldo real

## Evidencia revisada
- `backend/src/main/java/com/budgetpro/domain/proyecto/model/EstadoProyecto.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/model/EstadoPresupuesto.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java`
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/CompraDetalle.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/anticipo/model/AnticipoMovimiento.java`

## Resultado
- Pruebas no ejecutadas en esta fase.
- Se recomienda construir casos de prueba mínimos por cada flujo crítico.

## Lista mínima de pruebas sugeridas (sin implementación)
- Estados canónicos: no aceptar legacy en dominio.
- Aprobación de estimación: rechazar sin evidencia.
- Billetera: rechazo de ingreso con evidencia nula.
- Compra sin partida: permitida solo si existe clasificación.
- Anticipo: amortización solo con saldo real registrado.

