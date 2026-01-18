package com.budgetpro.infrastructure.persistence.entity.anticipo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "anticipo_movimiento",
       indexes = {
           @Index(name = "idx_anticipo_movimiento_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_anticipo_movimiento_tipo", columnList = "tipo")
       })
public class AnticipoMovimientoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private com.budgetpro.domain.finanzas.anticipo.model.TipoMovimientoAnticipo tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "referencia", nullable = false, length = 500)
    private String referencia;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AnticipoMovimientoEntity() {
    }

    public AnticipoMovimientoEntity(UUID id, UUID proyectoId, BigDecimal monto,
                                    com.budgetpro.domain.finanzas.anticipo.model.TipoMovimientoAnticipo tipo,
                                    LocalDateTime fecha, String referencia) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
        this.referencia = referencia;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public com.budgetpro.domain.finanzas.anticipo.model.TipoMovimientoAnticipo getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }
}
