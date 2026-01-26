package com.budgetpro.infrastructure.persistence.entity.catalogo;

import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla apu_insumo_snapshot.
 */
@Entity
@Table(name = "apu_insumo_snapshot", indexes = {
        @Index(name = "idx_apu_insumo_snapshot_apu", columnList = "apu_snapshot_id"),
        @Index(name = "idx_apu_insumo_snapshot_recurso", columnList = "recurso_external_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApuInsumoSnapshotEntity extends AuditEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apu_snapshot_id", nullable = false, updatable = false)
    private ApuSnapshotEntity apuSnapshot;

    @NotBlank
    @Column(name = "recurso_external_id", nullable = false, length = 255)
    private String recursoExternalId;

    @NotBlank
    @Column(name = "recurso_nombre", nullable = false, length = 500)
    private String recursoNombre;

    @NotNull
    @Digits(integer = 15, fraction = 6)
    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    // Campos de clasificación
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", length = 50)
    private TipoRecurso tipoRecurso;

    @Column(name = "orden_calculo")
    private Integer ordenCalculo;

    // Campos de unidades
    @Digits(integer = 15, fraction = 6)
    @Column(name = "aporte_unitario", precision = 19, scale = 6)
    private BigDecimal aporteUnitario;

    @Column(name = "unidad_aporte", length = 50)
    private String unidadAporte;

    @Column(name = "unidad_base", length = 50)
    private String unidadBase;

    @Digits(integer = 15, fraction = 6)
    @Column(name = "factor_conversion_unidad_base", precision = 19, scale = 6)
    private BigDecimal factorConversionUnidadBase;

    @Column(name = "unidad_compra", length = 50)
    private String unidadCompra;

    // Campos de precio/moneda
    @Column(name = "moneda", length = 3)
    private String moneda;

    @Digits(integer = 15, fraction = 6)
    @Column(name = "tipo_cambio_snapshot", precision = 19, scale = 6)
    private BigDecimal tipoCambioSnapshot;

    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_mercado", precision = 19, scale = 4)
    private BigDecimal precioMercado;

    @Digits(integer = 15, fraction = 4)
    @Column(name = "flete", precision = 19, scale = 4)
    private BigDecimal flete;

    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_puesto_en_obra", precision = 19, scale = 4)
    private BigDecimal precioPuestoEnObra;

    // Campos específicos MATERIAL
    @Digits(integer = 3, fraction = 4)
    @Column(name = "desperdicio", precision = 7, scale = 4)
    private BigDecimal desperdicio;

    // Campos específicos MANO_OBRA
    @OneToMany(mappedBy = "apuInsumoSnapshot", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ComposicionCuadrillaSnapshotEntity> composicionCuadrilla = new ArrayList<>();

    @Digits(integer = 15, fraction = 4)
    @Column(name = "costo_dia_cuadrilla_calculado", precision = 19, scale = 4)
    private BigDecimal costoDiaCuadrillaCalculado;

    @Column(name = "jornada_horas")
    private Integer jornadaHoras;

    // Campos específicos EQUIPO_MAQUINA
    @Digits(integer = 15, fraction = 4)
    @Column(name = "costo_hora_maquina", precision = 19, scale = 4)
    private BigDecimal costoHoraMaquina;

    @Digits(integer = 15, fraction = 6)
    @Column(name = "horas_uso", precision = 19, scale = 6)
    private BigDecimal horasUso;

    // Campos específicos EQUIPO_HERRAMIENTA
    @Digits(integer = 3, fraction = 4)
    @Column(name = "porcentaje_mano_obra", precision = 7, scale = 4)
    private BigDecimal porcentajeManoObra;

    @Column(name = "depende_de", length = 255)
    private String dependeDe;
}
