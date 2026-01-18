package com.budgetpro.infrastructure.persistence.entity;

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
 * Entidad JPA para la tabla partida.
 *
 * Inmutabilidad: metradoOriginal no puede cambiar si el presupuesto está CONGELADO.
 */
@Entity
@Table(name = "partida",
       indexes = {
           @Index(name = "idx_partida_presupuesto", columnList = "presupuesto_id"),
           @Index(name = "idx_partida_padre", columnList = "padre_id"),
           @Index(name = "idx_partida_codigo", columnList = "presupuesto_id, codigo")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartidaEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presupuesto_id", nullable = false, updatable = false)
    private PresupuestoEntity presupuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private PartidaEntity padre;

    @NotBlank
    @Size(max = 50)
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "item", length = 50)
    private String item;

    @NotBlank
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Size(max = 20)
    @Column(name = "unidad", length = 20)
    private String unidad;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "metrado_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal metradoOriginal;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "metrado_vigente", nullable = false, precision = 19, scale = 4)
    private BigDecimal metradoVigente;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "gastos_reales", nullable = false, precision = 19, scale = 4)
    private BigDecimal gastosReales;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "compromisos_pendientes", nullable = false, precision = 19, scale = 4)
    private BigDecimal compromisosPendientes;

    @Column(name = "nivel")
    private Integer nivel;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Transient
    private BigDecimal metradoOriginalSnapshot;

    @PostLoad
    private void postLoad() {
        this.metradoOriginalSnapshot = this.metradoOriginal;
    }

    @PrePersist
    private void prePersist() {
        if (metradoVigente == null) {
            metradoVigente = metradoOriginal;
        }
        if (gastosReales == null) {
            gastosReales = BigDecimal.ZERO;
        }
        if (compromisosPendientes == null) {
            compromisosPendientes = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    private void preUpdate() {
        if (presupuesto != null
            && presupuesto.getEstado() == com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.CONGELADO
            && metradoOriginalSnapshot != null
            && metradoOriginal != null
            && metradoOriginal.compareTo(metradoOriginalSnapshot) != 0) {
            throw new IllegalStateException("metradoOriginal es inmutable tras la aprobación del presupuesto.");
        }
    }

    /**
     * Constructor de compatibilidad para mapeos existentes.
     */
    public PartidaEntity(UUID id, PresupuestoEntity presupuesto, PartidaEntity padre,
                         String item, String descripcion, String unidad,
                         BigDecimal metrado, Integer nivel, Integer version) {
        this.id = id;
        this.presupuesto = presupuesto;
        this.padre = padre;
        this.item = item;
        this.codigo = item;
        this.descripcion = descripcion;
        this.unidad = unidad;
        this.metradoOriginal = metrado;
        this.metradoVigente = metrado;
        this.precioUnitario = BigDecimal.ZERO;
        this.gastosReales = BigDecimal.ZERO;
        this.compromisosPendientes = BigDecimal.ZERO;
        this.nivel = nivel;
        this.version = version;
    }

    /**
     * Compatibilidad con lógica existente (metrado vigente).
     */
    public BigDecimal getMetrado() {
        return metradoVigente;
    }

    /**
     * Compatibilidad con lógica existente (metrado vigente).
     */
    public void setMetrado(BigDecimal metrado) {
        this.metradoVigente = metrado;
    }
}
