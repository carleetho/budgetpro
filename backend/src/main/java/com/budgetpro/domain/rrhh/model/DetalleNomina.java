package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.rrhh.model.EmpleadoId;
import java.math.BigDecimal;
import java.util.Objects;

public class DetalleNomina {
    private final EmpleadoId empleadoId;
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

    private DetalleNomina(EmpleadoId empleadoId, BigDecimal salarioBase, BigDecimal horasExtras,
            BigDecimal bonoAsistencia, BigDecimal otrosIngresos, BigDecimal totalPercepciones,
            BigDecimal deduccionesFiscales, BigDecimal deduccionesSeguridadSocial, BigDecimal otrasDeducciones,
            BigDecimal totalDeducciones, BigDecimal netoAPagar, BigDecimal costoPatronal, Double fsrAplicado,
            Integer diasTrabajados) {
        this.empleadoId = Objects.requireNonNull(empleadoId, "empleadoId must not be null");
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

    public static DetalleNomina crear(EmpleadoId empleadoId, BigDecimal salarioBase, BigDecimal horasExtras,
            BigDecimal bonoAsistencia, BigDecimal otrosIngresos, BigDecimal deduccionesFiscales,
            BigDecimal deduccionesSeguridadSocial, BigDecimal otrasDeducciones, BigDecimal costoPatronal,
            Double fsrAplicado, Integer diasTrabajados) {

        BigDecimal totalPercepciones = salarioBase.add(horasExtras).add(bonoAsistencia).add(otrosIngresos);
        BigDecimal totalDeducciones = deduccionesFiscales.add(deduccionesSeguridadSocial).add(otrasDeducciones);
        BigDecimal netoAPagar = totalPercepciones.subtract(totalDeducciones);

        return new DetalleNomina(empleadoId, salarioBase, horasExtras, bonoAsistencia, otrosIngresos, totalPercepciones,
                deduccionesFiscales, deduccionesSeguridadSocial, otrasDeducciones, totalDeducciones, netoAPagar,
                costoPatronal, fsrAplicado, diasTrabajados);
    }

    // Getters
    public EmpleadoId getEmpleadoId() {
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
