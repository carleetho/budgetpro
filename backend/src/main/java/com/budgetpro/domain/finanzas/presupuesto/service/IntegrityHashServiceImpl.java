package com.budgetpro.domain.finanzas.presupuesto.service;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.infrastructure.observability.IntegrityEventLogger;
import com.budgetpro.infrastructure.observability.IntegrityMetrics;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de integridad criptográfica para presupuestos.
 * 
 * Utiliza SHA-256 para calcular hashes determinísticos y Merkle Tree para
 * agregar eficientemente las partidas del presupuesto.
 * 
 * **Algoritmo:**
 * - SHA-256 para hashing individual
 * - Merkle Tree para agregación de partidas (O(n log n))
 * - Determinístico: mismo input → mismo output
 * 
 * **Versión:** SHA-256-v1
 */
@Service
public class IntegrityHashServiceImpl implements IntegrityHashService {

    private static final String ALGORITHM = "SHA-256";
    private static final String VERSION = "v1";
    private static final String ALGORITHM_VERSION = "SHA-256-v1";

    private final PartidaRepository partidaRepository;
    private final ApuSnapshotRepository apuSnapshotRepository;
    private final IntegrityMetrics metrics;
    private final IntegrityEventLogger eventLogger;

    public IntegrityHashServiceImpl(PartidaRepository partidaRepository,
                                    ApuSnapshotRepository apuSnapshotRepository,
                                    IntegrityMetrics metrics,
                                    IntegrityEventLogger eventLogger) {
        this.partidaRepository = Objects.requireNonNull(partidaRepository, "PartidaRepository no puede ser nulo");
        this.apuSnapshotRepository = Objects.requireNonNull(apuSnapshotRepository, "ApuSnapshotRepository no puede ser nulo");
        this.metrics = Objects.requireNonNull(metrics, "IntegrityMetrics no puede ser nulo");
        this.eventLogger = Objects.requireNonNull(eventLogger, "IntegrityEventLogger no puede ser nulo");
    }

    /**
     * Calcula el hash criptográfico de aprobación basado en la estructura del presupuesto.
     * 
     * Incluye:
     * - Atributos raíz del presupuesto (ID, nombre, proyectoId, estado, esContractual)
     * - Merkle root de todas las partidas (incluyendo estructura jerárquica y APUs)
     * - Metadata de versión del algoritmo
     * 
     * @param presupuesto El presupuesto del cual calcular el hash
     * @return Hash SHA-256 de 64 caracteres hexadecimales
     */
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

    /**
     * Calcula el hash criptográfico de ejecución basado en el estado financiero actual.
     * 
     * Encadena al hash de aprobación y agrega:
     * - Hash de aprobación (base inmutable)
     * - Estado financiero de cada partida (gastos reales, compromisos pendientes)
     * - Timestamp de cálculo (para detectar cambios en el tiempo)
     * 
     * @param presupuesto El presupuesto del cual calcular el hash
     * @return Hash SHA-256 de 64 caracteres hexadecimales
     * @throws IllegalStateException si el presupuesto no tiene hash de aprobación
     */
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

    /**
     * Calcula el Merkle root de todas las partidas del presupuesto.
     * 
     * Proceso:
     * 1. Calcula hash individual de cada partida (incluyendo APU si existe)
     * 2. Ordena los hashes para determinismo
     * 3. Construye Merkle tree recursivamente
     * 
     * @param partidas Lista de partidas del presupuesto
     * @return Merkle root hash (64 caracteres hex)
     */
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

    /**
     * Calcula el hash de una partida individual incluyendo su APU snapshot si existe.
     * 
     * Incluye:
     * - Atributos estructurales de la partida (ID, item, descripción, metrado, presupuesto asignado)
     * - Estructura jerárquica (padreId, nivel)
     * - APU snapshot si existe (externalApuId, rendimientoVigente, insumos)
     * 
     * @param partida La partida de la cual calcular el hash
     * @return Hash SHA-256 de 64 caracteres hexadecimales
     */
    private String calculatePartidaHash(Partida partida) {
        StringBuilder data = new StringBuilder();

        // Partida structural attributes
        data.append(partida.getId().getValue());
        data.append(partida.getItem());
        data.append(partida.getDescripcion());
        data.append(partida.getUnidad());
        data.append(partida.getMetrado());
        data.append(partida.getPresupuestoAsignado());
        data.append(partida.getPadreId()); // Hierarchical structure
        data.append(partida.getNivel());

        // APU snapshot if exists (use rendimientoVigente, not original)
        Optional<APUSnapshot> apuOpt = apuSnapshotRepository.findByPartidaId(partida.getId().getValue());
        if (apuOpt.isPresent()) {
            APUSnapshot apu = apuOpt.get();
            data.append(apu.getExternalApuId());
            data.append(apu.getCatalogSource());
            data.append(apu.getRendimientoVigente()); // Use vigente, not original
            data.append(apu.getUnidadSnapshot());

            // Include all insumos in deterministic order
            List<APUInsumoSnapshot> insumos = apu.getInsumos();
            for (APUInsumoSnapshot insumo : insumos) {
                data.append(insumo.getRecursoExternalId());
                data.append(insumo.getRecursoNombre());
                data.append(insumo.getCantidad());
                data.append(insumo.getPrecioUnitario());
                data.append(insumo.getSubtotal());
            }
        }

        return calculateSHA256(data.toString());
    }

    /**
     * Calcula el Merkle root de una lista de hashes recursivamente.
     * 
     * Algoritmo:
     * - Si hay 0 hashes: retorna hash de string vacío
     * - Si hay 1 hash: retorna ese hash
     * - Si hay 2+ hashes: agrupa en pares, hashea cada par, y recursa
     * 
     * Complejidad: O(n log n)
     * 
     * @param hashes Lista de hashes a agregar
     * @return Merkle root hash (64 caracteres hex)
     */
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

    /**
     * Calcula el hash SHA-256 de un string.
     * 
     * @param data El string a hashear
     * @return Hash SHA-256 de 64 caracteres hexadecimales
     * @throws RuntimeException si SHA-256 no está disponible (no debería ocurrir)
     */
    private String calculateSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Convierte un array de bytes a string hexadecimal.
     * 
     * @param bytes El array de bytes a convertir
     * @return String hexadecimal de 64 caracteres (32 bytes * 2)
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
