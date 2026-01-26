package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ActualizarItemsCommand {

    private List<ItemUpdate> items;

    public ActualizarItemsCommand() {
    }

    public ActualizarItemsCommand(List<ItemUpdate> items) {
        this.items = items;
    }

    public List<ItemUpdate> getItems() {
        return items;
    }

    public void setItems(List<ItemUpdate> items) {
        this.items = items;
    }

    public static class ItemUpdate {
        private UUID partidaId;
        private String concepto;
        private BigDecimal montoContractual;
        private BigDecimal porcentajeAvancePeriodo;

        public ItemUpdate() {
        }

        public ItemUpdate(UUID partidaId, String concepto, BigDecimal montoContractual,
                BigDecimal porcentajeAvancePeriodo) {
            this.partidaId = partidaId;
            this.concepto = concepto;
            this.montoContractual = montoContractual;
            this.porcentajeAvancePeriodo = porcentajeAvancePeriodo;
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

        public BigDecimal getPorcentajeAvancePeriodo() {
            return porcentajeAvancePeriodo;
        }

        public void setPorcentajeAvancePeriodo(BigDecimal porcentajeAvancePeriodo) {
            this.porcentajeAvancePeriodo = porcentajeAvancePeriodo;
        }
    }
}
