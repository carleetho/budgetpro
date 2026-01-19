# Swiss-Grade Budget Integrity - Implementation Guide

**Fecha:** 2026-01-19  
**Versión:** 1.0  
**Propósito:** Guía técnica completa de la implementación de integridad criptográfica de presupuestos

---

## Tabla de Contenidos

1. [Overview](#overview)
2. [Dual-Hash Pattern](#dual-hash-pattern)
3. [Merkle Tree Implementation](#merkle-tree-implementation)
4. [Integration Points](#integration-points)
5. [Performance Characteristics](#performance-characteristics)
6. [Code References](#code-references)
7. [Troubleshooting](#troubleshooting)

---

## Overview

BudgetPro implementa sellado criptográfico de presupuestos usando hashing SHA-256 dual con validación mediante Merkle Tree. Este sistema garantiza que:

- **Estructura inmutable**: Una vez aprobado, el presupuesto no puede modificarse estructuralmente
- **Detección de tampering**: Cualquier modificación no autorizada se detecta antes de permitir transacciones
- **Audit trail completo**: Todos los eventos de integridad se registran para análisis forense
- **Validación preventiva**: Las compras y egresos validan integridad antes de ejecutarse

### Algoritmo

- **Hash Function**: SHA-256 (64 caracteres hexadecimales)
- **Versión**: SHA-256-v1
- **Merkle Tree**: O(n log n) para agregación de Partidas
- **Determinismo**: Mismo input → mismo output (sin timestamps en approval hash)

---

## Dual-Hash Pattern

### Approval Hash (Inmutable)

Captura la estructura del presupuesto al momento de aprobación:

**Componentes incluidos:**
- Atributos raíz del Presupuesto:
  - `id`, `nombre`, `proyectoId`, `estado`, `esContractual`
- Merkle Root de Partidas:
  - Cada Partida incluye: `id`, `item`, `descripción`, `unidad`, `metrado`, `presupuestoAsignado`, `padreId`, `nivel`
  - APU Snapshot asociado (si existe): `externalApuId`, `catalogSource`, `rendimientoVigente`, `unidadSnapshot`, `insumos`
- Metadata:
  - Versión del algoritmo (`SHA-256-v1`)

**Características:**
- **Inmutable**: No cambia después de aprobación
- **Determinístico**: Mismo presupuesto → mismo hash
- **Completo**: Captura toda la estructura jerárquica

**Referencia de código:**
```62:83:backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashServiceImpl.java
    @Override
    public String calculateApprovalHash(Presupuesto presupuesto) {
        Objects.requireNonNull(presupuesto, "El presupuesto no puede ser nulo");

        long startTime = System.currentTimeMillis();
        String correlationId = eventLogger.generateCorrelationId();

        StringBuilder data = new StringBuilder();

        // Presupuesto root attributes (estructura inmutable)
        data.append(presupuesto.getId().getValue());
        data.append(presupuesto.getNombre());
        data.append(presupuesto.getProyectoId());
        data.append(presupuesto.getEstado());
        data.append(presupuesto.getEsContractual());

        // Partidas Merkle root (incluye estructura jerárquica y APUs)
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuesto.getId().getValue());
        String partidasMerkleRoot = calculatePartidasMerkleRoot(partidas);
        data.append(partidasMerkleRoot);

        // Metadata (versión del algoritmo para future-proofing)
        data.append(VERSION);

        String hash = calculateSHA256(data.toString());
        
        // Record metrics and logging
        long duration = System.currentTimeMillis() - startTime;
        metrics.recordHashGeneration(duration, partidas.size(), "approval_hash", ALGORITHM_VERSION);
        eventLogger.logHashGeneration(
                correlationId,
                presupuesto.getId().getValue(),
                hash,
                null, // execution hash not calculated yet
                duration,
                partidas.size(),
                ALGORITHM_VERSION
        );

        return hash;
    }
```

### Execution Hash (Dinámico)

Captura el estado financiero actual del presupuesto:

**Componentes incluidos:**
- Hash de aprobación (encadenado, base inmutable)
- Estado financiero por Partida:
  - `id`, `gastosReales`, `compromisosPendientes`, `saldoDisponible`
- Timestamp de cálculo (para detectar cambios en el tiempo)

**Características:**
- **Dinámico**: Se actualiza después de cada transacción financiera
- **Encadenado**: Depende del hash de aprobación
- **Temporal**: Incluye timestamp para detección de cambios

**Referencia de código:**
```98:123:backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashServiceImpl.java
    @Override
    public String calculateExecutionHash(Presupuesto presupuesto) {
        Objects.requireNonNull(presupuesto, "El presupuesto no puede ser nulo");

        if (presupuesto.getIntegrityHashApproval() == null) {
            throw new IllegalStateException("Cannot calculate execution hash without approval hash");
        }

        long startTime = System.currentTimeMillis();
        String correlationId = eventLogger.generateCorrelationId();

        StringBuilder data = new StringBuilder();

        // Chain to approval hash (base immutable)
        data.append(presupuesto.getIntegrityHashApproval());

        // Financial state of each partida
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuesto.getId().getValue());
        for (Partida partida : partidas) {
            data.append(partida.getId().getValue());
            data.append(partida.getGastosReales());
            data.append(partida.getCompromisosPendientes());
            data.append(partida.getSaldoDisponible());
        }

        // Timestamp for change detection
        data.append(LocalDateTime.now().toString());

        String hash = calculateSHA256(data.toString());
        
        // Record metrics and logging
        long duration = System.currentTimeMillis() - startTime;
        metrics.recordHashGeneration(duration, partidas.size(), "execution_hash", ALGORITHM_VERSION);
        eventLogger.logHashGeneration(
                correlationId,
                presupuesto.getId().getValue(),
                presupuesto.getIntegrityHashApproval(),
                hash,
                duration,
                partidas.size(),
                ALGORITHM_VERSION
        );

        return hash;
    }
```

---

## Merkle Tree Implementation

El Merkle Tree se usa para agregar eficientemente todas las Partidas del presupuesto en un solo hash determinístico.

### Estructura

```
                    Merkle Root
                   /            \
              Level 1          Level 1
             /      \         /      \
        Partida1  Partida2  Partida3  Partida4
```

### Algoritmo

1. **Hash Individual**: Cada Partida se hashea incluyendo su APU Snapshot (si existe)
2. **Ordenamiento**: Los hashes se ordenan alfabéticamente para determinismo
3. **Agregación Recursiva**: Se agrupan en pares, se hashea cada par, y se recursa hasta obtener un solo root

**Referencia de código:**
```136:148:backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashServiceImpl.java
    private String calculatePartidasMerkleRoot(List<Partida> partidas) {
        if (partidas == null || partidas.isEmpty()) {
            return calculateSHA256(""); // Empty Merkle root
        }

        // Calculate individual partida hashes and sort for determinism
        List<String> partidaHashes = partidas.stream()
                .map(this::calculatePartidaHash)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        return calculateMerkleRoot(partidaHashes);
    }
```

```210:229:backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashServiceImpl.java
    private String calculateMerkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) {
            return calculateSHA256("");
        }
        if (hashes.size() == 1) {
            return hashes.get(0);
        }

        // Pair up hashes and hash each pair
        List<String> nextLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) {
            String left = hashes.get(i);
            // If odd number, duplicate last hash
            String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
            nextLevel.add(calculateSHA256(left + right));
        }

        // Recurse until single root
        return calculateMerkleRoot(nextLevel);
    }
```

### Complejidad

- **Tiempo**: O(n log n) donde n es el número de Partidas
- **Espacio**: O(n) para almacenar hashes intermedios
- **Determinismo**: Garantizado por ordenamiento alfabético

---

## Integration Points

### 1. Budget Approval

**Ubicación**: `AprobarPresupuestoUseCaseImpl.aprobar()`

**Flujo:**
1. Valida que el presupuesto no esté ya aprobado
2. Valida que todas las partidas hoja tengan APU
3. Llama a `presupuesto.aprobar(userId, integrityHashService)`
4. Genera ambos hashes (approval + execution)
5. Cambia estado a `CONGELADO`
6. Registra evento en audit log

**Referencia de código:**
```34:64:backend/src/main/java/com/budgetpro/application/presupuesto/usecase/AprobarPresupuestoUseCaseImpl.java
    @Override
    @Transactional
    public void aprobar(UUID presupuestoId) {
        // 1. Buscar el presupuesto
        PresupuestoId id = PresupuestoId.from(presupuestoId);
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNoEncontradoException(presupuestoId));

        // 2. Validar que el presupuesto no esté ya aprobado
        if (presupuesto.isAprobado()) {
            throw new PresupuestoNoPuedeAprobarseException(presupuestoId, "El presupuesto ya está aprobado");
        }

        // 3. Validar que todas las partidas hoja tengan APU
        if (!calculoPresupuestoService.todasLasPartidasHojaTienenAPU(presupuestoId)) {
            throw new PresupuestoNoPuedeAprobarseException(presupuestoId,
                    "No todas las partidas hoja tienen APU asociado");
        }

        // 4. Recalcular el presupuesto (validación implícita)
        // El cálculo se realiza al consultar el presupuesto, pero aquí validamos que sea posible
        calculoPresupuestoService.calcularCostoTotal(presupuestoId);

        // 5. Aprobar el presupuesto (cambia estado a CONGELADO, marca como contractual y genera hashes de integridad)
        // TODO: Obtener el ID del usuario actual del contexto de seguridad
        UUID approvedBy = UUID.randomUUID(); // Placeholder - debe obtenerse del contexto de seguridad
        presupuesto.aprobar(approvedBy, integrityHashService);

        // 6. Persistir los cambios
        presupuestoRepository.save(presupuesto);
    }
```

### 2. Purchase Approval

**Ubicación**: `ProcesarCompraService.procesar()`

**Flujo:**
1. Busca el Presupuesto del proyecto
2. Si está aprobado, valida integridad antes de procesar
3. Si validación exitosa, procesa la compra
4. Después de procesar, actualiza hash de ejecución

**Referencia de código:**
```84:95:backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java
        // Validar integridad del presupuesto (solo si fue aprobado y tiene hash)
        if (presupuesto.isAprobado()) {
            String correlationId = eventLogger.generateCorrelationId();
            long validationStartTime = System.currentTimeMillis();
            
            try {
                presupuesto.validarIntegridad(integrityHashService);
                
                // Registrar validación exitosa en audit log y métricas
                long validationDuration = System.currentTimeMillis() - validationStartTime;
                metrics.recordHashValidation(true, validationDuration, "SHA-256-v1");
                eventLogger.logHashValidation(
```

### 3. Expense Transaction

**Ubicación**: `Billetera.egresar()`

**Flujo:**
1. Valida regla CD-04 (evidencia pendiente)
2. Valida integridad del presupuesto (si está aprobado)
3. Valida saldo suficiente
4. Ejecuta el egreso

**Referencia de código:**
```149:152:backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java
        // CRÍTICO: Validar integridad criptográfica del presupuesto ANTES de permitir egreso
        // Solo si el presupuesto fue aprobado y tiene hash de integridad
        if (presupuesto != null && presupuesto.isAprobado()) {
            presupuesto.validarIntegridad(hashService);
        }
```

---

## Performance Characteristics

### Benchmarks

- **Hash Generation**: <100ms para 100 partidas
- **Hash Validation**: <100ms
- **Merkle Tree Construction**: O(n log n) donde n = número de partidas

### Optimizaciones

1. **Caching**: Los hashes se calculan solo cuando es necesario
2. **Lazy Loading**: Las Partidas y APU Snapshots se cargan bajo demanda
3. **Determinismo**: El ordenamiento garantiza que no se recalculen hashes innecesariamente

### Monitoring

Todas las operaciones emiten métricas:
- `budget.integrity.hash_generated.total`
- `budget.integrity.hash_calculation.duration` (p50, p95, p99)
- `budget.integrity.hash_validated.total`
- `budget.integrity.validation.duration` (p50, p95, p99)
- `budget.integrity.violations.total` (CRITICAL)

---

## Code References

### Domain Models

- **Presupuesto**: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/model/Presupuesto.java`
  - Método `aprobar()`: Genera hashes y sella el presupuesto
  - Método `validarIntegridad()`: Valida hash de aprobación
  - Método `actualizarHashEjecucion()`: Actualiza hash de ejecución

### Services

- **IntegrityHashService**: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashService.java`
- **IntegrityHashServiceImpl**: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityHashServiceImpl.java`
- **IntegrityAuditLog**: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/IntegrityAuditLog.java`

### Infrastructure

- **IntegrityMetrics**: `backend/src/main/java/com/budgetpro/infrastructure/observability/IntegrityMetrics.java`
- **IntegrityEventLogger**: `backend/src/main/java/com/budgetpro/infrastructure/observability/IntegrityEventLogger.java`

### Exceptions

- **BudgetIntegrityViolationException**: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/exception/BudgetIntegrityViolationException.java`

---

## Troubleshooting

### Violación de Integridad Detectada

**Síntoma**: `BudgetIntegrityViolationException` lanzada durante aprobación de compra o egreso

**Causas posibles:**
1. Modificación directa en base de datos (tampering)
2. Corrupción de datos
3. Bug en cálculo de hash
4. Cambio no autorizado de estructura

**Pasos de diagnóstico:**
1. Consultar audit log:
   ```sql
   SELECT * FROM presupuesto_integrity_audit 
   WHERE presupuesto_id = ? 
   ORDER BY validated_at DESC;
   ```
2. Comparar `expected_hash` vs `actual_hash`
3. Revisar logs estructurados para contexto completo
4. Verificar métricas: `budget.integrity.violations.total`

**Solución:**
- Si es tampering: Investigar acceso no autorizado
- Si es bug: Revisar código de cálculo de hash
- Si es cambio legítimo: Crear nuevo presupuesto (no modificar aprobado)

### Hash Generation Lenta

**Síntoma**: `budget.integrity.hash_calculation.duration` > 100ms

**Causas posibles:**
1. Muchas Partidas (>1000)
2. APU Snapshots grandes
3. Carga lenta de datos

**Solución:**
- Revisar número de Partidas
- Optimizar queries de PartidaRepository
- Considerar paginación para presupuestos muy grandes

### Hash Validation Falla Inesperadamente

**Síntoma**: Validación falla pero no hay cambios visibles

**Causas posibles:**
1. Cambio en orden de Partidas
2. Cambio en formato de datos
3. Problema de encoding

**Solución:**
- Verificar que Partidas estén ordenadas consistentemente
- Revisar logs para ver hash esperado vs actual
- Verificar encoding UTF-8 en todos los strings

---

## Referencias

- [Business Manifesto - Integridad Criptográfica](../context/BUSINESS_MANIFESTO.md#9-principio-de-integridad-criptográfica-swiss-grade)
- [Arquitectura Visual - Diagramas de Integridad](../ARQUITECTURA_VISUAL.md#11-arquitectura-de-integridad-criptográfica)
- [Operational Runbook - Procedimientos Operacionales](./OPERATIONAL_RUNBOOK.md)
