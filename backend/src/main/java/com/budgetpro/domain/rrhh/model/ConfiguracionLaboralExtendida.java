package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa la configuración laboral extendida con histórico y
 * factores adicionales. Reemplaza/Extiende la lógica de ConfiguracionLaboral
 * original para el módulo de RRHH.
 */
public class ConfiguracionLaboralExtendida {

    private final String id;
    private final ProyectoId proyectoId; // Puede ser null para configuración Global
    private final LocalDate fechaInicio;
    private LocalDate fechaFin; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - Mutable
                                // end date
    private Map<String, BigDecimal> factores; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh -
                                              // Regional labor factors updated by HR

    // Parámetros Base
    private final Integer diasAguinaldo;
    private final Integer diasVacaciones;
    private final BigDecimal porcentajeSeguridadSocial;
    private final Integer diasNoTrabajados;
    private final Integer diasLaborablesAno;

    // Parámetros Extendidos
    private final BigDecimal factorHorasExtras;
    private final BigDecimal factorTurnoNocturno;
    private final BigDecimal factorRiesgo;
    private final BigDecimal factorRegional;

    public ConfiguracionLaboralExtendida(String id, ProyectoId proyectoId, LocalDate fechaInicio, LocalDate fechaFin,
            Integer diasAguinaldo, Integer diasVacaciones, BigDecimal porcentajeSeguridadSocial,
            Integer diasNoTrabajados, Integer diasLaborablesAno, BigDecimal factorHorasExtras,
            BigDecimal factorTurnoNocturno, BigDecimal factorRiesgo, BigDecimal factorRegional) {
        this.id = Objects.requireNonNull(id, "ID no puede ser nulo");
        this.proyectoId = proyectoId;
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "Fecha inicio no puede ser nula");
        this.fechaFin = fechaFin;

        this.diasAguinaldo = diasAguinaldo != null ? diasAguinaldo : 0;
        this.diasVacaciones = diasVacaciones != null ? diasVacaciones : 0;
        this.porcentajeSeguridadSocial = porcentajeSeguridadSocial != null ? porcentajeSeguridadSocial
                : BigDecimal.ZERO;
        this.diasNoTrabajados = diasNoTrabajados != null ? diasNoTrabajados : 0;
        this.diasLaborablesAno = diasLaborablesAno != null ? diasLaborablesAno : 251;

        this.factorHorasExtras = factorHorasExtras != null ? factorHorasExtras : BigDecimal.ZERO;
        this.factorTurnoNocturno = factorTurnoNocturno != null ? factorTurnoNocturno : BigDecimal.ZERO;
        this.factorRiesgo = factorRiesgo != null ? factorRiesgo : BigDecimal.ZERO;
        this.factorRegional = factorRegional != null ? factorRegional : BigDecimal.ZERO;

        validar();
    }

    public static ConfiguracionLaboralExtendida crear(ProyectoId proyectoId, LocalDate fechaInicio,
            Integer diasAguinaldo, Integer diasVacaciones, BigDecimal porcentajeSeguridadSocial,
            Integer diasNoTrabajados, Integer diasLaborablesAno, BigDecimal factorHorasExtras,
            BigDecimal factorTurnoNocturno, BigDecimal factorRiesgo, BigDecimal factorRegional) {
        return new ConfiguracionLaboralExtendida(UUID.randomUUID().toString(), proyectoId, fechaInicio, null,
                diasAguinaldo, diasVacaciones, porcentajeSeguridadSocial, diasNoTrabajados, diasLaborablesAno,
                factorHorasExtras, factorTurnoNocturno, factorRiesgo, factorRegional);
    }

    private void validar() {
        if (diasAguinaldo < 0)
            throw new IllegalArgumentException("Días aguinaldo no puede ser negativo");
        if (diasVacaciones < 0)
            throw new IllegalArgumentException("Días vacaciones no puede ser negativo");
        if (porcentajeSeguridadSocial.compareTo(BigDecimal.ZERO) < 0
                || porcentajeSeguridadSocial.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Porcentaje seguridad social inválido");
        if (diasLaborablesAno <= 0)
            throw new IllegalArgumentException("Días laborables debe ser positivo");

        // Validar factores extendidos (asumiendo que son porcentajes o multiplicadores
        // positivos)
        if (factorHorasExtras.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Factor horas extras negativo");
        // ... otros validadores
    }

    public void cerrar(LocalDate fechaCierre) {
        if (fechaCierre == null)
            throw new IllegalArgumentException("Fecha cierre no puede ser nula");
        if (fechaCierre.isBefore(fechaInicio))
            throw new IllegalArgumentException("Fecha cierre no puede ser anterior a inicio");
        this.fechaFin = fechaCierre;
    }

    public boolean esActiva() {
        return fechaFin == null;
    }

    public boolean esGlobal() {
        return proyectoId == null;
    }

    public BigDecimal calcularFSRBase() {
        // Lógica original simplificada
        BigDecimal totalPagado = BigDecimal.valueOf(diasLaborablesAno).add(BigDecimal.valueOf(diasVacaciones))
                .add(BigDecimal.valueOf(diasAguinaldo)).add(BigDecimal.valueOf(diasNoTrabajados)); // Ajustar si
                                                                                                   // diasNoTrabajados
                                                                                                   // suma o resta
                                                                                                   // dependiendo de la
                                                                                                   // definicion exacta

        // Nota: En modelo original era diasLaborables + Vacaciones + Aguinaldo +
        // NoTrabajados. Asumimos Total Pagado / Total Trabajado
        BigDecimal totalTrabajado = BigDecimal.valueOf(diasLaborablesAno);

        if (totalPagado.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ONE;

        // FSR Base = (TP / TT) + (Seguridad Social / 100)
        // Ojo: Modelo original solo dividia TP/TT. Agregamos seguridad social
        // simplificado si se requiere,
        // pero seguiremos logica original: FSR = TP / TT

        BigDecimal fsr = totalPagado.divide(totalTrabajado, 4, RoundingMode.HALF_UP);

        // Agregar Seguridad Social si es porcentaje directo sobre la base
        BigDecimal ssFactor = porcentajeSeguridadSocial.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        return fsr.add(ssFactor);
    }

    public BigDecimal calcularFSRExtendido() {
        BigDecimal base = calcularFSRBase();

        // Formula Extendida: Base * (1 + SumaFactores)
        BigDecimal sumaFactores = factorHorasExtras.add(factorTurnoNocturno).add(factorRiesgo).add(factorRegional);

        return base.multiply(BigDecimal.ONE.add(sumaFactores)).setScale(4, RoundingMode.HALF_UP);
    }

    // Getters

    public String getId() {
        return id;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Integer getDiasAguinaldo() {
        return diasAguinaldo;
    }

    public Integer getDiasVacaciones() {
        return diasVacaciones;
    }

    public BigDecimal getPorcentajeSeguridadSocial() {
        return porcentajeSeguridadSocial;
    }

    public Integer getDiasNoTrabajados() {
        return diasNoTrabajados;
    }

    public Integer getDiasLaborablesAno() {
        return diasLaborablesAno;
    }

    public BigDecimal getFactorHorasExtras() {
        return factorHorasExtras;
    }

    public BigDecimal getFactorTurnoNocturno() {
        return factorTurnoNocturno;
    }

    public BigDecimal getFactorRiesgo() {
        return factorRiesgo;
    }

    public BigDecimal getFactorRegional() {
        return factorRegional;
    }
}
