package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.rrhh.model.DetalleNomina;
import java.math.BigDecimal;
import java.util.UUID;

public class DetalleNominaResponse {
    private final UUID empleadoId;
    private final BigDecimal salarioBase;
    private final BigDecimal horasExtras;
    private final BigDecimal bonoAsistencia;
    private final BigDecimal otrosIngresos;
    private final BigDecimal totalPercepciones;
    private final BigDecimal deduccionesFiscales;
    private final BigDecimal deduccionesSeguridadSocial;
    private final BigDecimal otrasDeducciones;
    private final BigDecimal totalDeducciones;
    private final BigDecimal netoAPagar;
    private final BigDecimal costoPatronal;
    private final Double fsrAplicado;
    private final Integer diasTrabajados;

    private DetalleNominaResponse(UUID empleadoId, BigDecimal salarioBase, BigDecimal horasExtras,
            BigDecimal bonoAsistencia, BigDecimal otrosIngresos, BigDecimal totalPercepciones,
            BigDecimal deduccionesFiscales, BigDecimal deduccionesSeguridadSocial, BigDecimal otrasDeducciones,
            BigDecimal totalDeducciones, BigDecimal netoAPagar, BigDecimal costoPatronal, Double fsrAplicado,
            Integer diasTrabajados) {
        this.empleadoId = empleadoId;
        this.salarioBase = salarioBase;
        this.horasExtras = horasExtras;
        this.bonoAsistencia = bonoAsistencia;
        this.otrosIngresos = otrosIngresos;
        this.totalPercepciones = totalPercepciones;
        this.deduccionesFiscales = deduccionesFiscales;
        this.deduccionesSeguridadSocial = deduccionesSeguridadSocial;
        this.otrasDeducciones = otrasDeducciones;
        this.totalDeducciones = totalDeducciones;
        this.netoAPagar = netoAPagar;
        this.costoPatronal = costoPatronal;
        this.fsrAplicado = fsrAplicado;
        this.diasTrabajados = diasTrabajados;
    }

    public static DetalleNominaResponse fromDomain(DetalleNomina detalle) {
        return new DetalleNominaResponse(detalle.getEmpleadoId().getValue(), detalle.getSalarioBase(),
                detalle.getHorasExtras(), detalle.getBonoAsistencia(), detalle.getOtrosIngresos(),
                detalle.getTotalPercepciones(), detalle.getDeduccionesFiscales(),
                detalle.getDeduccionesSeguridadSocial(), detalle.getOtrasDeducciones(), detalle.getTotalDeducciones(),
                detalle.getNetoAPagar(), detalle.getCostoPatronal(), detalle.getFsrAplicado(),
                detalle.getDiasTrabajados());
    }

    public UUID getEmpleadoId() {
        return empleadoId;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public BigDecimal getHorasExtras() {
        return horasExtras;
    }

    public BigDecimal getBonoAsistencia() {
        return bonoAsistencia;
    }

    public BigDecimal getOtrosIngresos() {
        return otrosIngresos;
    }

    public BigDecimal getTotalPercepciones() {
        return totalPercepciones;
    }

    public BigDecimal getDeduccionesFiscales() {
        return deduccionesFiscales;
    }

    public BigDecimal getDeduccionesSeguridadSocial() {
        return deduccionesSeguridadSocial;
    }

    public BigDecimal getOtrasDeducciones() {
        return otrasDeducciones;
    }

    public BigDecimal getTotalDeducciones() {
        return totalDeducciones;
    }

    public BigDecimal getNetoAPagar() {
        return netoAPagar;
    }

    public BigDecimal getCostoPatronal() {
        return costoPatronal;
    }

    public Double getFsrAplicado() {
        return fsrAplicado;
    }

    public Integer getDiasTrabajados() {
        return diasTrabajados;
    }
}
