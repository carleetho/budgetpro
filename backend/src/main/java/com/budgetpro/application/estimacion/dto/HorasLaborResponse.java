package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class HorasLaborResponse {

    private UUID partidaId;
    private BigDecimal horasTotales;
    private List<DetalleLabor> detalles;

    public HorasLaborResponse() {
    }

    public HorasLaborResponse(UUID partidaId, BigDecimal horasTotales, List<DetalleLabor> detalles) {
        this.partidaId = partidaId;
        this.horasTotales = horasTotales;
        this.detalles = detalles;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public BigDecimal getHorasTotales() {
        return horasTotales;
    }

    public void setHorasTotales(BigDecimal horasTotales) {
        this.horasTotales = horasTotales;
    }

    public List<DetalleLabor> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleLabor> detalles) {
        this.detalles = detalles;
    }

    public static class DetalleLabor {
        private String personal;
        private BigDecimal horas;

        public DetalleLabor() {
        }

        public DetalleLabor(String personal, BigDecimal horas) {
            this.personal = personal;
            this.horas = horas;
        }

        public String getPersonal() {
            return personal;
        }

        public void setPersonal(String personal) {
            this.personal = personal;
        }

        public BigDecimal getHoras() {
            return horas;
        }

        public void setHoras(BigDecimal horas) {
            this.horas = horas;
        }
    }
}
