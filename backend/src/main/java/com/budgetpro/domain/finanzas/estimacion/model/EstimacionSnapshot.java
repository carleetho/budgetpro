package com.budgetpro.domain.finanzas.estimacion.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot inmutable de una estimación aprobada. Almacena el estado completo en
 * formato JSON para auditoría y referencias históricas.
 */
public final class EstimacionSnapshot {

    private final EstimacionSnapshotId id;
    private final EstimacionId estimacionId;

    // JSONB Data
    private final String itemsSnapshot;
    private final String totalesSnapshot;
    private final String metadataSnapshot;

    private final LocalDateTime snapshotDate;
    private final String snapshotAlgorithm;

    private EstimacionSnapshot(EstimacionSnapshotId id, EstimacionId estimacionId, String itemsSnapshot,
            String totalesSnapshot, String metadataSnapshot, LocalDateTime snapshotDate, String snapshotAlgorithm) {
        this.id = Objects.requireNonNull(id, "El ID del snapshot no puede ser nulo");
        this.estimacionId = Objects.requireNonNull(estimacionId, "El ID de la estimación no puede ser nulo");
        this.itemsSnapshot = Objects.requireNonNull(itemsSnapshot, "El snapshot de items no puede ser nulo");
        this.totalesSnapshot = Objects.requireNonNull(totalesSnapshot, "El snapshot de totales no puede ser nulo");
        this.metadataSnapshot = Objects.requireNonNull(metadataSnapshot, "El snapshot de metadata no puede ser nulo");
        this.snapshotDate = Objects.requireNonNull(snapshotDate, "La fecha del snapshot no puede ser nula");
        this.snapshotAlgorithm = Objects.requireNonNull(snapshotAlgorithm,
                "El algoritmo del snapshot no puede ser nulo");
    }

    public static EstimacionSnapshot crear(EstimacionId estimacionId, String itemsJson, String totalesJson,
            String metadataJson) {
        return new EstimacionSnapshot(EstimacionSnapshotId.random(), estimacionId, itemsJson, totalesJson, metadataJson,
                LocalDateTime.now(), "ESTIMACION-SNAPSHOT-v1");
    }

    public static EstimacionSnapshot reconstruir(EstimacionSnapshotId id, EstimacionId estimacionId,
            String itemsSnapshot, String totalesSnapshot, String metadataSnapshot, LocalDateTime snapshotDate,
            String snapshotAlgorithm) {
        return new EstimacionSnapshot(id, estimacionId, itemsSnapshot, totalesSnapshot, metadataSnapshot, snapshotDate,
                snapshotAlgorithm);
    }

    public EstimacionSnapshotId getId() {
        return id;
    }

    public EstimacionId getEstimacionId() {
        return estimacionId;
    }

    public String getItemsSnapshot() {
        return itemsSnapshot;
    }

    public String getTotalesSnapshot() {
        return totalesSnapshot;
    }

    public String getMetadataSnapshot() {
        return metadataSnapshot;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public String getSnapshotAlgorithm() {
        return snapshotAlgorithm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EstimacionSnapshot that = (EstimacionSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
