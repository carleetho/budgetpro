package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.rrhh.model.EmpleadoId;

public class ResumenAsistenciaResponse {
    private final EmpleadoId empleadoId;
    private final int mes;
    private final int ano;
    private final int totalDiasTrabajados;
    private final double totalHorasTrabajadas;
    private final double totalHorasExtras;
    private final int totalAusencias;

    public ResumenAsistenciaResponse(EmpleadoId empleadoId, int mes, int ano, int totalDiasTrabajados,
            double totalHorasTrabajadas, double totalHorasExtras, int totalAusencias) {
        this.empleadoId = empleadoId;
        this.mes = mes;
        this.ano = ano;
        this.totalDiasTrabajados = totalDiasTrabajados;
        this.totalHorasTrabajadas = totalHorasTrabajadas;
        this.totalHorasExtras = totalHorasExtras;
        this.totalAusencias = totalAusencias;
    }

    public EmpleadoId getEmpleadoId() {
        return empleadoId;
    }

    public int getMes() {
        return mes;
    }

    public int getAno() {
        return ano;
    }

    public int getTotalDiasTrabajados() {
        return totalDiasTrabajados;
    }

    public double getTotalHorasTrabajadas() {
        return totalHorasTrabajadas;
    }

    public double getTotalHorasExtras() {
        return totalHorasExtras;
    }

    public int getTotalAusencias() {
        return totalAusencias;
    }
}
