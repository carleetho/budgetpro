package com.budgetpro.infrastructure.persistence.entity.rrhh;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.JsonbMapConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "nomina_detalles", indexes = {
        @Index(name = "idx_nomina_detalles_nomina", columnList = "nomina_id") }, uniqueConstraints = {
                @UniqueConstraint(name = "uk_nomina_empleado", columnNames = { "nomina_id", "empleado_id" }) })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class NominaDetalleEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomina_id", nullable = false)
    private NominaEntity nomina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoEntity empleado;

    @Column(name = "salario_base_calculado", nullable = false, precision = 19, scale = 2)
    private BigDecimal salarioBaseCalculado;

    @Column(name = "horas_regulares", precision = 5, scale = 2)
    private BigDecimal horasRegulares;

    @Column(name = "horas_extras", precision = 5, scale = 2)
    private BigDecimal horasExtras;

    @Column(name = "monto_horas_extras", precision = 19, scale = 2)
    private BigDecimal montoHorasExtras;

    @Column(name = "total_deducciones", precision = 19, scale = 2)
    private BigDecimal totalDeducciones;

    @Column(name = "total_bonificaciones", precision = 19, scale = 2)
    private BigDecimal totalBonificaciones;

    @Column(name = "neto_pagar", nullable = false, precision = 19, scale = 2)
    private BigDecimal netoPagar;

    @Convert(converter = JsonbMapConverter.class)
    @Column(name = "detalles_calculo", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> detallesCalculo;
}
