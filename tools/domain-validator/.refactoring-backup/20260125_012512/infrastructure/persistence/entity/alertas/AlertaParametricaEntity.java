package com.budgetpro.infrastructure.persistence.entity.alertas;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla alerta_parametrica.
 */
@Entity
@Table(name = "alerta_parametrica",
       indexes = {
           @Index(name = "idx_alerta_analisis", columnList = "analisis_id"),
           @Index(name = "idx_alerta_tipo", columnList = "tipo_alerta"),
           @Index(name = "idx_alerta_nivel", columnList = "nivel"),
           @Index(name = "idx_alerta_partida", columnList = "partida_id"),
           @Index(name = "idx_alerta_recurso", columnList = "recurso_id")
       })
public class AlertaParametricaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisis_id", nullable = false, updatable = false)
    private AnalisisPresupuestoEntity analisis;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, columnDefinition = "tipo_alerta_parametrica")
    private com.budgetpro.domain.finanzas.alertas.model.TipoAlertaParametrica tipoAlerta;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, columnDefinition = "nivel_alerta")
    private com.budgetpro.domain.finanzas.alertas.model.NivelAlerta nivel;

    @Column(name = "partida_id")
    private UUID partidaId;

    @Column(name = "recurso_id")
    private UUID recursoId;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "valor_detectado", precision = 19, scale = 6)
    private BigDecimal valorDetectado;

    @Column(name = "valor_esperado_min", precision = 19, scale = 6)
    private BigDecimal valorEsperadoMin;

    @Column(name = "valor_esperado_max", precision = 19, scale = 6)
    private BigDecimal valorEsperadoMax;

    @Column(name = "sugerencia", columnDefinition = "TEXT")
    private String sugerencia;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AlertaParametricaEntity() {
    }

    public AlertaParametricaEntity(UUID id, AnalisisPresupuestoEntity analisis,
                                 com.budgetpro.domain.finanzas.alertas.model.TipoAlertaParametrica tipoAlerta,
                                 com.budgetpro.domain.finanzas.alertas.model.NivelAlerta nivel,
                                 UUID partidaId, UUID recursoId, String mensaje,
                                 BigDecimal valorDetectado, BigDecimal valorEsperadoMin,
                                 BigDecimal valorEsperadoMax, String sugerencia, Integer version) {
        this.id = id;
        this.analisis = analisis;
        this.tipoAlerta = tipoAlerta;
        this.nivel = nivel;
        this.partidaId = partidaId;
        this.recursoId = recursoId;
        this.mensaje = mensaje;
        this.valorDetectado = valorDetectado;
        this.valorEsperadoMin = valorEsperadoMin;
        this.valorEsperadoMax = valorEsperadoMax;
        this.sugerencia = sugerencia;
        this.version = version;
    }

    // Getters y Setters (simplificados)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AnalisisPresupuestoEntity getAnalisis() { return analisis; }
    public void setAnalisis(AnalisisPresupuestoEntity analisis) { this.analisis = analisis; }
    public com.budgetpro.domain.finanzas.alertas.model.TipoAlertaParametrica getTipoAlerta() { return tipoAlerta; }
    public void setTipoAlerta(com.budgetpro.domain.finanzas.alertas.model.TipoAlertaParametrica tipoAlerta) { this.tipoAlerta = tipoAlerta; }
    public com.budgetpro.domain.finanzas.alertas.model.NivelAlerta getNivel() { return nivel; }
    public void setNivel(com.budgetpro.domain.finanzas.alertas.model.NivelAlerta nivel) { this.nivel = nivel; }
    public UUID getPartidaId() { return partidaId; }
    public void setPartidaId(UUID partidaId) { this.partidaId = partidaId; }
    public UUID getRecursoId() { return recursoId; }
    public void setRecursoId(UUID recursoId) { this.recursoId = recursoId; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public BigDecimal getValorDetectado() { return valorDetectado; }
    public void setValorDetectado(BigDecimal valorDetectado) { this.valorDetectado = valorDetectado; }
    public BigDecimal getValorEsperadoMin() { return valorEsperadoMin; }
    public void setValorEsperadoMin(BigDecimal valorEsperadoMin) { this.valorEsperadoMin = valorEsperadoMin; }
    public BigDecimal getValorEsperadoMax() { return valorEsperadoMax; }
    public void setValorEsperadoMax(BigDecimal valorEsperadoMax) { this.valorEsperadoMax = valorEsperadoMax; }
    public String getSugerencia() { return sugerencia; }
    public void setSugerencia(String sugerencia) { this.sugerencia = sugerencia; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
