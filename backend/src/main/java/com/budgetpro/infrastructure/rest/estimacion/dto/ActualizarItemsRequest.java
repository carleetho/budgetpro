package com.budgetpro.infrastructure.rest.estimacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ActualizarItemsRequest {

    @Valid
    @NotNull
    private List<ItemUpdate> items;

    public List<ItemUpdate> getItems() {
        return items;
    }

    public void setItems(List<ItemUpdate> items) {
        this.items = items;
    }

    public static class ItemUpdate {
        @NotNull
        private UUID partidaId;
        private String concepto;
        @NotNull
        private BigDecimal montoContractual;
        @NotNull
        private BigDecimal porcentajeAvancePeriodo;

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

        public BigDecimal getPorcentajeAvancePeriodo() {
            return porcentajeAvancePeriodo;
        }

        public void setPorcentajeAvancePeriodo(BigDecimal porcentajeAvancePeriodo) {
            this.porcentajeAvancePeriodo = porcentajeAvancePeriodo;
        }
    }
}
