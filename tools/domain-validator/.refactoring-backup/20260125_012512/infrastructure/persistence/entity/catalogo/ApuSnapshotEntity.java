package com.budgetpro.infrastructure.persistence.entity.catalogo;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla apu_snapshot.
 */
@Entity
@Table(name = "apu_snapshot",
       indexes = {
           @Index(name = "idx_apu_snapshot_partida", columnList = "partida_id"),
           @Index(name = "idx_apu_snapshot_date", columnList = "snapshot_date"),
           @Index(name = "idx_apu_snapshot_modificado", columnList = "rendimiento_modificado")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApuSnapshotEntity extends AuditEntity {

    @jakarta.persistence.Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false, updatable = false)
    private PartidaEntity partida;

    @NotBlank
    @Column(name = "external_apu_id", nullable = false, length = 255)
    private String externalApuId;

    @NotBlank
    @Column(name = "catalog_source", nullable = false, length = 50)
    private String catalogSource;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "rendimiento_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal rendimientoOriginal;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "rendimiento_vigente", nullable = false, precision = 19, scale = 4)
    private BigDecimal rendimientoVigente;

    @Column(name = "rendimiento_modificado", nullable = false)
    private Boolean rendimientoModificado = Boolean.FALSE;

    @Column(name = "rendimiento_modificado_por")
    private UUID rendimientoModificadoPor;

    @Column(name = "rendimiento_modificado_en")
    private LocalDateTime rendimientoModificadoEn;

    @NotBlank
    @Column(name = "unidad_snapshot", nullable = false, length = 50)
    private String unidadSnapshot;

    @NotNull
    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "apuSnapshot", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApuInsumoSnapshotEntity> insumos = new ArrayList<>();
}
