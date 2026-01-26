package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class EstimacionItemResponse {

    private UUID id;
    private UUID partidaId;
    private String concepto;
    private BigDecimal montoContractual;
    private BigDecimal porcentajeAnterior;
    private BigDecimal montoAnterior;
    private BigDecimal porcentajeActual;
    private BigDecimal montoActual;
    private BigDecimal porcentajeAcumulado;
    private BigDecimal montoAcumulado;
    private BigDecimal saldoPorEjercer;

    public EstimacionItemResponse() {
    }

    // Getters and Setters

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

    public BigDecimal getPorcentajeAcumulado() {
        return porcentajeAcumulado;
    }

    public void setPorcentajeAcumulado(BigDecimal porcentajeAcumulado) {
        this.porcentajeAcumulado = porcentajeAcumulado;
    }

    public BigDecimal getMontoAcumulado() {
        return montoAcumulado;
    }

    public void setMontoAcumulado(BigDecimal montoAcumulado) {
        this.montoAcumulado = montoAcumulado;
    }

    public BigDecimal getSaldoPorEjercer() {
        return saldoPorEjercer;
    }

    public void setSaldoPorEjercer(BigDecimal saldoPorEjercer) {
        this.saldoPorEjercer = saldoPorEjercer;
    }
}
