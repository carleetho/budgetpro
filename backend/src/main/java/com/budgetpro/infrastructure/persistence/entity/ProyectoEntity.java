package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.infrastructure.persistence.converter.EstadoProyectoConverter;
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
 * Entidad JPA para la tabla proyecto.
 *
 * Núcleo inmutable del presupuesto.
 */
@Entity
@Table(name = "proyecto",
       uniqueConstraints = @UniqueConstraint(name = "uq_proyecto_nombre", columnNames = "nombre"),
       indexes = {
           @Index(name = "idx_proyecto_estado", columnList = "estado")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProyectoEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 500)
    @Column(name = "ubicacion", length = 500)
    private String ubicacion;

    @NotNull
    @Convert(converter = EstadoProyectoConverter.class)
    @Column(name = "estado", nullable = false, length = 30)
    private com.budgetpro.domain.proyecto.model.EstadoProyecto estado;

    @NotBlank
    @Size(min = 3, max = 3)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "USD";

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "presupuesto_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal presupuestoTotal;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    private void prePersist() {
        if (moneda == null || moneda.isBlank()) {
            moneda = "USD";
        }
        if (presupuestoTotal == null) {
            presupuestoTotal = BigDecimal.ZERO;
        }
    }

    /**
     * Constructor de compatibilidad para mapeos existentes.
     */
    public ProyectoEntity(UUID id, String nombre, String ubicacion,
                          com.budgetpro.domain.proyecto.model.EstadoProyecto estado, Integer version) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.version = version;
    }
}
