package com.budgetpro.infrastructure.persistence.entity.almacen;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla movimiento_almacen.
 */
@Entity
@Table(name = "movimiento_almacen",
       indexes = {
           @Index(name = "idx_movimiento_almacen", columnList = "almacen_id"),
           @Index(name = "idx_movimiento_recurso", columnList = "recurso_id"),
           @Index(name = "idx_movimiento_tipo", columnList = "tipo_movimiento"),
           @Index(name = "idx_movimiento_fecha", columnList = "fecha_movimiento"),
           @Index(name = "idx_movimiento_partida", columnList = "partida_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MovimientoAlmacenEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "almacen_id", nullable = false, updatable = false)
    private UUID almacenId;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private UUID recursoId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipo;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "cantidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidad;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "costo_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "importe_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal importeTotal;

    @Column(name = "numero_documento", length = 100)
    private String numeroDocumento;

    @Column(name = "partida_id")
    private UUID partidaId;

    @Column(name = "centro_costo_id")
    private UUID centroCostoId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    private void prePersist() {
        if (tipo == null) {
            tipo = tipoMovimiento;
        }
        if (fecha == null && fechaMovimiento != null) {
            fecha = fechaMovimiento.atStartOfDay();
        }
        if (costoUnitario == null) {
            costoUnitario = precioUnitario;
        }
    }

    @AssertTrue(message = "partidaId es obligatorio cuando el tipo es SALIDA")
    private boolean isPartidaValidaParaSalida() {
        if (tipo == com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen.SALIDA) {
            return partidaId != null;
        }
        return true;
    }

    /**
     * Constructor de compatibilidad para mapeos existentes.
     */
    public MovimientoAlmacenEntity(UUID id, UUID almacenId, UUID recursoId,
                                   com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento,
                                   LocalDate fechaMovimiento, BigDecimal cantidad, BigDecimal precioUnitario,
                                   BigDecimal importeTotal, String numeroDocumento, UUID partidaId,
                                   UUID centroCostoId, String observaciones, Integer version) {
        this.id = id;
        this.almacenId = almacenId;
        this.recursoId = recursoId;
        this.tipoMovimiento = tipoMovimiento;
        this.tipo = tipoMovimiento;
        this.fechaMovimiento = fechaMovimiento;
        this.fecha = fechaMovimiento != null ? fechaMovimiento.atStartOfDay() : null;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitario = precioUnitario;
        this.importeTotal = importeTotal;
        this.numeroDocumento = numeroDocumento;
        this.partidaId = partidaId;
        this.centroCostoId = centroCostoId;
        this.observaciones = observaciones;
        this.version = version;
    }
}
