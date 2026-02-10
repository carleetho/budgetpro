package com.budgetpro.domain.logistica.compra.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregate Root del agregado COMPRA.
 * 
 * Representa una compra realizada en el proyecto.
 * 
 * Invariantes: - El proyectoId es obligatorio - La fecha no puede ser nula - El
 * proveedor no puede estar vacío - La lista de detalles no puede ser nula ni
 * vacía - El total = Σ subtotales de detalles
 * 
 * Contexto: Logística & Costos
 */
public final class Compra {

    private final CompraId id;
    private final UUID proyectoId;
    // JUSTIFICACIÓN ARQUITECTÓNICA: Aggregate Root con estado transaccional
    // mutable.
    // Campos que representan el estado evolutivo de la compra (antes de
    // aprobación):
    // - fecha: puede actualizarse (actualizarFecha)
    // - proveedor: puede cambiarse (actualizarProveedor)
    // - estado: workflow transitions (BORRADOR → APROBADA)
    // - total: recalculado dinámicamente cuando se agregan/modifican detalles
    // - version: optimistic locking
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private LocalDate fecha;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private String proveedor;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private EstadoCompra estado;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal total; // Calculado: Σ subtotales de detalles
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private Long version;

    // Lista de detalles (entidades internas del agregado)
    private final List<CompraDetalle> detalles;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Compra(CompraId id, UUID proyectoId, LocalDate fecha, String proveedor, EstadoCompra estado, Long version,
            List<CompraDetalle> detalles) {
        validarInvariantes(proyectoId, fecha, proveedor, detalles);

        this.id = Objects.requireNonNull(id, "El ID de la compra no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.proveedor = normalizarProveedor(proveedor);
        this.estado = estado != null ? estado : EstadoCompra.BORRADOR;
        this.version = version != null ? version : 0L;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.total = calcularTotal();
    }

    /**
     * Factory method para crear una nueva Compra en estado BORRADOR.
     */
    public static Compra crear(CompraId id, UUID proyectoId, LocalDate fecha, String proveedor,
            List<CompraDetalle> detalles) {
        return new Compra(id, proyectoId, fecha, proveedor, EstadoCompra.BORRADOR, 0L, detalles);
    }

    /**
     * Factory method para reconstruir una Compra desde persistencia.
     */
    public static Compra reconstruir(CompraId id, UUID proyectoId, LocalDate fecha, String proveedor,
            EstadoCompra estado, BigDecimal total, Long version, List<CompraDetalle> detalles) {
        Compra compra = new Compra(id, proyectoId, fecha, proveedor, estado, version, detalles);
        compra.total = total != null ? total : compra.calcularTotal();
        return compra;
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, LocalDate fecha, String proveedor, List<CompraDetalle> detalles) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (proveedor == null || proveedor.isBlank()) {
            throw new IllegalArgumentException("El proveedor no puede estar vacío");
        }
        if (detalles == null || detalles.isEmpty()) {
            // REGLA-031
            throw new IllegalArgumentException("La compra debe tener al menos un detalle");
        }
    }

    /**
     * Normaliza el proveedor (trim).
     */
    private String normalizarProveedor(String proveedor) {
        if (proveedor == null || proveedor.isBlank()) {
            throw new IllegalArgumentException("El proveedor no puede estar vacío");
        }
        return proveedor.trim();
    }

    /**
     * Calcula el total de la compra: Σ subtotales de detalles.
     */
    private BigDecimal calcularTotal() {
        return detalles.stream().map(CompraDetalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Agrega un detalle a la compra.
     */
    public void agregarDetalle(CompraDetalle detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo");
        }
        this.detalles.add(detalle);
        this.total = calcularTotal();
    }

    /**
     * Aprueba la compra (cambia el estado a APROBADA).
     */
    public void aprobar() {
        this.estado = EstadoCompra.APROBADA;
    }

    /**
     * Actualiza la fecha de la compra.
     */
    public void actualizarFecha(LocalDate nuevaFecha) {
        if (nuevaFecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        this.fecha = nuevaFecha;
    }

    /**
     * Actualiza el proveedor de la compra.
     */
    public void actualizarProveedor(String nuevoProveedor) {
        this.proveedor = normalizarProveedor(nuevoProveedor);
    }

    /**
     * Obtiene la lista de detalles (inmutable).
     */
    public List<CompraDetalle> getDetalles() {
        return List.copyOf(detalles);
    }

    // Getters

    public CompraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getProveedor() {
        return proveedor;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Verifica si la compra está aprobada.
     */
    public boolean isAprobada() {
        return estado == EstadoCompra.APROBADA;
    }

    /**
     * Verifica si la compra tiene detalles.
     */
    public boolean tieneDetalles() {
        return !detalles.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Compra compra = (Compra) o;
        return Objects.equals(id, compra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Compra{id=%s, proyectoId=%s, fecha=%s, proveedor='%s', estado=%s, total=%s, detalles=%d}",
                id, proyectoId, fecha, proveedor, estado, total, detalles.size());
    }
}
