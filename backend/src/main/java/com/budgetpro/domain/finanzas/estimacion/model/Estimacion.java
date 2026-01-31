package com.budgetpro.domain.finanzas.estimacion.model;

import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionItem;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionSnapshot;
import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.MontoEstimado;
import com.budgetpro.domain.finanzas.estimacion.model.PeriodoEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.RetencionPorcentaje;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
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
 * estar asociada a un Proyecto (Presupuesto). - El periodo no debe ser nulo. -
 * Los items deben ser válidos.
 */
public class Estimacion {

    private final EstimacionId id;
    private final PresupuestoId presupuestoId;
    private PeriodoEstimacion periodo;
    private EstadoEstimacion estado;
    private RetencionPorcentaje retencionPorcentaje;
    private List<EstimacionItem> items;
    private EstimacionSnapshot snapshot;

    // Metadata
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAprobacion;
    private UUID aprobadoPor;

    private Estimacion(EstimacionId id, PresupuestoId presupuestoId, PeriodoEstimacion periodo, EstadoEstimacion estado,
            RetencionPorcentaje retencionPorcentaje, List<EstimacionItem> items, EstimacionSnapshot snapshot,
            LocalDateTime fechaCreacion, LocalDateTime fechaAprobacion, UUID aprobadoPor) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El PresupuestoId no puede ser nulo");
        this.periodo = Objects.requireNonNull(periodo, "El periodo no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.retencionPorcentaje = Objects.requireNonNull(retencionPorcentaje, "La retención no puede ser nula");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "La lista de items no puede ser nula"));
        this.snapshot = snapshot; // Puede ser nulo si aun no se aprueba
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula");
        this.fechaAprobacion = fechaAprobacion;
        this.aprobadoPor = aprobadoPor;
    }

    public static Estimacion crear(PresupuestoId presupuestoId, PeriodoEstimacion periodo,
            RetencionPorcentaje retencion) {
        return new Estimacion(EstimacionId.random(), presupuestoId, periodo, EstadoEstimacion.BORRADOR,
                retencion != null ? retencion : RetencionPorcentaje.tenPercent(), new ArrayList<>(), null,
                LocalDateTime.now(), null, null);
    }

    public static Estimacion reconstruir(EstimacionId id, PresupuestoId presupuestoId, PeriodoEstimacion periodo,
            EstadoEstimacion estado, RetencionPorcentaje retencion, List<EstimacionItem> items,
            EstimacionSnapshot snapshot, LocalDateTime fechaCreacion, LocalDateTime fechaAprobacion, UUID aprobadoPor) {
        return new Estimacion(id, presupuestoId, periodo, estado, retencion, items, snapshot, fechaCreacion,
                fechaAprobacion, aprobadoPor);
    }

    /**
     * Agrega un item a la estimación. Solo permitido en estado BORRADOR.
     */
    public void agregarItem(EstimacionItem item) {
        validarModificacion();
        Objects.requireNonNull(item, "El item no puede ser nulo");
        this.items.add(item);
    }

    /**
     * Calcula el monto total de la estimación (suma de montos actuales de items).
     */
    public MontoEstimado calcularTotalEstimado() {
        return items.stream().map(EstimacionItem::getMontoActual).reduce(MontoEstimado.zero(), MontoEstimado::sumar);
    }

    /**
     * Calcula el monto de retención basado en el total estimado.
     */
    public MontoEstimado calcularMontoRetencion() {
        return retencionPorcentaje.calcularRetencion(calcularTotalEstimado());
    }

    /**
     * Alias para calcular total estimado para consistencia con UI/DTO.
     */
    public MontoEstimado calcularSubtotal() {
        return calcularTotalEstimado();
    }

    /**
     * Calcula el total a pagar (Estimado - Retención).
     */
    public MontoEstimado calcularTotalPagar() {
        return calcularTotalEstimado().restar(calcularMontoRetencion());
    }

    /**
     * Aprueba la estimación. Transición: BORRADOR -> APROBADA
     */
    public void aprobar(UUID aprobadoPor, String itemsJson, String totalesJson, String metadataJson) {
        if (this.estado != EstadoEstimacion.BORRADOR) {
            throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR");
        }
        if (this.items.isEmpty()) {
            throw new IllegalStateException("No se puede aprobar una estimación sin items");
        }
        Objects.requireNonNull(aprobadoPor, "El usuario aprobador es requerido");

        this.estado = EstadoEstimacion.APROBADA;
        this.fechaAprobacion = LocalDateTime.now();
        this.aprobadoPor = aprobadoPor;

        // Generar Snapshot inmutable
        this.snapshot = EstimacionSnapshot.crear(this.id, itemsJson, totalesJson, metadataJson);
    }

    /**
     * Marca la estimación como facturada. Transición: APROBADA -> FACTURADA
     */
    public void facturar() {
        if (this.estado != EstadoEstimacion.APROBADA) {
            throw new IllegalStateException("Solo se pueden facturar estimaciones APROBADAS");
        }
        this.estado = EstadoEstimacion.FACTURADA;
    }

