# Reporte de Cambio — Clasificación de Compras

## ID
- CD-03

## Resumen del cambio
- Se habilitó compra sin partida siempre que exista clasificación obligatoria.
- Se agregó clasificación al detalle de compra.

## Archivos modificados
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/NaturalezaGasto.java`
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/RelacionContractual.java`
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/RubroInsumo.java`
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/CompraDetalle.java`
- `backend/src/main/java/com/budgetpro/application/compra/dto/CompraDetalleCommand.java`
- `backend/src/main/java/com/budgetpro/application/compra/dto/CompraDetalleResponse.java`
- `backend/src/main/java/com/budgetpro/infrastructure/rest/compra/dto/CompraDetalleRequest.java`
- `backend/src/main/java/com/budgetpro/infrastructure/rest/compra/controller/CompraController.java`
- `backend/src/main/java/com/budgetpro/application/compra/usecase/RegistrarCompraUseCaseImpl.java`
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/compra/CompraDetalleEntity.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/mapper/compra/CompraMapper.java`
- `backend/src/main/resources/db/migration/V22__update_compra_detalle_clasificacion.sql`

## Impacto y riesgos
- Impacto: clasificación obligatoria y compra sin partida permitida.
- Riesgo: requiere actualizar integraciones para enviar clasificación.

