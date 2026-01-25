package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cuadrilla_miembros", indexes = {
        @Index(name = "idx_cuadrilla_miembros_empleado", columnList = "empleado_id") }, uniqueConstraints = {
                @UniqueConstraint(name = "uk_miembro_activo_cuadrilla", columnNames = { "empleado_id",
                        "cuadrilla_id" }) })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class CuadrillaMiembroEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuadrilla_id", nullable = false)
    private CuadrillaEntity cuadrilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoEntity empleado;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
