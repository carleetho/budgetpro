package com.budgetpro.infrastructure.persistence.entity.produccion;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para reporte de producción (RPC).
 */
@Entity
@Table(name = "reporte_produccion",
       indexes = {
           @Index(name = "idx_rpc_fecha", columnList = "fecha_reporte"),
           @Index(name = "idx_rpc_responsable", columnList = "responsable_id"),
           @Index(name = "idx_rpc_estado", columnList = "estado")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReporteProduccionEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "fecha_reporte", nullable = false)
    private LocalDate fechaReporte;

    @NotNull
    @Column(name = "responsable_id", nullable = false)
    private UUID responsableId;

    @Column(name = "aprobador_id")
    private UUID aprobadorId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReporteProduccion estado;

    @Size(max = 1000)
    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Size(max = 200)
    @Column(name = "ubicacion_gps", length = 200)
    private String ubicacionGps;

    @OneToMany(mappedBy = "reporteProduccion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleRPCEntity> detalles = new ArrayList<>();
}
