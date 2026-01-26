package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ConsumoMaterialResponse {

    private UUID partidaId;
    private BigDecimal cantidadTotal;
    private String unidad;
    private List<DetalleMaterial> detalles;

    public ConsumoMaterialResponse() {
    }

    public ConsumoMaterialResponse(UUID partidaId, BigDecimal cantidadTotal, String unidad,
            List<DetalleMaterial> detalles) {
        this.partidaId = partidaId;
        this.cantidadTotal = cantidadTotal;
        this.unidad = unidad;
        this.detalles = detalles;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public BigDecimal getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(BigDecimal cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public List<DetalleMaterial> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleMaterial> detalles) {
        this.detalles = detalles;
    }

    public static class DetalleMaterial {
        private String material;
        private BigDecimal cantidad;

        public DetalleMaterial() {
        }

        public DetalleMaterial(String material, BigDecimal cantidad) {
            this.material = material;
            this.cantidad = cantidad;
        }

        public String getMaterial() {
            return material;
        }

        public void setMaterial(String material) {
            this.material = material;
        }

        public BigDecimal getCantidad() {
            return cantidad;
        }

        public void setCantidad(BigDecimal cantidad) {
            this.cantidad = cantidad;
        }
    }
}
