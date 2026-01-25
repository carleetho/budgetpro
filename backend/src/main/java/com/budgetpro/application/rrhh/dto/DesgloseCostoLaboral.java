package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;
import java.time.Duration;

public class DesgloseCostoLaboral {
    private final String idGrupo;
    private final String nombreGrupo;
    private final Duration horasNormales;
    private final Duration horasExtras;
    private final BigDecimal costoTotal;
    private final BigDecimal costoPromedioHora;

    public DesgloseCostoLaboral(String idGrupo, String nombreGrupo, Duration horasNormales, Duration horasExtras,
            BigDecimal costoTotal, BigDecimal costoPromedioHora) {
        this.idGrupo = idGrupo;
        this.nombreGrupo = nombreGrupo;
        this.horasNormales = horasNormales;
        this.horasExtras = horasExtras;
        this.costoTotal = costoTotal;
        this.costoPromedioHora = costoPromedioHora;
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public Duration getHorasNormales() {
        return horasNormales;
    }

    public Duration getHorasExtras() {
        return horasExtras;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public BigDecimal getCostoPromedioHora() {
        return costoPromedioHora;
    }
}
