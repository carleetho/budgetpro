package com.budgetpro.domain.finanzas.recurso.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.budgetpro.domain.shared.model.TipoRecurso;

/**
 * Aggregate Root del agregado RECURSO. Representa un único concepto económico
 * global en todo BUDGETPRO.
 * 
 * Invariantes: - El nombre debe estar normalizado y no puede estar vacío - El
 * tipo no puede ser nulo - La unidadBase no puede estar vacía
 */
public final class Recurso {

    private final RecursoId id;
    private final String nombre;
    private final TipoRecurso tipo;
    private final String unidadBase;
    private final Map<String, Object> atributos;
    private final EstadoRecurso estado;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Recurso(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase, Map<String, Object> atributos,
            EstadoRecurso estado) {
        validarInvariantes(nombre, tipo, unidadBase);

        this.id = Objects.requireNonNull(id, "El ID del recurso no puede ser nulo");
        this.nombre = normalizarNombre(nombre);
        this.tipo = tipo;
        this.unidadBase = unidadBase;
        this.atributos = atributos != null ? Collections.unmodifiableMap(new HashMap<>(atributos))
                : Collections.emptyMap();
        this.estado = estado != null ? estado : EstadoRecurso.ACTIVO;
    }

    /**
     * Factory method para crear un nuevo Recurso.
     */
    public static Recurso crear(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase) {
        return new Recurso(id, nombre, tipo, unidadBase, null, EstadoRecurso.ACTIVO);
    }

    public static Recurso crear(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase,
            Map<String, Object> atributos) {
        return new Recurso(id, nombre, tipo, unidadBase, atributos, EstadoRecurso.ACTIVO);
    }

    public static Recurso crearProvisional(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase) {
        return new Recurso(id, nombre, tipo, unidadBase, null, EstadoRecurso.EN_REVISION);
    }

    public static Recurso crearProvisional(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase,
            Map<String, Object> atributos) {
        return new Recurso(id, nombre, tipo, unidadBase, atributos, EstadoRecurso.EN_REVISION);
    }

    /**
     * Factory method para reconstrucción desde persistencia.
     */
    public static Recurso reconstruir(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase,
            Map<String, Object> atributos, EstadoRecurso estado) {
        return new Recurso(id, nombre, tipo, unidadBase, atributos, estado);
    }

    private static String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del recurso no puede estar vacío");
        }
        return nombre.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    private void validarInvariantes(String nombre, TipoRecurso tipo, String unidadBase) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del recurso no puede estar vacío");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo del recurso no puede ser nulo");
        }
        if (unidadBase == null || unidadBase.isBlank()) {
            throw new IllegalArgumentException("La unidad base del recurso no puede estar vacía");
        }
    }

    /**
     * Actualiza el nombre del recurso.
     * 
     * @return Nuevo Recurso con el nombre actualizado.
     */
    public Recurso actualizarNombre(String nuevoNombre) {
        return new Recurso(this.id, nuevoNombre, this.tipo, this.unidadBase, this.atributos, this.estado);
    }

    /**
     * Actualiza la unidad base.
     * 
     * @return Nuevo Recurso con la unidad base actualizada.
     */
    public Recurso actualizarUnidadBase(String nuevaUnidadBase) {
        return new Recurso(this.id, this.nombre, this.tipo, nuevaUnidadBase, this.atributos, this.estado);
    }

    /**
     * Agrega o actualiza un atributo.
     * 
     * @return Nuevo Recurso con el atributo agregado.
     */
    public Recurso agregarAtributo(String clave, Object valor) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave del atributo no puede estar vacía");
        }
        Map<String, Object> nuevosAtributos = new HashMap<>(this.atributos);
        nuevosAtributos.put(clave, valor);
        return new Recurso(this.id, this.nombre, this.tipo, this.unidadBase, nuevosAtributos, this.estado);
    }

    /**
     * Elimina un atributo.
     * 
     * @return Nuevo Recurso sin el atributo.
     */
    public Recurso eliminarAtributo(String clave) {
        Map<String, Object> nuevosAtributos = new HashMap<>(this.atributos);
        nuevosAtributos.remove(clave);
        return new Recurso(this.id, this.nombre, this.tipo, this.unidadBase, nuevosAtributos, this.estado);
    }

    /**
     * Actualiza todos los atributos.
     * 
     * @return Nuevo Recurso con los nuevos atributos.
     */
    public Recurso actualizarAtributos(Map<String, Object> nuevosAtributos) {
        return new Recurso(this.id, this.nombre, this.tipo, this.unidadBase, nuevosAtributos, this.estado);
    }

    public Recurso activar() {
        return new Recurso(this.id, this.nombre, this.tipo, this.unidadBase, this.atributos, EstadoRecurso.ACTIVO);
    }

    public Recurso desactivar() {
        return new Recurso(this.id, this.nombre, this.tipo, this.unidadBase, this.atributos, EstadoRecurso.DEPRECADO);
    }

    public Recurso marcarEnRevision() {
        return new Recurso(this.id, this.nombre, this.tipo, this.unidadBase, this.atributos, EstadoRecurso.EN_REVISION);
    }

    // Getters

    public RecursoId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public String getUnidadBase() {
        return unidadBase;
    }

    public Map<String, Object> getAtributos() {
        return atributos;
    }

    public EstadoRecurso getEstado() {
        return estado;
    }

    public boolean isActivo() {
        return estado == EstadoRecurso.ACTIVO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Recurso recurso = (Recurso) o;
        return Objects.equals(id, recurso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Recurso{id=%s, nombre='%s', tipo=%s, unidadBase='%s', estado=%s}", id, nombre, tipo,
                unidadBase, estado);
    }
}
