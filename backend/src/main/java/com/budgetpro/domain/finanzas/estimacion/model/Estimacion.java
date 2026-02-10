package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raíz que representa una Estimación de Avance (Cobro al Cliente).
 * 
 * Relación N:1 con Proyecto.
 * 
 * Responsabilidad: - Representar un corte de estimación para cobro - Calcular
 * montos (bruto, amortización, retención, neto) - Gestionar el estado de la
 * estimación (BORRADOR, APROBADA, PAGADA)
 * 
 * Invariantes: - El proyectoId es obligatorio - El numeroEstimacion debe ser
 * único por proyecto - El montoNetoPagar = montoBruto - amortizacionAnticipo -
 * retencionFondoGarantia - El estado solo puede cambiar: BORRADOR -> APROBADA
 * -> PAGADA
 */
public final class Estimacion {

    private final EstimacionId id;
    private final UUID proyectoId;
    private Integer numeroEstimacion; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                      // Identity field
    private LocalDate fechaCorte; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                  // Business data
    private LocalDate periodoInicio; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                     // Business data
    private LocalDate periodoFin; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                  // Business data
    private BigDecimal montoBruto; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                   // Calculated field
    private BigDecimal amortizacionAnticipo; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion
                                             // - Managed financial state
    private BigDecimal retencionFondoGarantia; // nosemgrep:
                                               // budgetpro.domain.immutability.entity-final-fields.estimacion - Managed
                                               // financial state
    private BigDecimal montoNetoPagar; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                       // Calculated field
    private String evidenciaUrl; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                 // Updatable evidence
    private EstadoEstimacion estado; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                                     // State machine
    private List<DetalleEstimacion> detalles; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion
                                              // - Child entities managed list
    private Long version; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.estimacion -
                          // Optimistic locking