    /**
     * Anula la estimación. Permitido desde BORRADOR o APROBADA (si no se ha
     * facturado aún).
     */
    public void anular() {
        if (this.estado == EstadoEstimacion.FACTURADA) {
            throw new IllegalStateException("No se puede anular una estimación ya FACTURADA");
        }
        if (this.estado == EstadoEstimacion.ANULADA) {
            return; // Ya anulada, idempotente
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

    public PresupuestoId getPresupuestoId() {
        return presupuestoId;
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

    public List<EstimacionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public EstimacionSnapshot getSnapshot() {
        return snapshot;
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

    // ========================================================================
    // MÉTODOS DE COMPATIBILIDAD LEGACY
    // ========================================================================
    // Origen: commit 5e085db (AUDIT 2026-01-18)
    // Razón: Restaurar funcionalidad perdida en revert masivo
    // Usados por: GenerarEstimacionUseCaseImpl, EVMCalculationServiceImpl
    // TODO: Migrar UseCases a API DDD pura y eliminar estos métodos
    // ========================================================================

    /**
     * @deprecated Usar {@link #getPresupuestoId()} y extraer UUID con .getValue()
     *             Compatibilidad con código legacy que espera UUID directo
     */
    @Deprecated
    public java.util.UUID getProyectoId() {
        return presupuestoId.getValue();
    }

    /**
     * @deprecated No existe concepto de número de estimación en modelo DDD actual
     *             Retorna 0 como placeholder. Implementar lógica de numeración si
     *             es necesario.
     */
    @Deprecated
    public Integer getNumeroEstimacion() {
        // TODO: Implementar lógica de numeración si es requerido
        return 0;
    }

    /**
     * @deprecated Usar {@link #getPeriodo()} y extraer fechaFin con .getFechaFin()
     *             En modelo legacy, fechaCorte era el fin del periodo
     */
    @Deprecated
    public java.time.LocalDate getFechaCorte() {
        return periodo.getFechaFin();
    }

    /**
     * @deprecated Usar {@link #getPeriodo()} y extraer fechaInicio con
     *             .getFechaInicio()
     */
    @Deprecated
    public java.time.LocalDate getPeriodoInicio() {
        return periodo.getFechaInicio();
    }

    /**
     * @deprecated Usar {@link #getPeriodo()} y extraer fechaFin con .getFechaFin()
     */
    @Deprecated
    public java.time.LocalDate getPeriodoFin() {
        return periodo.getFechaFin();
    }

    /**
     * @deprecated Usar {@link #calcularTotalEstimado()} que retorna MontoEstimado
     *             Calcula suma de montos actuales de items
     */
    @Deprecated
    public java.math.BigDecimal getMontoBruto() {
        return calcularTotalEstimado().getValue();
    }

    /**
     * @deprecated No existe concepto de amortización de anticipo en modelo DDD
     *             actual Retorna ZERO como placeholder. Implementar si es
     *             necesario.
     */
    @Deprecated
    public java.math.BigDecimal getAmortizacionAnticipo() {
        // TODO: Implementar lógica de amortización si es requerido
        return java.math.BigDecimal.ZERO;
    }

    /**
     * @deprecated Usar {@link #calcularMontoRetencion()} que retorna MontoEstimado
     */
    @Deprecated
    public java.math.BigDecimal getRetencionFondoGarantia() {
        return calcularMontoRetencion().getValue();
    }

    /**
     * @deprecated Usar {@link #calcularTotalPagar()} que retorna MontoEstimado
     */
    @Deprecated
    public java.math.BigDecimal getMontoNetoPagar() {
        return calcularTotalPagar().getValue();
    }

    /**
     * @deprecated No existe concepto de evidenciaUrl en modelo DDD actual Retorna
     *             null como placeholder. Implementar si es necesario.
     */
    @Deprecated
    public String getEvidenciaUrl() {
        // TODO: Implementar lógica de evidencia si es requerido
        return null;
    }

    /**
     * @deprecated Usar {@link #getItems()} que retorna List<EstimacionItem> NOTA:
     *             Retorna lista vacía porque DetalleEstimacion no existe en modelo
     *             actual Si es necesario, implementar conversión EstimacionItem ->
     *             DetalleEstimacion
     */
    @Deprecated
    public java.util.List<DetalleEstimacion> getDetalles() {
        // TODO: Implementar conversión si DetalleEstimacion es requerido
        // Por ahora retorna lista vacía para evitar NullPointerException
        return java.util.Collections.emptyList();
    }

    /**
     * @deprecated No existe concepto de version en modelo DDD actual Retorna 0L
     *             como placeholder. Implementar optimistic locking si es necesario.
     */
    @Deprecated
    public Long getVersion() {
        // TODO: Implementar versioning si es requerido
        return 0L;
    }

    /**
     * @deprecated Usar {@link #agregarItem(EstimacionItem)} NOTA: No hace nada
     *             porque DetalleEstimacion no existe en modelo actual
     */
    @Deprecated
    public void agregarDetalle(DetalleEstimacion detalle) {
        // TODO: Implementar conversión DetalleEstimacion -> EstimacionItem si es
        // necesario
        // Por ahora no hace nada para evitar errores
        throw new UnsupportedOperationException(
                "agregarDetalle() no soportado. Usar agregarItem(EstimacionItem) en su lugar.");
    }
}
