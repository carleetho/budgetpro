package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cuadrillas", indexes = {
        @Index(name = "idx_cuadrillas_proyecto", columnList = "proyecto_id") }, uniqueConstraints = {
                @UniqueConstraint(name = "uk_cuadrilla_codigo_proyecto", columnNames = { "proyecto_id", "codigo" }) })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class CuadrillaEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private ProyectoEntity proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capataz_id")
    private EmpleadoEntity capataz;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "cuadrilla", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CuadrillaMiembroEntity> miembros = new ArrayList<>();
}
