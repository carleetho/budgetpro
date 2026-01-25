package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ConfiguracionLaboralExtendidaResponse(String id, ProyectoId proyectoId, LocalDate fechaInicio,
        LocalDate fechaFin, boolean activa, BigDecimal fsrBase, BigDecimal fsrExtendido, Integer diasAguinaldo,
        Integer diasVacaciones, BigDecimal porcentajeSeguridadSocial, Integer diasNoTrabajados,
        Integer diasLaborablesAno, BigDecimal factorHorasExtras, BigDecimal factorTurnoNocturno,
        BigDecimal factorRiesgo, BigDecimal factorRegional) {
}
