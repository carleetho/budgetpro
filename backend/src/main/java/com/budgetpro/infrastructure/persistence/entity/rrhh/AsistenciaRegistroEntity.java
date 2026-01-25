package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.domain.rrhh.model.EstadoAsistencia;
import com.budgetpro.infrastructure.persistence.converter.EstadoAsistenciaConverter;
import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "asistencia_registros", indexes = {
        @Index(name = "idx_asistencia_empleado_fecha", columnList = "empleado_id, fecha"),
        @Index(name = "idx_asistencia_proyecto", columnList = "proyecto_id") })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class AsistenciaRegistroEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoEntity empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private ProyectoEntity proyecto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "hora_inicio_break")
    private LocalTime horaInicioBreak;

    @Column(name = "hora_fin_break")
    private LocalTime horaFinBreak;

    @Column(name = "horas_trabajadas", precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(name = "horas_extras", precision = 5, scale = 2)
    private BigDecimal horasExtras;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Convert(converter = EstadoAsistenciaConverter.class)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAsistencia estado;
}
