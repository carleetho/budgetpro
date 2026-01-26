package com.budgetpro.domain.finanzas.reajuste.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raíz que representa una Estimación de Reajuste de Costos.
 * 
 * Relación N:1 con Proyecto y Presupuesto.
 * 
 * Responsabilidad:
 * - Calcular reajuste de costos mediante fórmula polinómica: Pr = Po × (I1 / Io)
 * - Gestionar el estado de la estimación (BORRADOR, APROBADA, APLICADA)
 * - Calcular diferencial a cobrar
 * 
 * Invariantes:
 * - El proyectoId y presupuestoId son obligatorios
 * - El numeroEstimacion debe ser único por proyecto
 * - El montoReajustado = montoBase × (indiceActual / indiceBase)
 * - El diferencial = montoReajustado - montoBase
 */
public final class EstimacionReajuste {

    private final EstimacionReajusteId id;
    private final UUID proyectoId;
    private final UUID presupuestoId;
    private Integer numeroEstimacion;
    private LocalDate fechaCorte;
    private String indiceBaseCodigo;
    private LocalDate indiceBaseFecha;
    private String indiceActualCodigo;
    private LocalDate indiceActualFecha;
    private BigDecimal valorIndiceBase;
    private BigDecimal valorIndiceActual;
    private BigDecimal montoBase;
    private BigDecimal montoReajustado;
    private BigDecimal diferencial;
    private BigDecimal porcentajeVariacion;
    private EstadoEstimacionReajuste estado;
    private String observaciones;
    private List<DetalleReajustePartida> detalles;
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private EstimacionReajuste(EstimacionReajusteId id, UUID proyectoId, UUID presupuestoId,
                               Integer numeroEstimacion, LocalDate fechaCorte,
                               String indiceBaseCodigo, LocalDate indiceBaseFecha,
                               String indiceActualCodigo, LocalDate indiceActualFecha,
                               BigDecimal valorIndiceBase, BigDecimal valorIndiceActual,
                               BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial,
                               BigDecimal porcentajeVariacion, EstadoEstimacionReajuste estado,
                               String observaciones, List<DetalleReajustePartida> detalles, Long version) {
        validarInvariantes(proyectoId, presupuestoId, numeroEstimacion, valorIndiceBase, valorIndiceActual);
        
        this.id = Objects.requireNonNull(id, "El ID de la estimación no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.numeroEstimacion = numeroEstimacion;
        this.fechaCorte = fechaCorte;
        this.indiceBaseCodigo = indiceBaseCodigo;
        this.indiceBaseFecha = indiceBaseFecha;
        this.indiceActualCodigo = indiceActualCodigo;
        this.indiceActualFecha = indiceActualFecha;
        this.valorIndiceBase = valorIndiceBase;
        this.valorIndiceActual = valorIndiceActual;
        this.montoBase = montoBase != null ? montoBase : BigDecimal.ZERO;
        this.montoReajustado = montoReajustado != null ? montoReajustado : BigDecimal.ZERO;
        this.diferencial = diferencial != null ? diferencial : BigDecimal.ZERO;
        this.porcentajeVariacion = porcentajeVariacion != null ? porcentajeVariacion : BigDecimal.ZERO;
        this.estado = estado != null ? estado : EstadoEstimacionReajuste.BORRADOR;
        this.observaciones = observaciones;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva EstimacionReajuste.
     */
    public static EstimacionReajuste crear(EstimacionReajusteId id, UUID proyectoId, UUID presupuestoId,
                                           Integer numeroEstimacion, LocalDate fechaCorte,
                                           String indiceBaseCodigo, LocalDate indiceBaseFecha,
                                           String indiceActualCodigo, LocalDate indiceActualFecha,
                                           BigDecimal valorIndiceBase, BigDecimal valorIndiceActual,
                                           BigDecimal montoBase) {
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte,
                                    indiceBaseCodigo, indiceBaseFecha, indiceActualCodigo, indiceActualFecha,
                                    valorIndiceBase, valorIndiceActual, montoBase, null, null, null,
                                    EstadoEstimacionReajuste.BORRADOR, null, null, 0L);
    }

    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static EstimacionReajuste reconstruir(EstimacionReajusteId id, UUID proyectoId, UUID presupuestoId,
                                                Integer numeroEstimacion, LocalDate fechaCorte,
                                                String indiceBaseCodigo, LocalDate indiceBaseFecha,
                                                String indiceActualCodigo, LocalDate indiceActualFecha,
                                                BigDecimal valorIndiceBase, BigDecimal valorIndiceActual,
                                                BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial,
                                                BigDecimal porcentajeVariacion, EstadoEstimacionReajuste estado,
                                                String observaciones, List<DetalleReajustePartida> detalles, Long version) {
        return new EstimacionReajuste(id, proyectoId, presupuestoId, numeroEstimacion, fechaCorte,
                                    indiceBaseCodigo, indiceBaseFecha, indiceActualCodigo, indiceActualFecha,
                                    valorIndiceBase, valorIndiceActual, montoBase, montoReajustado, diferencial,
                                    porcentajeVariacion, estado, observaciones, detalles, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, UUID presupuestoId, Integer numeroEstimacion,
                                   BigDecimal valorIndiceBase, BigDecimal valorIndiceActual) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }
        if (numeroEstimacion != null && numeroEstimacion <= 0) {
            throw new IllegalArgumentException("El número de estimación debe ser positivo");
        }
        if (valorIndiceBase != null && valorIndiceBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del índice base debe ser mayor a cero");
        }
        if (valorIndiceActual != null && valorIndiceActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del índice actual debe ser mayor a cero");
        }
    }

