package com.budgetpro.infrastructure.persistence.entity;

// REGLA-110
// REGLA-111
// REGLA-112
// REGLA-152
import com.budgetpro.infrastructure.persistence.converter.EstadoPresupuestoConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
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
           // REGLA-153
           @Index(name = "idx_presupuesto_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_presupuesto_estado", columnList = "estado")
       })
// REGLA-157
// REGLA-156
// REGLA-155
// REGLA-154
// REGLA-143
// REGLA-113
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
// REGLA-061
// REGLA-062
// REGLA-063
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
    // REGLA-101
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

    @Column(name = "codigo", length = 50)
    private String codigo;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "distrito_id")
    private UUID distritoId;

    @Column(name = "fecha_elaboracion")
    private LocalDate fechaElaboracion;

    @Column(name = "plazo_dias")
    private Integer plazoDias;

    @NotNull
    @Column(name = "jornada_diaria", nullable = false, precision = 5, scale = 2)
    private BigDecimal jornadaDiaria;

    @Column(name = "moneda_base_id")
    private UUID monedaBaseId;

    @Column(name = "moneda_alterna_id")
    private UUID monedaAlternaId;

    @Column(name = "factor_cambio", precision = 19, scale = 8)
    private BigDecimal factorCambio;

    @NotNull
    @Column(name = "requiere_formula_polinomica", nullable = false)
    private Boolean requiereFormulaPolinomica = Boolean.FALSE;

    @NotBlank
    @Column(name = "tipo_apu", nullable = false, length = 20)
    private String tipoApu;

    @Column(name = "decimales_precios")
    private Integer decimalesPrecios;

    @Column(name = "decimales_metrados")
    private Integer decimalesMetrados;

    @Column(name = "decimales_incidencias")
    private Integer decimalesIncidencias;

    @NotNull
    @Column(name = "es_contractual_vigente", nullable = false)
    private Boolean esContractualVigente = Boolean.FALSE;

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
        if (jornadaDiaria == null) {
            jornadaDiaria = new BigDecimal("8.00");
        }
        if (tipoApu == null) {
            tipoApu = "EDIFICACIONES";
        }
        if (requiereFormulaPolinomica == null) {
            requiereFormulaPolinomica = Boolean.FALSE;
        }
        if (esContractualVigente == null) {
            esContractualVigente = Boolean.FALSE;
        }
    }

    @PreUpdate
    private void preUpdate() {
        // REGLA-046
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
