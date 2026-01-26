package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.infrastructure.persistence.converter.TipoEmpleadoConverter;
import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "historial_laboral", indexes = { @Index(name = "idx_historial_empleado", columnList = "empleado_id"),
        @Index(name = "idx_historial_fechas", columnList = "fecha_inicio, fecha_fin") })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class HistorialLaboralEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoEntity empleado;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Column(name = "salario_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal salarioBase;

    @Convert(converter = TipoEmpleadoConverter.class)
    @Column(name = "tipo_contrato", nullable = false, length = 50)
    private TipoEmpleado tipoEmpleado;

    @Column(name = "unidad_salario", nullable = false, length = 20)
    private String unidadSalario = "MENSUAL";

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "motivo_cambio", length = 255)
    private String motivoCambio;
}
