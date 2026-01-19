package com.budgetpro.domain.catalogo.service;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.infrastructure.catalogo.observability.CatalogEventLogger;
import com.budgetpro.infrastructure.catalogo.observability.CatalogMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de dominio para crear snapshots desde catálogos externos.
 */
@Service
@Transactional
public class SnapshotService {

    private final CatalogPort catalogPort;
    private final CatalogMetrics catalogMetrics;
    private final CatalogEventLogger catalogEventLogger;

    public SnapshotService(CatalogPort catalogPort,
                          CatalogMetrics catalogMetrics,
                          CatalogEventLogger catalogEventLogger) {
        this.catalogPort = catalogPort;
        this.catalogMetrics = catalogMetrics;
        this.catalogEventLogger = catalogEventLogger;
    }

    public APUSnapshot createAPUSnapshot(String externalApuId, String catalogSource) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        long startTime = System.currentTimeMillis();

        if (externalApuId == null || externalApuId.isBlank()) {
            throw new IllegalArgumentException("El externalApuId no puede estar vacío");
        }
        if (catalogSource == null || catalogSource.isBlank()) {
            throw new IllegalArgumentException("El catalogSource no puede estar vacío");
        }

        try {
            APUSnapshot apuData = catalogPort.fetchAPU(externalApuId, catalogSource);
            UUID partidaId = Objects.requireNonNull(apuData.getPartidaId(), "El partidaId del catálogo no puede ser nulo");

            APUSnapshot snapshot = APUSnapshot.crear(
                    APUSnapshotId.generate(),
                    partidaId,
                    externalApuId,
                    catalogSource,
                    apuData.getRendimientoOriginal(),
                    apuData.getUnidadSnapshot(),
                    LocalDateTime.now()
            );

            for (APUInsumoSnapshot insumo : apuData.getInsumos()) {
                String recursoExternalId = insumo.getRecursoExternalId();
                if (!validateRecursoProxy(recursoExternalId, catalogSource)) {
                    throw new CatalogNotFoundException(recursoExternalId, catalogSource);
                }

                RecursoSnapshot recursoSnapshot = catalogPort.fetchRecurso(recursoExternalId, catalogSource);
                APUInsumoSnapshot insumoSnapshot = APUInsumoSnapshot.crear(
                        APUInsumoSnapshotId.generate(),
                        recursoSnapshot.externalId(),
                        recursoSnapshot.nombre(),
                        insumo.getCantidad(),
                        recursoSnapshot.precioReferencial()
                );
                snapshot.agregarInsumo(insumoSnapshot);
            }

            long durationMs = System.currentTimeMillis() - startTime;
            int insumosCount = snapshot.getInsumos().size();
            catalogMetrics.recordSnapshotCreation(durationMs, catalogSource, insumosCount);
            catalogEventLogger.logSnapshotCreation(
                    correlationId,
                    snapshot.getId().getValue(),
                    externalApuId,
                    catalogSource,
                    durationMs,
                    insumosCount
            );

            return snapshot;
        } catch (CatalogNotFoundException | CatalogServiceException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            catalogEventLogger.logCatalogError(correlationId, catalogSource, "createAPUSnapshot", externalApuId, e);
            throw e;
        } catch (RuntimeException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            catalogEventLogger.logCatalogError(correlationId, catalogSource, "createAPUSnapshot", externalApuId, e);
            throw new CatalogServiceException(catalogSource, "Error al crear snapshot desde catálogo", e);
        }
    }

    public void actualizarRendimiento(APUSnapshot snapshot, BigDecimal nuevoRendimiento, UUID usuarioId) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        Objects.requireNonNull(snapshot, "El snapshot no puede ser nulo");
        
        BigDecimal rendimientoAnterior = snapshot.getRendimientoVigente();
        BigDecimal rendimientoOriginal = snapshot.getRendimientoOriginal();
        
        snapshot.actualizarRendimiento(nuevoRendimiento, usuarioId);
        
        // Solo registrar si realmente cambió
        if (rendimientoAnterior.compareTo(nuevoRendimiento) != 0) {
            catalogMetrics.recordRendimientoOverride(snapshot.getCatalogSource());
            catalogEventLogger.logRendimientoModification(
                    correlationId,
                    snapshot.getId().getValue(),
                    rendimientoOriginal,
                    rendimientoAnterior,
                    nuevoRendimiento,
                    usuarioId
            );
        }
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
