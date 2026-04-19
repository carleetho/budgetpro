package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Especialidades bajo un presupuesto (Ej. Estructuras, Eléctricas). Opción B.
 */
@Entity
@Table(name = "subpresupuesto",
        indexes = @Index(name = "idx_subpresupuesto_presupuesto", columnList = "presupuesto_id"))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubpresupuestoEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presupuesto_id", nullable = false)
    private PresupuestoEntity presupuesto;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "total_presupuestado", precision = 19, scale = 4)
    private BigDecimal totalPresupuestado;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    private void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (orden == null) {
            orden = 0;
        }
    }
}
