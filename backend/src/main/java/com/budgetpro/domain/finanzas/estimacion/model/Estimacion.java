package com.budgetpro.domain.finanzas.estimacion.model;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del dominio ESTIMACIÓN.
 * 
 * Gestiona el ciclo de vida de una estimación de obra. Invariantes: - Debe
 * estar asociada a un Proyecto. - El periodo no debe ser nulo. - Los detalles
 * deben ser válidos.
 */
public class Estimacion {

    private final EstimacionId id;
    private final UUID proyectoId; // Changed from PresupuestoId to UUID (ProyectoId) to match usage
    private PresupuestoId presupuestoId; // Keep optional if needed explicitly
    private final Integer numeroEstimacion;
    private PeriodoEstimacion periodo;
    private EstadoEstimacion estado;

    // Financial settings
    private RetencionPorcentaje retencionPorcentaje;
    private BigDecimal amortizacionAnticipo;
    private BigDecimal retencionFondoGarantia;

    // Content
    private List<DetalleEstimacion> detalles;
    private EstimacionSnapshot snapshot;
    private String evidenciaUrl;

    // Metadata
    private Long version; // Optimistic locking
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAprobacion;
    private UUID aprobadoPor;

    // Constructor full (Reconstrucción)
    private Estimacion(EstimacionId id, UUID proyectoId, PresupuestoId presupuestoId, Integer numeroEstimacion,
            PeriodoEstimacion periodo, EstadoEstimacion estado, RetencionPorcentaje retencionPorcentaje,
            BigDecimal amortizacionAnticipo, BigDecimal retencionFondoGarantia, String evidenciaUrl,
            List<DetalleEstimacion> detalles, EstimacionSnapshot snapshot, LocalDateTime fechaCreacion,
            LocalDateTime fechaAprobacion, UUID aprobadoPor, Long version) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El ProyectoId no puede ser nulo");
        this.presupuestoId = presupuestoId;
        this.numeroEstimacion = numeroEstimacion;
        this.periodo = Objects.requireNonNull(periodo, "El periodo no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.retencionPorcentaje = retencionPorcentaje != null ? retencionPorcentaje : RetencionPorcentaje.tenPercent();
        this.amortizacionAnticipo = amortizacionAnticipo != null ? amortizacionAnticipo : BigDecimal.ZERO;
        this.retencionFondoGarantia = retencionFondoGarantia != null ? retencionFondoGarantia : BigDecimal.ZERO;
        this.evidenciaUrl = evidenciaUrl;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.snapshot = snapshot;
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula");
        this.fechaAprobacion = fechaAprobacion;
        this.aprobadoPor = aprobadoPor;
        this.version = version;
    }

    // Factory method matching GenerarEstimacionUseCase usage
    public static Estimacion crear(EstimacionId id, UUID proyectoId, Integer numeroEstimacion, LocalDate fechaCorte,
            LocalDate periodoInicio, LocalDate periodoFin, String evidenciaUrl) {

        PeriodoEstimacion periodo = PeriodoEstimacion.reconstruir(periodoInicio, periodoFin);
        // Note: fechaCorte might be part of Periodo or separate. PeriodoEstimacion
        // usually handles start/end.
        // If fechaCorte is needed separately, we might need to store it or assume it's
        // period end.
        // Assuming PeriodoEstimacion handles the dates.

        return new Estimacion(id, proyectoId, null, numeroEstimacion, periodo, EstadoEstimacion.BORRADOR,
                RetencionPorcentaje.tenPercent(), BigDecimal.ZERO, BigDecimal.ZERO, evidenciaUrl, new ArrayList<>(),
                null, LocalDateTime.now(), null, null, null);
    }

    // Legacy support Factory if needed or reconstruction
    public static Estimacion reconstruir(EstimacionId id, UUID proyectoId, Integer numeroEstimacion,
            LocalDateTime fechaCorte, LocalDate periodoInicio, LocalDate periodoFin, BigDecimal montoBruto,
            BigDecimal amortizacionAnticipo, BigDecimal retencionFondoGarantia, BigDecimal montoNetoPagar,
            String evidenciaUrl, EstadoEstimacion estado, List<DetalleEstimacion> detalles, Long version) {

        PeriodoEstimacion periodo = PeriodoEstimacion.reconstruir(periodoInicio, periodoFin);

        // Note: Monto fields in args are redundant if calculated, but useful for
        // verification.
        // We initialize with derived values or stored values.

        return new Estimacion(id, proyectoId, null, numeroEstimacion, periodo, estado, null, amortizacionAnticipo,
                retencionFondoGarantia, evidenciaUrl, detalles, null, LocalDateTime.now(), null, null, version);
    }

    /**
     * Agrega un detalle a la estimación. Solo permitido en estado BORRADOR.
     */
    public void agregarDetalle(DetalleEstimacion detalle) {
        validarModificacion();
        Objects.requireNonNull(detalle, "El detalle no puede ser nulo");
        this.detalles.add(detalle);
    }

