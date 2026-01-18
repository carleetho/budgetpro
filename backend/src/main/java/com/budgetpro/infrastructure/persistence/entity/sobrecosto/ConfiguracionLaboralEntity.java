package com.budgetpro.infrastructure.persistence.entity.sobrecosto;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla configuracion_laboral.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "configuracion_laboral",
       indexes = {
           @Index(name = "idx_config_laboral_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_config_laboral_global", columnList = "proyecto_id", unique = true)
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_config_laboral_global", columnNames = "proyecto_id")
       })
public class ConfiguracionLaboralEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", unique = true)
    private UUID proyectoId; // null para configuración global (singleton)

    @Column(name = "dias_aguinaldo", nullable = false)
    private Integer diasAguinaldo;

    @Column(name = "dias_vacaciones", nullable = false)
    private Integer diasVacaciones;

    @Column(name = "porcentaje_seguridad_social", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeSeguridadSocial;

    @Column(name = "dias_no_trabajados", nullable = false)
    private Integer diasNoTrabajados;

    @Column(name = "dias_laborables_ano", nullable = false)
    private Integer diasLaborablesAno;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected ConfiguracionLaboralEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public ConfiguracionLaboralEntity(UUID id, UUID proyectoId,
                                      Integer diasAguinaldo, Integer diasVacaciones,
                                      BigDecimal porcentajeSeguridadSocial,
                                      Integer diasNoTrabajados, Integer diasLaborablesAno,
                                      Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.diasAguinaldo = diasAguinaldo;
        this.diasVacaciones = diasVacaciones;
        this.porcentajeSeguridadSocial = porcentajeSeguridadSocial;
        this.diasNoTrabajados = diasNoTrabajados;
        this.diasLaborablesAno = diasLaborablesAno;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public Integer getDiasAguinaldo() {
        return diasAguinaldo;
    }

    public void setDiasAguinaldo(Integer diasAguinaldo) {
        this.diasAguinaldo = diasAguinaldo;
    }

    public Integer getDiasVacaciones() {
        return diasVacaciones;
    }

    public void setDiasVacaciones(Integer diasVacaciones) {
        this.diasVacaciones = diasVacaciones;
    }

    public BigDecimal getPorcentajeSeguridadSocial() {
        return porcentajeSeguridadSocial;
    }

    public void setPorcentajeSeguridadSocial(BigDecimal porcentajeSeguridadSocial) {
        this.porcentajeSeguridadSocial = porcentajeSeguridadSocial;
    }

    public Integer getDiasNoTrabajados() {
        return diasNoTrabajados;
    }

    public void setDiasNoTrabajados(Integer diasNoTrabajados) {
        this.diasNoTrabajados = diasNoTrabajados;
    }

    public Integer getDiasLaborablesAno() {
        return diasLaborablesAno;
    }

    public void setDiasLaborablesAno(Integer diasLaborablesAno) {
        this.diasLaborablesAno = diasLaborablesAno;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
