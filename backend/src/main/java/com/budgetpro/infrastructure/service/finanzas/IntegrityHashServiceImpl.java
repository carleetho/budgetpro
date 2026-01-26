package com.budgetpro.infrastructure.service.finanzas;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.shared.port.out.ObservabilityPort;
// Importamos la interfaz del servicio de dominio (Define el CONTRATO)
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;

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
 * Implementación de Infraestructura para el servicio de integridad. *
 * UBICACIÓN: Infrastructure Layer (Correcto) RESPONSABILIDAD: Implementar la
 * lógica de hashing y conectar con puertos de salida.
 */
@Service
public class IntegrityHashServiceImpl implements IntegrityHashService {

    private static final String ALGORITHM = "SHA-256";
    private static final String VERSION = "v1";
    private static final String ALGORITHM_VERSION = "SHA-256-v1";

    private final PartidaRepository partidaRepository;
    private final ApuSnapshotRepository apuSnapshotRepository;
    // Unificamos métricas y logs en un solo puerto
    private final ObservabilityPort observabilityPort;

    public IntegrityHashServiceImpl(PartidaRepository partidaRepository, ApuSnapshotRepository apuSnapshotRepository,
            ObservabilityPort observabilityPort) {
        this.partidaRepository = Objects.requireNonNull(partidaRepository, "PartidaRepository no puede ser nulo");
        this.apuSnapshotRepository = Objects.requireNonNull(apuSnapshotRepository,
                "ApuSnapshotRepository no puede ser nulo");
        this.observabilityPort = Objects.requireNonNull(observabilityPort, "ObservabilityPort no puede ser nulo");
    }

    @Override
    public String calculateApprovalHash(Presupuesto presupuesto) {
        Objects.requireNonNull(presupuesto, "El presupuesto no puede ser nulo");

        long startTime = System.currentTimeMillis();
        // Usamos el puerto para generar el ID, desacoplando la implementación del
        // logger
        String correlationId = observabilityPort.generateCorrelationId();

        StringBuilder data = new StringBuilder();

        // 1. Presupuesto root attributes
        data.append(presupuesto.getId().getValue());
        data.append(presupuesto.getNombre());
        data.append(presupuesto.getProyectoId());
        data.append(presupuesto.getEstado());
        data.append(presupuesto.getEsContractual());

        // 2. Partidas Merkle root
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuesto.getId().getValue());
        String partidasMerkleRoot = calculatePartidasMerkleRoot(partidas);
        data.append(partidasMerkleRoot);

        // 3. Metadata
        data.append(VERSION);

        String hash = calculateSHA256(data.toString());

        long duration = System.currentTimeMillis() - startTime;

        // Unificamos la llamada de observabilidad a través del puerto
        observabilityPort.recordHashEvent(correlationId, "approval_hash", presupuesto.getId().getValue(), hash,
                duration, partidas.size(), ALGORITHM_VERSION);

        return hash;
    }

    @Override
    public String calculateExecutionHash(Presupuesto presupuesto) {
        Objects.requireNonNull(presupuesto, "El presupuesto no puede ser nulo");

        if (presupuesto.getIntegrityHashApproval() == null) {
            throw new IllegalStateException("Cannot calculate execution hash without approval hash");
        }

        long startTime = System.currentTimeMillis();
        String correlationId = observabilityPort.generateCorrelationId();

        StringBuilder data = new StringBuilder();

        // Chain to approval hash
        data.append(presupuesto.getIntegrityHashApproval());

        // Financial state
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuesto.getId().getValue());
        for (Partida partida : partidas) {
            data.append(partida.getId().getValue());
            data.append(partida.getGastosReales());
            data.append(partida.getCompromisosPendientes());
            data.append(partida.getSaldoDisponible());
        }

        // Timestamp
        data.append(LocalDateTime.now().toString());

        String hash = calculateSHA256(data.toString());

        long duration = System.currentTimeMillis() - startTime;

        // Llamada unificada al puerto
        observabilityPort.recordHashEvent(correlationId, "execution_hash", presupuesto.getId().getValue(), hash,
                duration, partidas.size(), ALGORITHM_VERSION);

        return hash;
    }

    // --- Métodos Privados (Lógica de Merkle Tree - Sin Cambios) ---

    private String calculatePartidasMerkleRoot(List<Partida> partidas) {
        if (partidas == null || partidas.isEmpty()) {
            return calculateSHA256("");
        }

        List<String> partidaHashes = partidas.stream().map(this::calculatePartidaHash).sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        return calculateMerkleRoot(partidaHashes);
    }

    private String calculatePartidaHash(Partida partida) {
        StringBuilder data = new StringBuilder();

        data.append(partida.getId().getValue());
        data.append(partida.getItem());
        data.append(partida.getDescripcion());
        data.append(partida.getUnidad());
        data.append(partida.getMetrado());
        data.append(partida.getPresupuestoAsignado());
        data.append(partida.getPadreId());
        data.append(partida.getNivel());

        Optional<APUSnapshot> apuOpt = apuSnapshotRepository.findByPartidaId(partida.getId().getValue());
        if (apuOpt.isPresent()) {
            APUSnapshot apu = apuOpt.get();
            data.append(apu.getExternalApuId());
            data.append(apu.getCatalogSource());
            data.append(apu.getRendimientoVigente());
            data.append(apu.getUnidadSnapshot());

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

    private String calculateMerkleRoot(List<String> hashes) {
        if (hashes.isEmpty()) {
            return calculateSHA256("");
        }
        if (hashes.size() == 1) {
            return hashes.get(0);
        }

        List<String> nextLevel = new ArrayList<>();
        for (int i = 0; i < hashes.size(); i += 2) {
            String left = hashes.get(i);
            String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
            nextLevel.add(calculateSHA256(left + right));
        }

        return calculateMerkleRoot(nextLevel);
    }

    private String calculateSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

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