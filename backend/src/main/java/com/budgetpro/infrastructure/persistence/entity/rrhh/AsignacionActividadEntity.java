package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "asignaciones_actividad", indexes = {
        @Index(name = "idx_actividad_cuadrilla_partida", columnList = "cuadrilla_id, partida_id"),
        @Index(name = "idx_actividad_fecha", columnList = "fecha") })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class AsignacionActividadEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuadrilla_id", nullable = false)
    private CuadrillaEntity cuadrilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private PartidaEntity partida;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_asignadas", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasAsignadas;

    @Column(name = "observaciones", length = 255)
    private String observaciones;
}
