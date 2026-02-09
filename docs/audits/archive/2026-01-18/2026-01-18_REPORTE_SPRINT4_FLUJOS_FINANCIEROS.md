# Reporte — Sprint 4 (Flujos Contractuales y Financieros)

## Responsable
- Arquitecto Financiero / Backend Lead

## Alcance
- Evidencia obligatoria en cobros
- Anticipos y amortizaciones con fuente real
- Ingreso en billetera con evidencia

## Evidencia revisada
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/anticipo/model/AnticipoMovimiento.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/anticipo/port/out/AnticipoMovimientoRepository.java`
- `backend/src/main/resources/db/migration/V20__add_estimacion_evidencia.sql`
- `backend/src/main/resources/db/migration/V21__create_anticipo_movimiento_schema.sql`

## Resultado
- La aprobación de estimación exige evidencia contractual previa.
- El ingreso a billetera rechaza evidencia nula/vacía.
- La amortización de anticipo se calcula desde saldo real registrado.
- La amortización queda registrada como movimiento de anticipo.

## Riesgos / pendientes
- Requiere registrar anticipos reales para habilitar amortizaciones.
- Evidencia debe ser provista en el flujo de generación de estimaciones para aprobar.

