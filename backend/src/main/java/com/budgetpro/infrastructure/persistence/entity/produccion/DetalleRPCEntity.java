package com.budgetpro.infrastructure.persistence.entity.produccion;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Detalle de reporte de producción (RPC) por partida.
 */
@Entity
@Table(name = "detalle_rpc",
       indexes = {
           @Index(name = "idx_detalle_rpc_reporte", columnList = "reporte_id"),
           @Index(name = "idx_detalle_rpc_partida", columnList = "partida_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
// REGLA-059
public class DetalleRPCEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporte_id", nullable = false)
    private ReporteProduccionEntity reporteProduccion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private PartidaEntity partida;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    // REGLA-077
    @Column(name = "cantidad_reportada", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidadReportada;
}
