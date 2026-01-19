package com.budgetpro.infrastructure.persistence.entity.catalogo;

import com.budgetpro.domain.catalogo.model.EstadoProxy;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla recurso_proxy.
 */
@Entity
@Table(name = "recurso_proxy",
       uniqueConstraints = @UniqueConstraint(name = "uq_recurso_proxy_external",
                                             columnNames = {"external_id", "catalog_source"}),
       indexes = {
           @Index(name = "idx_recurso_proxy_external", columnList = "external_id, catalog_source"),
           @Index(name = "idx_recurso_proxy_estado", columnList = "estado"),
           @Index(name = "idx_recurso_proxy_tipo", columnList = "tipo_snapshot")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RecursoProxyEntity extends AuditEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @NotBlank
    @Column(name = "catalog_source", nullable = false, length = 50)
    private String catalogSource;

    @NotBlank
    @Column(name = "nombre_snapshot", nullable = false, length = 500)
    private String nombreSnapshot;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_snapshot", nullable = false, length = 50)
    private TipoRecurso tipoSnapshot;

    @NotBlank
    @Column(name = "unidad_snapshot", nullable = false, length = 50)
    private String unidadSnapshot;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "precio_snapshot", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioSnapshot;

    @NotNull
    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoProxy estado = EstadoProxy.ACTIVO;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
