package com.budgetpro.infrastructure.persistence.entity.evm;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para persistencia de snapshots EVM.
 */
@Entity
@Table(name = "evm_snapshot", indexes = { @Index(name = "idx_evm_snapshot_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_evm_snapshot_fecha_corte", columnList = "fecha_corte") }, uniqueConstraints = {
                @UniqueConstraint(name = "uk_evm_snapshot_proyecto_fecha", columnNames = { "proyecto_id",
                        "fecha_corte" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class EVMSnapshotEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false, updatable = false, insertable = false)
    private ProyectoEntity proyecto;

    @NotNull
    @Column(name = "fecha_corte", nullable = false, updatable = false)
    private LocalDateTime fechaCorte;

    @NotNull
    @Column(name = "fecha_calculo", nullable = false, updatable = false)
    private LocalDateTime fechaCalculo;

    // Métricas Base
    @NotNull
    @Column(name = "pv", nullable = false, precision = 19, scale = 4)
    private BigDecimal pv;

    @NotNull
    @Column(name = "ev", nullable = false, precision = 19, scale = 4)
    private BigDecimal ev;

    @NotNull
    @Column(name = "ac", nullable = false, precision = 19, scale = 4)
    private BigDecimal ac;

    @NotNull
    @Column(name = "bac", nullable = false, precision = 19, scale = 4)
    private BigDecimal bac;

    // Métricas de Variación
    @NotNull
    @Column(name = "cv", nullable = false, precision = 19, scale = 4)
    private BigDecimal cv;

    @NotNull
    @Column(name = "sv", nullable = false, precision = 19, scale = 4)
    private BigDecimal sv;

    // Índices de Desempeño
    @NotNull
    @Column(name = "cpi", nullable = false, precision = 19, scale = 4)
    private BigDecimal cpi;

    @NotNull
    @Column(name = "spi", nullable = false, precision = 19, scale = 4)
    private BigDecimal spi;

    // Proyecciones
    @NotNull
    @Column(name = "eac", nullable = false, precision = 19, scale = 4)
    private BigDecimal eac;

    @NotNull
    @Column(name = "etc", nullable = false, precision = 19, scale = 4)
    private BigDecimal etc;

    @NotNull
    @Column(name = "vac", nullable = false, precision = 19, scale = 4)
    private BigDecimal vac;

    @Column(name = "interpretacion", length = 1000)
    private String interpretacion;
}
