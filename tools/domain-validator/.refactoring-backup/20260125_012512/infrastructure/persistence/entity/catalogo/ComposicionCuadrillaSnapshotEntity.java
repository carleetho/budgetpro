package com.budgetpro.infrastructure.persistence.entity.catalogo;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA para la tabla composicion_cuadrilla_snapshot.
 */
@Entity
@Table(name = "composicion_cuadrilla_snapshot",
       indexes = {
           @Index(name = "idx_composicion_cuadrilla_insumo", columnList = "apu_insumo_snapshot_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ComposicionCuadrillaSnapshotEntity extends AuditEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apu_insumo_snapshot_id", nullable = false, updatable = false)
    private ApuInsumoSnapshotEntity apuInsumoSnapshot;

    @NotBlank
    @Column(name = "personal_external_id", nullable = false, length = 255)
    private String personalExternalId;

    @NotBlank
    @Column(name = "personal_nombre", nullable = false, length = 500)
    private String personalNombre;

    @NotNull
    @Digits(integer = 15, fraction = 6)
    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "costo_dia", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoDia;

    @NotBlank
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;
}
