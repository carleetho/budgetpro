package com.budgetpro.domain.finanzas.ordencambio.model;

import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.domain.shared.model.TipoRecurso;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un recurso (material, mano de obra, equipo) necesario
 * para una Orden de Cambio. Puede ser un recurso del catálogo local (recursoId)
 * o externo (externalRecursoId).
 * 
 * Invariantes: - El ordenCambioId no puede ser nulo - Debe tener al menos
 * recursoId O externalRecursoId - Nombre no vacío - Tipo no nulo - Cantidad
 * positiva - Unidad no vacía
 */
public class OrdenCambioRecurso {

    private final UUID id;
    private final OrdenCambioId ordenCambioId;
    private final RecursoId recursoId; // Nullable
    private final String externalRecursoId; // Nullable
    private final String nombre;
    private final TipoRecurso tipo;
    private final BigDecimal cantidad;
    private final String unidad;

    private OrdenCambioRecurso(UUID id, OrdenCambioId ordenCambioId, RecursoId recursoId, String externalRecursoId,
            String nombre, TipoRecurso tipo, BigDecimal cantidad, String unidad) {

        validarInvariantes(ordenCambioId, recursoId, externalRecursoId, nombre, tipo, cantidad, unidad);

        this.id = Objects.requireNonNull(id, "El ID del recurso no puede ser nulo");
        this.ordenCambioId = ordenCambioId;
        this.recursoId = recursoId;
        this.externalRecursoId = externalRecursoId;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.unidad = unidad.trim();
    }

    public static OrdenCambioRecurso crear(OrdenCambioId ordenCambioId, RecursoId recursoId, String externalRecursoId,
            String nombre, TipoRecurso tipo, BigDecimal cantidad, String unidad) {
        return new OrdenCambioRecurso(UUID.randomUUID(), ordenCambioId, recursoId, externalRecursoId, nombre, tipo,
                cantidad, unidad);
    }

    public static OrdenCambioRecurso reconstruir(UUID id, OrdenCambioId ordenCambioId, RecursoId recursoId,
            String externalRecursoId, String nombre, TipoRecurso tipo, BigDecimal cantidad, String unidad) {
        return new OrdenCambioRecurso(id, ordenCambioId, recursoId, externalRecursoId, nombre, tipo, cantidad, unidad);
    }

    private void validarInvariantes(OrdenCambioId ordenCambioId, RecursoId recursoId, String externalRecursoId,
            String nombre, TipoRecurso tipo, BigDecimal cantidad, String unidad) {
        if (ordenCambioId == null)
            throw new IllegalArgumentException("El ordenCambioId no puede ser nulo");
        if (recursoId == null && (externalRecursoId == null || externalRecursoId.isBlank())) {
            throw new IllegalArgumentException("Debe especificarse recursoId o externalRecursoId");
        }
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del recurso no puede estar vacío");
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de recurso no puede ser nulo");
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        if (unidad == null || unidad.isBlank())
            throw new IllegalArgumentException("La unidad no puede estar vacía");
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public OrdenCambioId getOrdenCambioId() {
        return ordenCambioId;
    }

    public RecursoId getRecursoId() {
        return recursoId;
    }

    public String getExternalRecursoId() {
        return externalRecursoId;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrdenCambioRecurso that = (OrdenCambioRecurso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
