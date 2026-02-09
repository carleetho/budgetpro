# Reporte de Cambio — CD-04 Evidencia en Estimación

## ID
- CD-04
- RS-01

## Resumen del cambio
- Se incorporó evidencia contractual en la estimación.
- La aprobación exige evidencia previa y ya no bloquea permanentemente.

## Archivos modificados
- `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/estimacion/EstimacionEntity.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/mapper/estimacion/EstimacionMapper.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/usecase/AprobarEstimacionUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/dto/GenerarEstimacionCommand.java`
- `backend/src/main/java/com/budgetpro/application/estimacion/dto/EstimacionResponse.java`
- `backend/src/main/java/com/budgetpro/infrastructure/rest/estimacion/dto/GenerarEstimacionRequest.java`
- `backend/src/main/java/com/budgetpro/infrastructure/rest/estimacion/controller/EstimacionController.java`
- `backend/src/main/resources/db/migration/V20__add_estimacion_evidencia.sql`

## Impacto y riesgos
- Impacto: no existe aprobación sin evidencia contractual válida.
- Riesgo: requiere poblar evidencia en nuevas estimaciones para aprobar.

