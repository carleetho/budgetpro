#!/bin/bash
# Script para crear commits atómicos de REQ-64 (period consistency)
# Ejecutar desde la raíz del repositorio: ./scripts/atomic-commits-req64.sh
#
# Si git commit falla con "unknown option trailer":
#   1. Cursor: desactivar Attribution en Settings > Agents > Attribution
#   2. O ejecutar: git config core.hooksPath /dev/null (luego --unset)
# Ver: docs/TROUBLESHOOTING_GIT_COMMIT.md

set -e
cd "$(git rev-parse --show-toplevel)"

# Reset si hay algo en staging
git reset HEAD 2>/dev/null || true

echo "=== Commit 1: Dominio Proyecto + FrecuenciaControl ==="
git add \
  backend/src/main/java/com/budgetpro/domain/finanzas/proyecto/model/FrecuenciaControl.java \
  backend/src/main/java/com/budgetpro/domain/proyecto/model/Proyecto.java \
  backend/src/main/resources/db/migration/V24__add_frecuencia_control_to_proyecto.sql \
  backend/src/main/resources/db/migration/V25__add_fecha_inicio_to_proyecto.sql \
  backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/ProyectoEntity.java \
  backend/src/main/java/com/budgetpro/infrastructure/persistence/mapper/ProyectoMapper.java \
  backend/src/main/java/com/budgetpro/domain/proyecto/port/out/ProyectoRepository.java \
  backend/src/main/java/com/budgetpro/infrastructure/persistence/adapter/ProyectoRepositoryAdapter.java \
  backend/src/main/java/com/budgetpro/infrastructure/persistence/repository/ProyectoJpaRepository.java \
  backend/src/test/java/com/budgetpro/domain/proyecto/model/ProyectoFrecuenciaTest.java \
  backend/src/test/java/com/budgetpro/infrastructure/persistence/adapter/ProyectoRepositoryAdapterTest.java
git commit -m "feat(REQ-64): add FrecuenciaControl and Proyecto period consistency

- Add FrecuenciaControl enum (SEMANAL, QUINCENAL, MENSUAL)
- Add fechaInicio and frecuenciaControl to Proyecto
- Add configurarFrecuencia and esFechaCorteValida
- Add migrations V24 and V25 for proyecto table
- Add ProyectoRepository.findAllWithFrecuenciaControl
- Add ProyectoFrecuenciaTest and ProyectoRepositoryAdapterTest"

echo "=== Commit 2: EVMTimeSeriesRepository existsByProyectoIdAndFechaCorte ==="
git add \
  backend/src/main/java/com/budgetpro/domain/finanzas/evm/port/out/EVMTimeSeriesRepository.java \
  backend/src/main/java/com/budgetpro/infrastructure/persistence/adapter/evm/EVMTimeSeriesRepositoryAdapter.java \
  backend/src/main/java/com/budgetpro/infrastructure/persistence/repository/evm/JpaEVMTimeSeriesRepository.java
git commit -m "feat(REQ-64): add existsByProyectoIdAndFechaCorte to EVMTimeSeriesRepository

- Used by scheduler to avoid duplicate period closures"

echo "=== Commit 3: CerrarPeriodoUseCase and service ==="
git add \
  backend/src/main/java/com/budgetpro/application/finanzas/evm/exception/ \
  backend/src/main/java/com/budgetpro/application/finanzas/evm/port/in/CerrarPeriodoUseCase.java \
  backend/src/main/java/com/budgetpro/application/finanzas/evm/service/ \
  backend/src/test/java/com/budgetpro/application/finanzas/evm/service/
git commit -m "feat(REQ-64): add CerrarPeriodoUseCase and CerrarPeriodoService

- CerrarPeriodoUseCase returns periodoId (PER-fechaCorte)
- PeriodoFechaInvalidaException for unaligned dates
- Validates against proyecto.frecuenciaControl and fechaInicio"

echo "=== Commit 4: REST endpoint cerrar-periodo ==="
git add \
  backend/src/main/java/com/budgetpro/infrastructure/rest/evm/dto/CerrarPeriodoRequest.java \
  backend/src/main/java/com/budgetpro/infrastructure/rest/evm/dto/CerrarPeriodoResponse.java \
  backend/src/main/java/com/budgetpro/infrastructure/rest/evm/controller/EVMController.java \
  backend/src/main/java/com/budgetpro/infrastructure/rest/controller/GlobalExceptionHandler.java \
  backend/src/test/java/com/budgetpro/infrastructure/rest/evm/controller/EVMControllerTest.java \
  backend/src/test/java/com/budgetpro/infrastructure/rest/evm/controller/EVMControllerCerrarPeriodoIT.java \
  backend/src/test/java/com/budgetpro/infrastructure/rest/evm/controller/EVMControllerForecastTest.java
git commit -m "feat(REQ-64): add REST endpoint POST /evm/{id}/cerrar-periodo

- Returns periodoId matching ValuacionCerradaEvent for traceability
- GlobalExceptionHandler for PeriodoFechaInvalidaException 422"

echo "=== Commit 5: EVMPeriodoCierreScheduler ==="
git add \
  backend/src/main/java/com/budgetpro/BudgetProApplication.java \
  backend/src/main/java/com/budgetpro/infrastructure/scheduler/ \
  backend/src/test/java/com/budgetpro/infrastructure/scheduler/
git commit -m "feat(REQ-64): add EVMPeriodoCierreScheduler for automatic period closure

- @Scheduled daily 00:05 UTC
- Processes projects with frecuenciaControl configured
- Skips duplicates via existsByProyectoIdAndFechaCorte
- EnableScheduling in BudgetProApplication"

echo "=== Commit 6: Documentation ==="
git add docs/canonical/modules/EVM_MODULE_CANONICAL.md
git commit -m "docs: update EVM_MODULE_CANONICAL for REQ-64 period consistency"

echo ""
echo "=== Commits creados. Quedan sin commitear: ==="
git status
echo ""
echo "Opcional: .budgetpro/metrics.json (chore) - normalmente no se versiona"
