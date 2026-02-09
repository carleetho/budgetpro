package com.budgetpro.domain.catalogo.service;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.shared.port.out.ObservabilityPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de dominio para crear snapshots desde catálogos externos.
 */
public class SnapshotService {

    private final CatalogPort catalogPort;
    private final ObservabilityPort observability;

    public SnapshotService(CatalogPort catalogPort, ObservabilityPort observability) {
        this.catalogPort = catalogPort;
        this.observability = observability;
    }

    public APUSnapshot createAPUSnapshot(String externalApuId, String catalogSource) {
        String correlationId = observability.generateCorrelationId();
        long startTime = System.currentTimeMillis();

        if (externalApuId == null || externalApuId.isBlank()) {
            throw new IllegalArgumentException("El externalApuId no puede estar vacío");
        }
        if (catalogSource == null || catalogSource.isBlank()) {
            throw new IllegalArgumentException("El catalogSource no puede estar vacío");
        }

        try {
            APUSnapshot apuData = catalogPort.fetchAPU(externalApuId, catalogSource);
            UUID partidaId = Objects.requireNonNull(apuData.getPartidaId(),
                    "El partidaId del catálogo no puede ser nulo");

            APUSnapshot snapshot = APUSnapshot.crear(APUSnapshotId.generate(), partidaId, externalApuId, catalogSource,
                    apuData.getRendimientoOriginal(), apuData.getUnidadSnapshot(), LocalDateTime.now());

            for (APUInsumoSnapshot insumo : apuData.getInsumos()) {
                String recursoExternalId = insumo.getRecursoExternalId();
                if (!validateRecursoProxy(recursoExternalId, catalogSource)) {
                    throw new CatalogNotFoundException(recursoExternalId, catalogSource);
                }

                RecursoSnapshot recursoSnapshot = catalogPort.fetchRecurso(recursoExternalId, catalogSource);
                APUInsumoSnapshot insumoSnapshot = APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(),
                        recursoSnapshot.externalId(), recursoSnapshot.nombre(), insumo.getCantidad(),
                        recursoSnapshot.precioReferencial());
                snapshot = snapshot.agregarInsumo(insumoSnapshot);
            }

            long durationMs = System.currentTimeMillis() - startTime;
            int insumosCount = snapshot.getInsumos().size();
            observability.recordMetrics("catalog.snapshot.creation", (double) durationMs, "source", catalogSource);
            observability.logEvent("SNAPSHOT_CREATED", String.format("Snapshot %s created from %s in %dms",
                    snapshot.getId().getValue(), catalogSource, durationMs));

            return snapshot;
        } catch (CatalogNotFoundException | CatalogServiceException e) {
            observability.logError(correlationId, catalogSource, "createAPUSnapshot", externalApuId, e);
            throw e;
        } catch (RuntimeException e) {
            observability.logError(correlationId, catalogSource, "createAPUSnapshot", externalApuId, e);
            throw new CatalogServiceException(catalogSource, "Error al crear snapshot desde catálogo", e);
        }
    }

    public APUSnapshot actualizarRendimiento(APUSnapshot snapshot, BigDecimal nuevoRendimiento, UUID usuarioId) {
        Objects.requireNonNull(snapshot, "El snapshot no puede ser nulo");

        BigDecimal rendimientoAnterior = snapshot.getRendimientoVigente();
        BigDecimal rendimientoOriginal = snapshot.getRendimientoOriginal();

        APUSnapshot updatedSnapshot = snapshot.actualizarRendimiento(nuevoRendimiento, usuarioId);

        // Solo registrar si realmente cambió
        if (rendimientoAnterior.compareTo(nuevoRendimiento) != 0) {
            observability.recordMetrics("catalog.rendimiento.override", 1.0, "source",
                    updatedSnapshot.getCatalogSource());
            observability.logEvent("RENDIMIENTO_MODIFIED",
                    String.format("Rendimiento on snapshot %s changed from %s to %s",
                            updatedSnapshot.getId().getValue(), rendimientoAnterior, nuevoRendimiento));
        }
        return updatedSnapshot;
    }

    public boolean validateRecursoProxy(String externalId, String catalogSource) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("El externalId no puede estar vacío");
        }
        if (catalogSource == null || catalogSource.isBlank()) {
            throw new IllegalArgumentException("El catalogSource no puede estar vacío");
        }
        return catalogPort.isRecursoActive(externalId, catalogSource);
    }
}
