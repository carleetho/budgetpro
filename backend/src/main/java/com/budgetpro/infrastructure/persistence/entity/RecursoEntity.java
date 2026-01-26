package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.domain.recurso.model.EstadoRecurso;
import com.budgetpro.domain.shared.model.TipoRecurso;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla `recurso` del ERD físico. Representa la
 * persistencia del agregado Recurso del dominio.
 */
@Entity
@Table(name = "recurso", uniqueConstraints = {
        @UniqueConstraint(name = "uq_recurso_nombre", columnNames = "nombre_normalizado") }, indexes = {
                @Index(name = "idx_recurso_tipo", columnList = "tipo"),
                @Index(name = "idx_recurso_estado", columnList = "estado") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RecursoEntity extends AuditEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "nombre", nullable = false, columnDefinition = "TEXT")
    private String nombre;

    @NotBlank
    @Column(name = "nombre_normalizado", nullable = false, unique = true, columnDefinition = "TEXT")
    private String nombreNormalizado;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoRecurso tipo;

    @NotBlank
    @Size(max = 20)
    @Column(name = "unidad_base", nullable = false, length = 20)
    private String unidadBase;

    @Size(max = 20)
    @Column(name = "unidad", length = 20)
    private String unidad;

    @NotNull
    @Digits(integer = 15, fraction = 4)
    @Column(name = "costo_referencia", nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal costoReferencia;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "atributos", nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> atributos = new HashMap<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoRecurso estado = EstadoRecurso.ACTIVO;

    /**
     * Constructor de compatibilidad para mapeos existentes.
     */
    public RecursoEntity(UUID id, String nombre, String nombreNormalizado, TipoRecurso tipo, String unidadBase,
            Map<String, Object> atributos, EstadoRecurso estado, UUID createdBy) {
        this.id = id;
        this.nombre = nombre;
        this.nombreNormalizado = nombreNormalizado;
        this.tipo = tipo;
        this.unidadBase = unidadBase;
        this.atributos = atributos != null ? new HashMap<>(atributos) : new HashMap<>();
        this.estado = estado != null ? estado : EstadoRecurso.ACTIVO;
        setCreatedBy(createdBy);
    }
}
