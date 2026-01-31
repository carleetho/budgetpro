package com.budgetpro.domain.finanzas.recurso.model;

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
 * 
 * Este es el núcleo del "Shared Kernel — Catálogo de Recursos".
 */
public final class Recurso {

    private final RecursoId id;
    private String nombre;
    private final TipoRecurso tipo;
    private String unidadBase;
    private Map<String, Object> atributos;
    private EstadoRecurso estado;

    /**
     * Constructor privado. Usar factory methods o builder.
     */
    private Recurso(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase, Map<String, Object> atributos,
            EstadoRecurso estado) {
        validarInvariantes(nombre, tipo, unidadBase);

        this.id = Objects.requireNonNull(id, "El ID del recurso no puede ser nulo");
        this.nombre = normalizarNombre(nombre);
        this.tipo = tipo;
        this.unidadBase = unidadBase;
        this.atributos = atributos != null ? new HashMap<>(atributos) : new HashMap<>();
        this.estado = estado != null ? estado : EstadoRecurso.ACTIVO;
    }

    /**
     * Factory method para crear un nuevo Recurso con estado ACTIVO por defecto.
     */
    public static Recurso crear(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase) {
        return new Recurso(id, nombre, tipo, unidadBase, null, EstadoRecurso.ACTIVO);
    }

    /**
     * Factory method para crear un Recurso con atributos adicionales y estado
     * ACTIVO por defecto.
     */
    public static Recurso crear(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase,
            Map<String, Object> atributos) {
        return new Recurso(id, nombre, tipo, unidadBase, atributos, EstadoRecurso.ACTIVO);
    }

    /**
     * Factory method para crear un Recurso provisional con estado EN_REVISION.
     * Usado en el Wireflow 1 cuando se requiere crear un recurso durante una compra
     * directa.
     */
    public static Recurso crearProvisional(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase) {
        return new Recurso(id, nombre, tipo, unidadBase, null, EstadoRecurso.EN_REVISION);
    }

    /**
     * Factory method para crear un Recurso provisional con atributos y estado
     * EN_REVISION.
     */
    public static Recurso crearProvisional(RecursoId id, String nombre, TipoRecurso tipo, String unidadBase,
            Map<String, Object> atributos) {
        return new Recurso(id, nombre, tipo, unidadBase, atributos, EstadoRecurso.EN_REVISION);
    }

    /**
     * Normaliza el nombre del recurso según la regla de negocio: Trim + UpperCase +
     * reemplazar espacios múltiples por uno solo. Ejemplo: " cemento gris " ->
     * "CEMENTO GRIS"
     * 
     * @param nombre El nombre a normalizar
     * @return El nombre normalizado
     */
    private static String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del recurso no puede estar vacío");
        }
        return nombre.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /**
     * Valida las invariantes del agregado antes de crear o modificar.
     */
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
     * Actualiza el nombre del recurso aplicando normalización automática. El nombre
     * se normaliza según: Trim + UpperCase + reemplazar espacios múltiples por uno
     * solo.
     */
    public void actualizarNombre(String nuevoNombre) {
        this.nombre = normalizarNombre(nuevoNombre);
    }

    /**
     * Actualiza la unidad base del recurso.
     */
    public void actualizarUnidadBase(String nuevaUnidadBase) {
        if (nuevaUnidadBase == null || nuevaUnidadBase.isBlank()) {
            throw new IllegalArgumentException("La unidad base del recurso no puede estar vacía");
        }
        this.unidadBase = nuevaUnidadBase;
    }

    /**
     * Agrega o actualiza un atributo adicional del recurso.
     */
    public void agregarAtributo(String clave, Object valor) {
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("La clave del atributo no puede estar vacía");
        }
        if (atributos == null) {
            atributos = new HashMap<>();
        }
        atributos.put(clave, valor);
    }

    /**
     * Elimina un atributo del recurso.
     */
    public void eliminarAtributo(String clave) {
        if (atributos != null) {
            atributos.remove(clave);
        }
    }

    /**
     * Actualiza todos los atributos del recurso.
     */
    public void actualizarAtributos(Map<String, Object> nuevosAtributos) {
        this.atributos = nuevosAtributos != null ? new HashMap<>(nuevosAtributos) : new HashMap<>();
    }

    /**
     * Activa el recurso (cambia el estado a ACTIVO).
     */
    public void activar() {
        this.estado = EstadoRecurso.ACTIVO;
    }

    /**
     * Desactiva el recurso (cambia el estado a DEPRECADO).
     */
    public void desactivar() {
        this.estado = EstadoRecurso.DEPRECADO;
    }

    /**
     * Marca el recurso como en revisión.
     */
    public void marcarEnRevision() {
        this.estado = EstadoRecurso.EN_REVISION;
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
        return atributos != null ? Map.copyOf(atributos) : Map.of();
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
        return "Recurso{" + "id=" + id + ", nombre='" + nombre + '\'' + ", tipo=" + tipo + ", unidadBase='" + unidadBase
                + '\'' + ", estado=" + estado + '}';
    }
}
