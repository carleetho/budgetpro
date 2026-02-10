package com.budgetpro.infrastructure.persistence.entity.cambio;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA para las órdenes de cambio.
 */
@Entity
@Table(name = "orden_cambio",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_orden_cambio_codigo_proyecto", columnNames = {"proyecto_id", "codigo"})
       },
       indexes = {
           @Index(name = "idx_orden_cambio_proyecto", columnList = "proyecto_id"),
           // REGLA-160
           // REGLA-161
           @Index(name = "idx_orden_cambio_estado", columnList = "estado")
       })
// REGLA-158
// REGLA-114
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrdenCambioEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false, updatable = false)
    private ProyectoEntity proyecto;

    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoOrdenCambio tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoOrdenCambio estado;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    // REGLA-159
    @Column(name = "impacto_presupuesto", nullable = false, precision = 19, scale = 4)
    private BigDecimal impactoPresupuesto;

    @NotNull
    @Column(name = "impacto_plazo", nullable = false)
    private Integer impactoPlazo;
}
