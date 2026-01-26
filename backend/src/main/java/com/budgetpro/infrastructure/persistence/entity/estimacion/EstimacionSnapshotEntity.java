package com.budgetpro.infrastructure.persistence.entity.estimacion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "estimacion_snapshot")
public class EstimacionSnapshotEntity {

    @Id
    @Column(name = "snapshot_id")
    private UUID id;

    @Column(name = "estimacion_id", nullable = false)
    private UUID estimacionId; // Keeping as ID reference to avoid eager loading aggregate if accessed alone,
                               // or standard ManyToOne.
    // Requirement implies standalone snapshot but usually linked. Let's map it as
    // UUID for simplicity or ManyToOne if relation traversal needed.
    // Given the pattern in Cronograma, it used direct ID reference for optimization
    // in some contexts. But standard JPA prefers ManyToOne.
    // CronogramaSnapshotEntity used columns for IDs. I will follow that usage.

    @Column(name = "items_snapshot", columnDefinition = "jsonb", nullable = false)
    private String itemsSnapshot;

    @Column(name = "totales_snapshot", columnDefinition = "jsonb", nullable = false)
    private String totalesSnapshot;

    @Column(name = "metadata_snapshot", columnDefinition = "jsonb", nullable = false)
    private String metadataSnapshot;

    @CreationTimestamp
    @Column(name = "snapshot_date", nullable = false, updatable = false)
    private LocalDateTime snapshotDate;

    @Column(name = "snapshot_algorithm", nullable = false)
    private String snapshotAlgorithm;

    public EstimacionSnapshotEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEstimacionId() {
        return estimacionId;
    }

    public void setEstimacionId(UUID estimacionId) {
        this.estimacionId = estimacionId;
    }

    public String getItemsSnapshot() {
        return itemsSnapshot;
    }

    public void setItemsSnapshot(String itemsSnapshot) {
        this.itemsSnapshot = itemsSnapshot;
    }

    public String getTotalesSnapshot() {
        return totalesSnapshot;
    }

    public void setTotalesSnapshot(String totalesSnapshot) {
        this.totalesSnapshot = totalesSnapshot;
    }

    public String getMetadataSnapshot() {
        return metadataSnapshot;
    }

    public void setMetadataSnapshot(String metadataSnapshot) {
        this.metadataSnapshot = metadataSnapshot;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public String getSnapshotAlgorithm() {
        return snapshotAlgorithm;
    }

    public void setSnapshotAlgorithm(String snapshotAlgorithm) {
        this.snapshotAlgorithm = snapshotAlgorithm;
    }
}