    /**
     * Calcula el monto reajustado usando la fórmula polinómica.
     */
    public void calcularMontoReajustado(BigDecimal montoBaseTotal) {
        if (valorIndiceBase == null || valorIndiceActual == null) {
            throw new IllegalStateException("Los índices deben estar configurados para calcular el reajuste");
        }
        
        // Pr = Po × (I1 / Io)
        this.montoReajustado = montoBaseTotal
                .multiply(valorIndiceActual)
                .divide(valorIndiceBase, 4, java.math.RoundingMode.HALF_UP);
        
        // Diferencial = Pr - Po
        this.diferencial = montoReajustado.subtract(montoBaseTotal)
                .setScale(4, java.math.RoundingMode.HALF_UP);
        
        // % Variación = ((I1 / Io) - 1) × 100
        BigDecimal factor = valorIndiceActual.divide(valorIndiceBase, 4, java.math.RoundingMode.HALF_UP);
        this.porcentajeVariacion = factor.subtract(BigDecimal.ONE)
                .multiply(new BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Agrega un detalle de reajuste por partida.
     */
    public void agregarDetalle(DetalleReajustePartida detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        this.detalles.add(detalle);
    }

    /**
     * Aprueba la estimación de reajuste.
     */
    public void aprobar() {
        if (this.estado != EstadoEstimacionReajuste.BORRADOR) {
            throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR");
        }
        this.estado = EstadoEstimacionReajuste.APROBADA;
    }

    /**
     * Aplica la estimación de reajuste al presupuesto.
     */
    public void aplicar() {
        if (this.estado != EstadoEstimacionReajuste.APROBADA) {
            throw new IllegalStateException("Solo se pueden aplicar estimaciones en estado APROBADA");
        }
        this.estado = EstadoEstimacionReajuste.APLICADA;
    }

    // Getters
    
    public EstimacionReajusteId getId() { return id; }
    public UUID getProyectoId() { return proyectoId; }
    public UUID getPresupuestoId() { return presupuestoId; }
    public Integer getNumeroEstimacion() { return numeroEstimacion; }
    public LocalDate getFechaCorte() { return fechaCorte; }
    public String getIndiceBaseCodigo() { return indiceBaseCodigo; }
    public LocalDate getIndiceBaseFecha() { return indiceBaseFecha; }
    public String getIndiceActualCodigo() { return indiceActualCodigo; }
    public LocalDate getIndiceActualFecha() { return indiceActualFecha; }
    public BigDecimal getValorIndiceBase() { return valorIndiceBase; }
    public BigDecimal getValorIndiceActual() { return valorIndiceActual; }
    public BigDecimal getMontoBase() { return montoBase; }
    public BigDecimal getMontoReajustado() { return montoReajustado; }
    public BigDecimal getDiferencial() { return diferencial; }
    public BigDecimal getPorcentajeVariacion() { return porcentajeVariacion; }
    public EstadoEstimacionReajuste getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public List<DetalleReajustePartida> getDetalles() { return Collections.unmodifiableList(detalles); }
    public Long getVersion() { return version; }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstimacionReajuste that = (EstimacionReajuste) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
