package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class EstimacionResponse {

        private UUID id;
        private UUID presupuestoId;
        private String estado;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private BigDecimal retencionPorcentaje;
        private BigDecimal montoRetencion;
        private BigDecimal subtotal;
        private BigDecimal totalPagar;
        private List<EstimacionItemResponse> items;

        public EstimacionResponse() {
        }

        // Getters and Setters

        public UUID getId() {
                return id;
        }

        public void setId(UUID id) {
                this.id = id;
        }

        public UUID getPresupuestoId() {
                return presupuestoId;
        }

        public void setPresupuestoId(UUID presupuestoId) {
                this.presupuestoId = presupuestoId;
        }

        public String getEstado() {
                return estado;
        }

        public void setEstado(String estado) {
                this.estado = estado;
        }

        public LocalDate getFechaInicio() {
                return fechaInicio;
        }

        public void setFechaInicio(LocalDate fechaInicio) {
                this.fechaInicio = fechaInicio;
        }

        public LocalDate getFechaFin() {
                return fechaFin;
        }

        public void setFechaFin(LocalDate fechaFin) {
                this.fechaFin = fechaFin;
        }

        public BigDecimal getRetencionPorcentaje() {
                return retencionPorcentaje;
        }

        public void setRetencionPorcentaje(BigDecimal retencionPorcentaje) {
                this.retencionPorcentaje = retencionPorcentaje;
        }

        public BigDecimal getMontoRetencion() {
                return montoRetencion;
        }

        public void setMontoRetencion(BigDecimal montoRetencion) {
                this.montoRetencion = montoRetencion;
        }

        public BigDecimal getSubtotal() {
                return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
                this.subtotal = subtotal;
        }

        public BigDecimal getTotalPagar() {
                return totalPagar;
        }

        public void setTotalPagar(BigDecimal totalPagar) {
                this.totalPagar = totalPagar;
        }

        public List<EstimacionItemResponse> getItems() {
                return items;
        }

        public void setItems(List<EstimacionItemResponse> items) {
                this.items = items;
        }
}
