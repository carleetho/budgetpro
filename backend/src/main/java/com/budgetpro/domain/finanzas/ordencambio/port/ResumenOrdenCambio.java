package com.budgetpro.domain.finanzas.ordencambio.port;

import java.math.BigDecimal;

/**
 * DTO para presentar un resumen estadístico de órdenes de cambio por proyecto.
 */
public class ResumenOrdenCambio {
    private final int totalOrdenes;
    private final int aprobadas;
    private final int enRevision;
    private final int rechazadas;
    private final BigDecimal montoTotalAprobado;
    private final double impactoPresupuesto; // Porcentaje del presupuesto original
    private final int diasAdicionales;

    public ResumenOrdenCambio(int totalOrdenes, int aprobadas, int enRevision, int rechazadas,
            BigDecimal montoTotalAprobado, double impactoPresupuesto, int diasAdicionales) {
        this.totalOrdenes = totalOrdenes;
        this.aprobadas = aprobadas;
        this.enRevision = enRevision;
        this.rechazadas = rechazadas;
        this.montoTotalAprobado = montoTotalAprobado;
        this.impactoPresupuesto = impactoPresupuesto;
        this.diasAdicionales = diasAdicionales;
    }

    public int getTotalOrdenes() {
        return totalOrdenes;
    }

    public int getAprobadas() {
        return aprobadas;
    }

    public int getEnRevision() {
        return enRevision;
    }

    public int getRechazadas() {
        return rechazadas;
    }

    public BigDecimal getMontoTotalAprobado() {
        return montoTotalAprobado;
    }

    public double getImpactoPresupuesto() {
        return impactoPresupuesto;
    }

    public int getDiasAdicionales() {
        return diasAdicionales;
    }
}
