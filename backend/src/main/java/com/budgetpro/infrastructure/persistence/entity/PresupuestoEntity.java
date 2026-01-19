package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.infrastructure.persistence.converter.EstadoPresupuestoConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla presupuesto.
 *
 * Regla de negocio: una vez CONGELADO, el presupuesto es de solo lectura.
 */
@Entity
@Table(name = "presupuesto",
       indexes = {
           @Index(name = "idx_presupuesto_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_presupuesto_estado", columnList = "estado")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PresupuestoEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false, updatable = false, insertable = false)
    private ProyectoEntity proyecto;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @NotNull
    @Convert(converter = EstadoPresupuestoConverter.class)
    @Column(name = "estado", nullable = false, length = 30)
    private com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto estado;

    @NotNull
    @Column(name = "es_linea_base", nullable = false)
    private Boolean esLineaBase = Boolean.FALSE;

    @Column(name = "es_contractual", nullable = false)
    private Boolean esContractual = Boolean.FALSE;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // Integrity Hash Fields (Swiss-Grade Budget Sealing)
    @Column(name = "integrity_hash_approval", length = 64)
    private String integrityHashApproval;

    @Column(name = "integrity_hash_execution", length = 64)
    private String integrityHashExecution;

    @Column(name = "integrity_hash_generated_at")
    private LocalDateTime integrityHashGeneratedAt;

    @Column(name = "integrity_hash_generated_by")
    private UUID integrityHashGeneratedBy;

    @Column(name = "integrity_hash_algorithm", length = 20)
    private String integrityHashAlgorithm;

    @Transient
    private com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto estadoOriginal;

    @PostLoad
    private void postLoad() {
        this.estadoOriginal = this.estado;
    }

    @PrePersist
    private void prePersist() {
        if (esLineaBase == null) {
            esLineaBase = Boolean.FALSE;
        }
        if (esContractual == null) {
            esContractual = Boolean.FALSE;
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (estadoOriginal == com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.CONGELADO) {
            throw new IllegalStateException("Presupuesto CONGELADO es de solo lectura.");
        }
        if (estadoOriginal == com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.INVALIDADO) {
            throw new IllegalStateException("Presupuesto INVALIDADO es terminal y de solo auditoría.");
        }
    }

    /**
     * Constructor de compatibilidad para mapeos existentes.
     */
    public PresupuestoEntity(UUID id, UUID proyectoId, String nombre,
                             com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto estado,
                             Boolean esContractual, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.nombre = nombre;
        this.estado = estado;
        this.esContractual = esContractual;
        this.version = version;
    }
}
