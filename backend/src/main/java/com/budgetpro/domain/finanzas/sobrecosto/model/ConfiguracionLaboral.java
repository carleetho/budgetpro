package com.budgetpro.domain.finanzas.sobrecosto.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public final class ConfiguracionLaboral {
    private final ConfiguracionLaboralId id;
    // nosemgrep
    private UUID proyectoId;
    // nosemgrep
    private Integer diasAguinaldo;
    // nosemgrep
    private Integer diasVacaciones;
    // nosemgrep
    private BigDecimal porcentajeSeguridadSocial;
    // nosemgrep
    private Integer diasNoTrabajados;
    // nosemgrep
    private Integer diasLaborablesAno;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    private ConfiguracionLaboral(ConfiguracionLaboralId id, UUID proyectoId,
                                Integer diasAguinaldo, Integer diasVacaciones,
                                BigDecimal porcentajeSeguridadSocial, Integer diasNoTrabajados,
                                Integer diasLaborablesAno, Long version) {
        validarInvariantes(diasAguinaldo, diasVacaciones, porcentajeSeguridadSocial,
                          diasNoTrabajados, diasLaborablesAno);
        this.id = Objects.requireNonNull(id);
        this.proyectoId = proyectoId;
        this.diasAguinaldo = diasAguinaldo != null ? diasAguinaldo : 0;
        this.diasVacaciones = diasVacaciones != null ? diasVacaciones : 0;
        this.porcentajeSeguridadSocial = porcentajeSeguridadSocial != null ? porcentajeSeguridadSocial : BigDecimal.ZERO;
        this.diasNoTrabajados = diasNoTrabajados != null ? diasNoTrabajados : 0;
        this.diasLaborablesAno = diasLaborablesAno != null ? diasLaborablesAno : 251;
        this.version = version != null ? version : 0L;
    }

    public static ConfiguracionLaboral crearGlobal(ConfiguracionLaboralId id,
                                                   Integer diasAguinaldo, Integer diasVacaciones,
                                                   BigDecimal porcentajeSeguridadSocial,
                                                   Integer diasNoTrabajados, Integer diasLaborablesAno) {
        return new ConfiguracionLaboral(id, null, diasAguinaldo, diasVacaciones,
                                       porcentajeSeguridadSocial, diasNoTrabajados,
                                       diasLaborablesAno, 0L);
    }

    public static ConfiguracionLaboral crearPorProyecto(ConfiguracionLaboralId id, UUID proyectoId,
                                                       Integer diasAguinaldo, Integer diasVacaciones,
                                                       BigDecimal porcentajeSeguridadSocial,
                                                       Integer diasNoTrabajados, Integer diasLaborablesAno) {
        return new ConfiguracionLaboral(id, proyectoId, diasAguinaldo, diasVacaciones,
                                       porcentajeSeguridadSocial, diasNoTrabajados,
                                       diasLaborablesAno, 0L);
    }

    public static ConfiguracionLaboral reconstruir(ConfiguracionLaboralId id, UUID proyectoId,
                                                   Integer diasAguinaldo, Integer diasVacaciones,
                                                   BigDecimal porcentajeSeguridadSocial,
                                                   Integer diasNoTrabajados, Integer diasLaborablesAno,
                                                   Long version) {
        return new ConfiguracionLaboral(id, proyectoId, diasAguinaldo, diasVacaciones,
                                       porcentajeSeguridadSocial, diasNoTrabajados,
                                       diasLaborablesAno, version);
    }

    private void validarInvariantes(Integer diasAguinaldo, Integer diasVacaciones,
                                   BigDecimal porcentajeSeguridadSocial,
                                   Integer diasNoTrabajados, Integer diasLaborablesAno) {
        if (diasAguinaldo != null && diasAguinaldo < 0) {
            throw new IllegalArgumentException("Los días de aguinaldo no pueden ser negativos");
        }
        if (diasVacaciones != null && diasVacaciones < 0) {
            throw new IllegalArgumentException("Los días de vacaciones no pueden ser negativos");
        }
        if (porcentajeSeguridadSocial != null) {
            if (porcentajeSeguridadSocial.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El porcentaje de seguridad social no puede ser negativo");
            }
            if (porcentajeSeguridadSocial.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("El porcentaje de seguridad social no puede ser mayor a 100%");
            }
        }
        if (diasNoTrabajados != null && diasNoTrabajados < 0) {
            throw new IllegalArgumentException("Los días no trabajados no pueden ser negativos");
        }
        if (diasLaborablesAno != null && diasLaborablesAno <= 0) {
            throw new IllegalArgumentException("Los días laborables al año deben ser positivos");
        }
    }

    public BigDecimal calcularFSR() {
        BigDecimal totalTrabajado = new BigDecimal(this.diasLaborablesAno);
        BigDecimal totalPagado = new BigDecimal(this.diasLaborablesAno)
                .add(new BigDecimal(this.diasVacaciones))
                .add(new BigDecimal(this.diasAguinaldo))
                .add(new BigDecimal(this.diasNoTrabajados));
        if (totalPagado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return totalTrabajado.divide(totalPagado, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularSalarioReal(BigDecimal salarioBase) {
        if (salarioBase == null || salarioBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El salario base debe ser positivo");
        }
        BigDecimal fsr = calcularFSR();
        return salarioBase.multiply(fsr).setScale(4, RoundingMode.HALF_UP);
    }

    public ConfiguracionLaboralId getId() { return id; }
    public UUID getProyectoId() { return proyectoId; }
    public Integer getDiasAguinaldo() { return diasAguinaldo; }
    public Integer getDiasVacaciones() { return diasVacaciones; }
    public BigDecimal getPorcentajeSeguridadSocial() { return porcentajeSeguridadSocial; }
    public Integer getDiasNoTrabajados() { return diasNoTrabajados; }
    public Integer getDiasLaborablesAno() { return diasLaborablesAno; }
    public Long getVersion() { return version; }
    public boolean isGlobal() { return proyectoId == null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfiguracionLaboral that = (ConfiguracionLaboral) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ConfiguracionLaboral{id=%s, proyectoId=%s, fsr=%s}", 
                           id, proyectoId, calcularFSR());
    }
}
