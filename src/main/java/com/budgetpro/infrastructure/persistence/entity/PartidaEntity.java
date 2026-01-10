package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.domain.finanzas.partida.EstadoPartida;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import jakarta.persistence.*;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla `partida` del ERD físico.
 * Representa la persistencia del agregado Partida del dominio.
 */
@Entity
@Table(name = "partida",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_partida_presupuesto_codigo", columnNames = {"presupuesto_id", "codigo"})
       },
       indexes = {
           @Index(name = "idx_partida_presupuesto", columnList = "presupuesto_id"),
           @Index(name = "idx_partida_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_partida_codigo", columnList = "codigo"),
           @Index(name = "idx_partida_estado", columnList = "estado"),
           @Index(name = "idx_partida_tipo", columnList = "tipo")
       })
public class PartidaEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_partida_presupuesto"))
    private PresupuestoEntity presupuesto;

    @Column(name = "proyecto_id", nullable = false, columnDefinition = "UUID")
    private UUID proyectoId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, columnDefinition = "TEXT")
    private String nombre;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoRecurso tipo;

    @Column(name = "monto_presupuestado", nullable = false, precision = 19, scale = 4, 
            columnDefinition = "NUMERIC(19,4) NOT NULL DEFAULT 0")
    private BigDecimal montoPresupuestado = BigDecimal.ZERO;

    @Column(name = "monto_reservado", nullable = false, precision = 19, scale = 4,
            columnDefinition = "NUMERIC(19,4) NOT NULL DEFAULT 0")
    private BigDecimal montoReservado = BigDecimal.ZERO;

    @Column(name = "monto_ejecutado", nullable = false, precision = 19, scale = 4,
            columnDefinition = "NUMERIC(19,4) NOT NULL DEFAULT 0")
    private BigDecimal montoEjecutado = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "estado_partida")
    private EstadoPartida estado = EstadoPartida.BORRADOR;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors

    public PartidaEntity() {
        // Constructor vacío requerido por JPA
    }

    public PartidaEntity(UUID id, PresupuestoEntity presupuesto, UUID proyectoId, String codigo,
                        String nombre, TipoRecurso tipo, BigDecimal montoPresupuestado,
                        BigDecimal montoReservado, BigDecimal montoEjecutado, EstadoPartida estado,
                        Long version) {
        this.id = id;
        this.presupuesto = presupuesto;
        this.proyectoId = proyectoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.montoPresupuestado = montoPresupuestado != null ? montoPresupuestado : BigDecimal.ZERO;
        this.montoReservado = montoReservado != null ? montoReservado : BigDecimal.ZERO;
        this.montoEjecutado = montoEjecutado != null ? montoEjecutado : BigDecimal.ZERO;
        this.estado = estado != null ? estado : EstadoPartida.BORRADOR;
        this.version = version != null ? version : 0L;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PresupuestoEntity getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(PresupuestoEntity presupuesto) {
        this.presupuesto = presupuesto;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public UUID getPresupuestoId() {
        return presupuesto != null ? presupuesto.getId() : null;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public void setTipo(TipoRecurso tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMontoPresupuestado() {
        return montoPresupuestado;
    }

    public void setMontoPresupuestado(BigDecimal montoPresupuestado) {
        this.montoPresupuestado = montoPresupuestado;
    }

    public BigDecimal getMontoReservado() {
        return montoReservado;
    }

    public void setMontoReservado(BigDecimal montoReservado) {
        this.montoReservado = montoReservado;
    }

    public BigDecimal getMontoEjecutado() {
        return montoEjecutado;
    }

    public void setMontoEjecutado(BigDecimal montoEjecutado) {
        this.montoEjecutado = montoEjecutado;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
