# Reporte de Cambio — Anticipo y Amortización

## ID
- VD-01
- FG-02

## Resumen del cambio
- Se eliminó el placeholder de saldo de anticipo.
- Se introdujo registro auditable de movimientos de anticipo.

## Archivos modificados
- `backend/src/main/java/com/budgetpro/domain/finanzas/anticipo/model/TipoMovimientoAnticipo.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/anticipo/model/AnticipoMovimiento.java`
- `backend/src/main/java/com/budgetpro/domain/finanzas/anticipo/port/out/AnticipoMovimientoRepository.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/anticipo/AnticipoMovimientoEntity.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/repository/anticipo/AnticipoMovimientoJpaRepository.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/mapper/anticipo/AnticipoMovimientoMapper.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/adapter/anticipo/AnticipoMovimientoRepositoryAdapter.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`
- `backend/src/main/resources/db/migration/V21__create_anticipo_movimiento_schema.sql`

## Impacto y riesgos
- Impacto: la amortización se calcula con saldo real y se registra.
- Riesgo: requiere registrar anticipos reales para habilitar amortización.