    /**
     * Constructor privado. Usar factory methods.
     */
    private Estimacion(EstimacionId id, UUID proyectoId, Integer numeroEstimacion, LocalDate fechaCorte,
            LocalDate periodoInicio, LocalDate periodoFin, BigDecimal montoBruto, BigDecimal amortizacionAnticipo,
            BigDecimal retencionFondoGarantia, BigDecimal montoNetoPagar, String evidenciaUrl, EstadoEstimacion estado,
            List<DetalleEstimacion> detalles, Long version) {
        validarInvariantes(proyectoId, numeroEstimacion, periodoInicio, periodoFin);

        this.id = Objects.requireNonNull(id, "El ID de la estimación no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.numeroEstimacion = numeroEstimacion;
        this.fechaCorte = fechaCorte;
        this.periodoInicio = periodoInicio;
        this.periodoFin = periodoFin;
        this.montoBruto = montoBruto != null ? montoBruto : BigDecimal.ZERO;
        this.amortizacionAnticipo = amortizacionAnticipo != null ? amortizacionAnticipo : BigDecimal.ZERO;
        this.retencionFondoGarantia = retencionFondoGarantia != null ? retencionFondoGarantia : BigDecimal.ZERO;
        this.montoNetoPagar = montoNetoPagar != null ? montoNetoPagar : calcularMontoNeto();
        this.evidenciaUrl = evidenciaUrl;
        this.estado = estado != null ? estado : EstadoEstimacion.BORRADOR;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva Estimacion.
     */
    public static Estimacion crear(EstimacionId id, UUID proyectoId, Integer numeroEstimacion, LocalDate fechaCorte,
            LocalDate periodoInicio, LocalDate periodoFin, String evidenciaUrl) {
        return new Estimacion(id, proyectoId, numeroEstimacion, fechaCorte, periodoInicio, periodoFin, null, null, null,
                null, evidenciaUrl, EstadoEstimacion.BORRADOR, null, 0L);
    }

    /**
     * Factory method para reconstruir una Estimacion desde persistencia.
     */
    public static Estimacion reconstruir(EstimacionId id, UUID proyectoId, Integer numeroEstimacion,
            LocalDate fechaCorte, LocalDate periodoInicio, LocalDate periodoFin, BigDecimal montoBruto,
            BigDecimal amortizacionAnticipo, BigDecimal retencionFondoGarantia, BigDecimal montoNetoPagar,
            String evidenciaUrl, EstadoEstimacion estado, List<DetalleEstimacion> detalles, Long version) {
        return new Estimacion(id, proyectoId, numeroEstimacion, fechaCorte, periodoInicio, periodoFin, montoBruto,
                amortizacionAnticipo, retencionFondoGarantia, montoNetoPagar, evidenciaUrl, estado, detalles, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, Integer numeroEstimacion, LocalDate periodoInicio,
            LocalDate periodoFin) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (numeroEstimacion != null && numeroEstimacion <= 0) {
            // REGLA-012
            throw new IllegalArgumentException("El número de estimación debe ser positivo");
        }
        if (periodoInicio != null && periodoFin != null) {
            if (periodoFin.isBefore(periodoInicio)) {
                // REGLA-013
                throw new IllegalArgumentException("El periodo de fin no puede ser menor al periodo de inicio");
            }
        }
    }

    /**
     * Calcula el monto neto a pagar: montoBruto - amortizacionAnticipo -
     * retencionFondoGarantia.
     */
    private BigDecimal calcularMontoNeto() {
        // REGLA-011
        return this.montoBruto.subtract(this.amortizacionAnticipo).subtract(this.retencionFondoGarantia).setScale(4,
                java.math.RoundingMode.HALF_UP);
    }

    /**
     * Recalcula el monto bruto basándose en los detalles.
     */
    public void recalcularMontoBruto() {
        this.montoBruto = detalles.stream().map(DetalleEstimacion::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, java.math.RoundingMode.HALF_UP);
        this.montoNetoPagar = calcularMontoNeto();
    }

    /**
     * Agrega un detalle de estimación.
     */
    public void agregarDetalle(DetalleEstimacion detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        this.detalles.add(detalle);
        recalcularMontoBruto();
    }

    /**
     * Actualiza la amortización de anticipo.
     */
    public void actualizarAmortizacionAnticipo(BigDecimal nuevaAmortizacion) {
        if (nuevaAmortizacion == null) {
            this.amortizacionAnticipo = BigDecimal.ZERO;
        } else if (nuevaAmortizacion.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La amortización de anticipo no puede ser negativa");
        } else {
            this.amortizacionAnticipo = nuevaAmortizacion;
        }
        this.montoNetoPagar = calcularMontoNeto();
    }

    /**
     * Actualiza la retención de fondo de garantía.
     */
    public void actualizarRetencionFondoGarantia(BigDecimal nuevaRetencion) {
        if (nuevaRetencion == null) {
            this.retencionFondoGarantia = BigDecimal.ZERO;
        } else if (nuevaRetencion.compareTo(BigDecimal.ZERO) < 0) {
            // REGLA-014
            throw new IllegalArgumentException("La retención de fondo de garantía no puede ser negativa");
        } else {
            this.retencionFondoGarantia = nuevaRetencion;
        }
        this.montoNetoPagar = calcularMontoNeto();
    }

    /**
     * Actualiza la evidencia contractual.
     */
    public void actualizarEvidenciaUrl(String evidenciaUrl) {
        this.evidenciaUrl = evidenciaUrl;
    }

    /**
     * Aprueba la estimación (cambia estado a APROBADA).
     */
    public void aprobar() {
        if (this.estado != EstadoEstimacion.BORRADOR) {
            throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR");
        }
        this.estado = EstadoEstimacion.APROBADA;
    }

    /**
     * Marca la estimación como pagada (cambia estado a PAGADA).
     */
    public void marcarComoPagada() {
        if (this.estado != EstadoEstimacion.APROBADA) {
            // REGLA-010
            throw new IllegalStateException("Solo se pueden marcar como pagadas estimaciones en estado APROBADA");
        }
        this.estado = EstadoEstimacion.PAGADA;
    }

    // Getters

    public EstimacionId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public Integer getNumeroEstimacion() {
        return numeroEstimacion;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFin() {
        return periodoFin;
    }

    public BigDecimal getMontoBruto() {
        return montoBruto;
    }

    public BigDecimal getAmortizacionAnticipo() {
        return amortizacionAnticipo;
    }

    public BigDecimal getRetencionFondoGarantia() {
        return retencionFondoGarantia;
    }

    public BigDecimal getMontoNetoPagar() {
        return montoNetoPagar;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public EstadoEstimacion getEstado() {
        return estado;
    }

    public List<DetalleEstimacion> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Estimacion that = (Estimacion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Estimacion{id=%s, proyectoId=%s, numeroEstimacion=%d, estado=%s, montoNetoPagar=%s}", id,
                proyectoId, numeroEstimacion, estado, montoNetoPagar);
    }
}
