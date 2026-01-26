package com.budgetpro.infrastructure.persistence.entity.estimacion;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "avance_partida")
public class AvancePartidaEntity {

    @Id
    @Column(name = "avance_id")
    private UUID id;

    @Column(name = "partida_id", nullable = false)
    private UUID partidaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimacion_id", nullable = false)
    private EstimacionEntity estimacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "porcentaje_avance", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAvance;

    @Column(name = "monto_acumulado", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoAcumulado;

    public AvancePartidaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public EstimacionEntity getEstimacion() {
        return estimacion;
    }

    public void setEstimacion(EstimacionEntity estimacion) {
        this.estimacion = estimacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public BigDecimal getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(BigDecimal porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public BigDecimal getMontoAcumulado() {
        return montoAcumulado;
    }

    public void setMontoAcumulado(BigDecimal montoAcumulado) {
        this.montoAcumulado = montoAcumulado;
    }
}
