package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "nominas", indexes = {
        @Index(name = "idx_nominas_proyecto_periodo", columnList = "proyecto_id, periodo_inicio") })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class NominaEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private ProyectoEntity proyecto;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "total_bruto", precision = 19, scale = 2)
    private BigDecimal totalBruto;

    @Column(name = "total_neto", precision = 19, scale = 2)
    private BigDecimal totalNeto;

    @Column(name = "cantidad_empleados")
    private Integer cantidadEmpleados;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "nomina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NominaDetalleEntity> detalles = new ArrayList<>();
}
