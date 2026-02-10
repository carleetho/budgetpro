package com.budgetpro.infrastructure.persistence.entity.rrhh;

// REGLA-125
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.infrastructure.persistence.converter.EstadoEmpleadoConverter;
import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import com.budgetpro.infrastructure.persistence.entity.JsonbMapConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "empleados", indexes = { @Index(name = "idx_empleados_estado", columnList = "estado"),
        @Index(name = "idx_empleados_nombre", columnList = "apellido, nombre") })
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion", nullable = false, updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_actualizacion", nullable = false)) })
public class EmpleadoEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "numero_identificacion", nullable = false, length = 50, unique = true)
    private String numeroIdentificacion;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Convert(converter = EstadoEmpleadoConverter.class)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEmpleado estado;

    @Convert(converter = JsonbMapConverter.class)
    @Column(name = "atributos", columnDefinition = "jsonb")
    private Map<String, Object> atributos;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialLaboralEntity> historialLaboral = new ArrayList<>();
}
