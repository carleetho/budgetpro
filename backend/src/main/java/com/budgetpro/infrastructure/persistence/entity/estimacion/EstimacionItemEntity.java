package com.budgetpro.infrastructure.persistence.entity.estimacion;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "estimacion_item")
public class EstimacionItemEntity {

    @Id
    @Column(name = "item_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimacion_id", nullable = false)
    private EstimacionEntity estimacion;

    @Column(name = "partida_id", nullable = false)
    private UUID partidaId;

    @Column(name = "concepto", nullable = false)
    private String concepto;

    @Column(name = "monto_contractual", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoContractual;

    @Column(name = "porcentaje_anterior", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAnterior;

    @Column(name = "monto_anterior", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoAnterior;

    @Column(name = "porcentaje_actual", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeActual;

    @Column(name = "monto_actual", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoActual;

    // Required by JPA
    public EstimacionItemEntity() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EstimacionEntity getEstimacion() {
        return estimacion;
    }

    public void setEstimacion(EstimacionEntity estimacion) {
        this.estimacion = estimacion;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getMontoContractual() {
        return montoContractual;
    }

    public void setMontoContractual(BigDecimal montoContractual) {
        this.montoContractual = montoContractual;
    }

    public BigDecimal getPorcentajeAnterior() {
        return porcentajeAnterior;
    }

    public void setPorcentajeAnterior(BigDecimal porcentajeAnterior) {
        this.porcentajeAnterior = porcentajeAnterior;
    }

    public BigDecimal getMontoAnterior() {
        return montoAnterior;
    }

    public void setMontoAnterior(BigDecimal montoAnterior) {
        this.montoAnterior = montoAnterior;
    }

    public BigDecimal getPorcentajeActual() {
        return porcentajeActual;
    }

    public void setPorcentajeActual(BigDecimal porcentajeActual) {
        this.porcentajeActual = porcentajeActual;
    }

    public BigDecimal getMontoActual() {
        return montoActual;
    }

    public void setMontoActual(BigDecimal montoActual) {
        this.montoActual = montoActual;
    }
}
