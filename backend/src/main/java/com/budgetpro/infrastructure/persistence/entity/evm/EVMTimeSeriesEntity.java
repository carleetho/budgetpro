package com.budgetpro.infrastructure.persistence.entity.evm;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad JPA para persistencia de la serie temporal EVM.
 *
 * Regla de inmutabilidad: todas las columnas de negocio son updatable=false.
 */
@Entity
@Table(name = "evm_time_series", indexes = {
        @Index(name = "idx_evm_time_series_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_evm_time_series_proyecto_fecha", columnList = "proyecto_id, fecha_corte"),
        @Index(name = "idx_evm_time_series_fecha_corte", columnList = "fecha_corte")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_evm_time_series_proyecto_fecha", columnNames = { "proyecto_id", "fecha_corte" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EVMTimeSeriesEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "proyecto_id", nullable = false, updatable = false, insertable = false)
    private ProyectoEntity proyecto;

    @NotNull
    @Column(name = "fecha_corte", nullable = false, updatable = false)
    private LocalDate fechaCorte;

    @NotNull
    @Column(name = "periodo", nullable = false, updatable = false)
    private Integer periodo;

    @NotNull
    @Column(name = "moneda", nullable = false, length = 3, updatable = false)
    private String moneda;

    @NotNull
    @Column(name = "pv", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal pvAcumulado;

    @NotNull
    @Column(name = "ev", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal evAcumulado;

    @NotNull
    @Column(name = "ac", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal acAcumulado;

    @NotNull
    @Column(name = "bac", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal bacTotal;

    @NotNull
    @Column(name = "bac_ajustado", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal bacAjustado;

    @NotNull
    @Column(name = "cpi", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal cpiPeriodo;

    @NotNull
    @Column(name = "spi", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal spiPeriodo;
}

