package com.budgetpro.domain.finanzas.alertas.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa una alerta paramétrica generada por el análisis.
 * 
 * No es un agregado, es un resultado de análisis que se persiste para auditoría.
 */
public final class AlertaParametrica {
    
    private final UUID id;
    private final TipoAlertaParametrica tipoAlerta;
    private final NivelAlerta nivel;
    private final UUID partidaId; // Opcional
    private final UUID recursoId; // Opcional
    private final String mensaje;
    private final BigDecimal valorDetectado;
    private final BigDecimal valorEsperadoMin; // Opcional
    private final BigDecimal valorEsperadoMax; // Opcional
    private final String sugerencia; // Opcional
    
    /**
     * Constructor privado. Usar factory method.
     */
    private AlertaParametrica(UUID id, TipoAlertaParametrica tipoAlerta, NivelAlerta nivel,
                             UUID partidaId, UUID recursoId, String mensaje,
                             BigDecimal valorDetectado, BigDecimal valorEsperadoMin,
                             BigDecimal valorEsperadoMax, String sugerencia) {
        this.id = Objects.requireNonNull(id, "El ID de la alerta no puede ser nulo");
        this.tipoAlerta = Objects.requireNonNull(tipoAlerta, "El tipo de alerta no puede ser nulo");
        this.nivel = Objects.requireNonNull(nivel, "El nivel de alerta no puede ser nulo");
        this.partidaId = partidaId;
        this.recursoId = recursoId;
        this.mensaje = Objects.requireNonNull(mensaje, "El mensaje no puede ser nulo");
        this.valorDetectado = valorDetectado;
        this.valorEsperadoMin = valorEsperadoMin;
        this.valorEsperadoMax = valorEsperadoMax;
        this.sugerencia = sugerencia;
    }
    
    /**
     * Factory method para crear una alerta paramétrica.
     */
    public static AlertaParametrica crear(TipoAlertaParametrica tipoAlerta, NivelAlerta nivel,
                                         UUID partidaId, UUID recursoId, String mensaje,
                                         BigDecimal valorDetectado, BigDecimal valorEsperadoMin,
                                         BigDecimal valorEsperadoMax, String sugerencia) {
        return new AlertaParametrica(
            UUID.randomUUID(),
            tipoAlerta,
            nivel,
            partidaId,
            recursoId,
            mensaje,
            valorDetectado,
            valorEsperadoMin,
            valorEsperadoMax,
            sugerencia
        );
    }
    
    /**
     * Factory method simplificado para alertas sin valores esperados.
     */
    public static AlertaParametrica crear(TipoAlertaParametrica tipoAlerta, NivelAlerta nivel,
                                         UUID partidaId, UUID recursoId, String mensaje,
                                         BigDecimal valorDetectado, String sugerencia) {
        return crear(tipoAlerta, nivel, partidaId, recursoId, mensaje,
                    valorDetectado, null, null, sugerencia);
    }
    
    // Getters
    
    public UUID getId() {
        return id;
    }
    
    public TipoAlertaParametrica getTipoAlerta() {
        return tipoAlerta;
    }
    
    public NivelAlerta getNivel() {
        return nivel;
    }
    
    public UUID getPartidaId() {
        return partidaId;
    }
    
    public UUID getRecursoId() {
        return recursoId;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public BigDecimal getValorDetectado() {
        return valorDetectado;
    }
    
    public BigDecimal getValorEsperadoMin() {
        return valorEsperadoMin;
    }
    
    public BigDecimal getValorEsperadoMax() {
        return valorEsperadoMax;
    }
    
    public String getSugerencia() {
        return sugerencia;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertaParametrica that = (AlertaParametrica) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
