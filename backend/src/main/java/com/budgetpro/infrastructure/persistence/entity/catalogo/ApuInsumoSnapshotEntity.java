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
 * Entidad JPA para la tabla apu_insumo_snapshot.
 */
@Entity
@Table(name = "apu_insumo_snapshot",
       indexes = {
           @Index(name = "idx_apu_insumo_snapshot_apu", columnList = "apu_snapshot_id"),
           @Index(name = "idx_apu_insumo_snapshot_recurso", columnList = "recurso_external_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApuInsumoSnapshotEntity extends AuditEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apu_snapshot_id", nullable = false, updatable = false)
    private ApuSnapshotEntity apuSnapshot;

    @NotBlank
    @Column(name = "recurso_external_id", nullable = false, length = 255)
    private String recursoExternalId;

    @NotBlank
    @Column(name = "recurso_nombre", nullable = false, length = 500)
    private String recursoNombre;

    @NotNull
    @Digits(integer = 15, fraction = 6)
    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;
}