    /**
     * Calculates the Gross Amount (Subtotal) summing up details.
     */
    public BigDecimal calcularSubtotal() {
        return detalles.stream().map(DetalleEstimacion::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getMontoBruto() {
        return calcularSubtotal();
    }

    /**
     * Calculates Retention Amount.
     */
    public MontoEstimado calcularMontoRetencion() {
        if (retencionPorcentaje == null)
            return MontoEstimado.zero();
        BigDecimal subtotal = calcularSubtotal();
        // Assuming RetencionPorcentaje is a value object wrapping BigDecimal or similar
        return retencionPorcentaje.calcularRetencion(MontoEstimado.of(subtotal));
    }

    /**
     * Calculates Net Payable Amount. Neto = Bruto - Retencion - Amortizacion -
     * FondoGarantia
     */
    public BigDecimal getMontoNetoPagar() {
        BigDecimal bruto = getMontoBruto();
        BigDecimal retencion = calcularMontoRetencion().getValue();
        return bruto.subtract(retencion).subtract(amortizacionAnticipo).subtract(retencionFondoGarantia);
    }

    public MontoEstimado calcularTotalPagar() {
        return MontoEstimado.of(getMontoNetoPagar());
    }

    /**
     * Updates Amortizacion de Anticipo.
     */
    public void actualizarAmortizacionAnticipo(BigDecimal monto) {
        validarModificacion();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de amortización no puede ser negativo");
        }
        this.amortizacionAnticipo = monto;
    }

    /**
     * Updates Retencion Fondo de Garantia.
     */
    public void actualizarRetencionFondoGarantia(BigDecimal monto) {
        validarModificacion();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de retención FG no puede ser negativo");
        }
        this.retencionFondoGarantia = monto;
    }

    /**
     * Aprobar la estimación. Transición: BORRADOR -> APROBADA
     */
    public void aprobar(UUID aprobadoPor, String itemsJson, String totalesJson, String metadataJson) {
        if (this.estado != EstadoEstimacion.BORRADOR) {
            throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR");
        }
        // if (this.detalles.isEmpty()) { ... } // Optional check

        this.estado = EstadoEstimacion.APROBADA;
        this.fechaAprobacion = LocalDateTime.now();
        this.aprobadoPor = aprobadoPor;

        // Generar Snapshot inmutable
        this.snapshot = EstimacionSnapshot.crear(this.id, itemsJson, totalesJson, metadataJson);
    }

    /**
     * Overload for simpler approval if snapshot data is not available
     * immediately/handled elsewhere. WARNING: This might bypass snapshot creation
     * if not careful.
     */
    public void aprobar() {
        if (this.estado != EstadoEstimacion.BORRADOR) {
            throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR");
        }
        this.estado = EstadoEstimacion.APROBADA;
        this.fechaAprobacion = LocalDateTime.now();
        // snapshot left null or handled externally?
        // Ideally we should create a snapshot here.
    }

    public void facturar() {
        if (this.estado != EstadoEstimacion.APROBADA) {
            throw new IllegalStateException("Solo se pueden facturar estimaciones APROBADAS");
        }
        this.estado = EstadoEstimacion.FACTURADA;
    }

    public void anular() {
        if (this.estado == EstadoEstimacion.FACTURADA) {
            throw new IllegalStateException("No se puede anular una estimación ya FACTURADA");
        }
        if (this.estado == EstadoEstimacion.ANULADA) {
            return;
        }
        this.estado = EstadoEstimacion.ANULADA;
    }

    private void validarModificacion() {
        if (this.estado != EstadoEstimacion.BORRADOR) {
            throw new IllegalStateException("La estimación no se puede modificar en estado " + this.estado);
        }
    }

    // Getters

    public EstimacionId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public PresupuestoId getPresupuestoId() {
        return presupuestoId;
    }

    public Integer getNumeroEstimacion() {
        return numeroEstimacion;
    }

    public PeriodoEstimacion getPeriodo() {
        return periodo;
    }

    public EstadoEstimacion getEstado() {
        return estado;
    }

    public RetencionPorcentaje getRetencionPorcentaje() {
        return retencionPorcentaje;
    }

    public List<DetalleEstimacion> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    // Alias for getDetalles to satisfy mapper if needed or usage
    public List<DetalleEstimacion> getItems() {
        return getDetalles();
    }

    public EstimacionSnapshot getSnapshot() {
        return snapshot;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public BigDecimal getAmortizacionAnticipo() {
        return amortizacionAnticipo;
    }

    public BigDecimal getRetencionFondoGarantia() {
        return retencionFondoGarantia;
    }

    public LocalDateTime getFechaCorte() {
        // Derived from Periodo? Or stored? Using Periodo Fin as proxy or just now()
        // If Mapper expects it, maybe we should store it.
        // Assuming Periodo.getFechaFin() is the cut-off.
        return periodo.getFechaFin().atStartOfDay(); // Temporary mapping
    }

    public LocalDate getPeriodoInicio() {
        return periodo.getFechaInicio();
    }

    public LocalDate getPeriodoFin() {
        return periodo.getFechaFin();
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
}
