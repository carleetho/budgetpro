package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.rrhh.model.Empleado;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CalculadorFSR {

    public BigDecimal calcularFSR(ConfiguracionLaboral config, Empleado empleado) {
        Objects.requireNonNull(config, "ConfiguracionLaboral must not be null");
        Objects.requireNonNull(empleado, "Empleado must not be null");

        // Note: We use the values from ConfiguracionLaboral but implement the standard
        // FSR formula:
        // FSR = (Total Paid Days / Total Worked Days)
        // Total Paid Days = Days in Year + Vacations + Aguinaldo + Holidays
        // (DiasNoTrabajados)
        // Total Worked Days = Days in Year (DiasLaborablesAno)
        // Note: The existing method in ConfiguracionLaboral might be inverted, so we
        // implement it here explicitly
        // to ensure correctness for the payroll calculation and allow for
        // employee-specific overrides in the future.

        BigDecimal diasLaborables = new BigDecimal(config.getDiasLaborablesAno());

        // Potential future improvement: Calculate vacation days based on employee
        // seniority (antigüedad)
        // using empleado.getFechaIngreso() and a seniority table.
        // For now, we use the project/global configuration default.
        BigDecimal diasVacaciones = new BigDecimal(config.getDiasVacaciones());

        BigDecimal diasAguinaldo = new BigDecimal(config.getDiasAguinaldo());
        BigDecimal diasNoTrabajados = new BigDecimal(config.getDiasNoTrabajados());

        BigDecimal totalPagado = diasLaborables.add(diasVacaciones).add(diasAguinaldo).add(diasNoTrabajados);

        if (diasLaborables.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // FSR = Total Pagado / Dias Laborables
        return totalPagado.divide(diasLaborables, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularCostoPatronal(ConfiguracionLaboral config, BigDecimal salarioBase) {
        // This can be used to calculate employer cost including Social Security
        if (config.getPorcentajeSeguridadSocial() == null) {
            return BigDecimal.ZERO;
        }
        return salarioBase
                .multiply(config.getPorcentajeSeguridadSocial().divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
    }
}
