package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import java.time.LocalDateTime;

public class EstimacionResponse {

        private UUID id;
        private UUID presupuestoId;
        private Long numeroEstimacion;
        private String estado;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private LocalDateTime fechaCorte;
        private BigDecimal retencionPorcentaje;
        private BigDecimal montoRetencion;
        private BigDecimal subtotal;
        private BigDecimal totalPagar;
        private BigDecimal amortizacionAnticipo;
        private BigDecimal retencionFondoGarantia;
        private String evidenciaUrl;
        private List<DetalleEstimacionResponse> detalles;
        private int version;

        public EstimacionResponse() {
        }

        // Constructor matching usage in GenerarEstimacionUseCaseImpl
        public EstimacionResponse(UUID id, UUID presupuestoId, Integer numeroEstimacion, LocalDateTime fechaCorte,
                        LocalDate fechaInicio, LocalDate fechaFin, BigDecimal subtotal, BigDecimal amortizacionAnticipo,
                        BigDecimal retencionFondoGarantia, BigDecimal totalPagar, String evidenciaUrl,
                        EstadoEstimacion estado, List<DetalleEstimacionResponse> detalles, int version) {
                this.id = id;
                this.presupuestoId = presupuestoId;
                this.numeroEstimacion = numeroEstimacion != null ? numeroEstimacion.longValue() : null;
                this.fechaCorte = fechaCorte;
                this.fechaInicio = fechaInicio;
                this.fechaFin = fechaFin;
                this.subtotal = subtotal;
                this.amortizacionAnticipo = amortizacionAnticipo;
                this.retencionFondoGarantia = retencionFondoGarantia;
                this.totalPagar = totalPagar;
                this.evidenciaUrl = evidenciaUrl;
                this.estado = estado != null ? estado.name() : null;
                this.detalles = detalles;
                this.version = version;
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

        public Long getNumeroEstimacion() {
                return numeroEstimacion;
        }

        public void setNumeroEstimacion(Long numeroEstimacion) {
                this.numeroEstimacion = numeroEstimacion;
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

        public LocalDateTime getFechaCorte() {
                return fechaCorte;
        }

        public void setFechaCorte(LocalDateTime fechaCorte) {
                this.fechaCorte = fechaCorte;
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

        public BigDecimal getAmortizacionAnticipo() {
                return amortizacionAnticipo;
        }

        public void setAmortizacionAnticipo(BigDecimal amortizacionAnticipo) {
                this.amortizacionAnticipo = amortizacionAnticipo;
        }

        public BigDecimal getRetencionFondoGarantia() {
                return retencionFondoGarantia;
        }

        public void setRetencionFondoGarantia(BigDecimal retencionFondoGarantia) {
                this.retencionFondoGarantia = retencionFondoGarantia;
        }

        public BigDecimal getTotalPagar() {
                return totalPagar;
        }

        public void setTotalPagar(BigDecimal totalPagar) {
                this.totalPagar = totalPagar;
        }

        public String getEvidenciaUrl() {
                return evidenciaUrl;
        }

        public void setEvidenciaUrl(String evidenciaUrl) {
                this.evidenciaUrl = evidenciaUrl;
        }

        public List<DetalleEstimacionResponse> getDetalles() {
                return detalles;
        }

        public void setDetalles(List<DetalleEstimacionResponse> detalles) {
                this.detalles = detalles;
        }

        // Alias for getters/setters if consumers use 'items'
        public List<DetalleEstimacionResponse> getItems() {
                return detalles;
        }

        public void setItems(List<DetalleEstimacionResponse> items) {
                this.detalles = items;
        }

        public int getVersion() {
                return version;
        }

        public void setVersion(int version) {
                this.version = version;
        }
}
