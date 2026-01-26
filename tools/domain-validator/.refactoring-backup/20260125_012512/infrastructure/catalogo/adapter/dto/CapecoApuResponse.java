package com.budgetpro.infrastructure.catalogo.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CapecoApuResponse {

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("unidad")
    private String unidad;

    @JsonProperty("rendimiento")
    private BigDecimal rendimiento;

    @JsonProperty("insumos")
    private List<CapecoApuInsumoResponse> insumos;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(BigDecimal rendimiento) {
        this.rendimiento = rendimiento;
    }

    public List<CapecoApuInsumoResponse> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<CapecoApuInsumoResponse> insumos) {
        this.insumos = insumos;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CapecoApuInsumoResponse {
        @JsonProperty("recurso_external_id")
        private String recursoExternalId;

        @JsonProperty("recurso_nombre")
        private String recursoNombre;

        @JsonProperty("cantidad")
        private BigDecimal cantidad;

        @JsonProperty("precio_unitario")
        private BigDecimal precioUnitario;

        public String getRecursoExternalId() {
            return recursoExternalId;
        }

        public void setRecursoExternalId(String recursoExternalId) {
            this.recursoExternalId = recursoExternalId;
        }

        public String getRecursoNombre() {
            return recursoNombre;
        }

        public void setRecursoNombre(String recursoNombre) {
            this.recursoNombre = recursoNombre;
        }

        public BigDecimal getCantidad() {
            return cantidad;
        }

        public void setCantidad(BigDecimal cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }
    }
}
