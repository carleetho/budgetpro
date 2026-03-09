package com.budgetpro.domain.proyecto.model;

import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root del agregado PROYECTO.
 *
 * Representa un proyecto de construcción con su identidad, ubicación y estado.
 *
 * Invariantes: - El nombre no puede estar vacío - El nombre debe ser único
 * (validado a nivel de persistencia) - El estado no puede ser nulo
 *
 * Contexto: Gestión de Proyectos
 */
public final class Proyecto {

    private final ProyectoId id;
    private final String nombre;
    private final String ubicacion;
    private final EstadoProyecto estado;
    private final LocalDateTime fechaInicio;
    private final FrecuenciaControl frecuenciaControl;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Proyecto(ProyectoId id, String nombre, String ubicacion, EstadoProyecto estado,
                     LocalDateTime fechaInicio, FrecuenciaControl frecuenciaControl) {
        validarInvariantes(nombre, estado);

        this.id = Objects.requireNonNull(id, "El ID del proyecto no puede ser nulo");
        this.nombre = normalizarNombre(nombre);
        this.ubicacion = ubicacion != null ? ubicacion.trim() : null;
        // REGLA-043
        this.estado = Objects.requireNonNull(estado, "El estado del proyecto no puede ser nulo");
        this.fechaInicio = fechaInicio;
        this.frecuenciaControl = frecuenciaControl;
    }

    /**
     * Factory method para crear un nuevo Proyecto en estado BORRADOR.
     */
    public static Proyecto crear(ProyectoId id, String nombre, String ubicacion) {
        return new Proyecto(id, nombre, ubicacion, EstadoProyecto.BORRADOR, null, null);
    }

    /**
     * Factory method para reconstruir un Proyecto desde persistencia.
     */
    public static Proyecto reconstruir(ProyectoId id, String nombre, String ubicacion, EstadoProyecto estado) {
        return reconstruir(id, nombre, ubicacion, estado, null, null);
    }

    /**
     * Factory method para reconstruir un Proyecto desde persistencia con frecuencia de control.
     */
    public static Proyecto reconstruir(ProyectoId id, String nombre, String ubicacion, EstadoProyecto estado,
                                      LocalDateTime fechaInicio, FrecuenciaControl frecuenciaControl) {
        return new Proyecto(id, nombre, ubicacion, estado, fechaInicio, frecuenciaControl);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(String nombre, EstadoProyecto estado) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del proyecto no puede ser nulo");
        }
    }

    /**
     * Normaliza el nombre del proyecto (trim).
     */
    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío");
        }
        return nombre.trim();
    }

    /**
     * Actualiza el nombre del proyecto.
     *
     * @return Nueva instancia de Proyecto con el nombre actualizado.
     */
    public Proyecto actualizarNombre(String nuevoNombre) {
        return new Proyecto(this.id, nuevoNombre, this.ubicacion, this.estado, this.fechaInicio, this.frecuenciaControl);
    }

    /**
     * Actualiza la ubicación del proyecto.
     *
     * @return Nueva instancia de Proyecto con la ubicación actualizada.
     */
    public Proyecto actualizarUbicacion(String nuevaUbicacion) {
        return new Proyecto(this.id, this.nombre, nuevaUbicacion, this.estado, this.fechaInicio, this.frecuenciaControl);
    }

    /**
     * Inicia el proyecto (cambia el estado a ACTIVO).
     *
     * @return Nueva instancia de Proyecto en estado ACTIVO.
     */
    public Proyecto activar() {
        return new Proyecto(this.id, this.nombre, this.ubicacion, EstadoProyecto.ACTIVO, this.fechaInicio, this.frecuenciaControl);
    }

    /**
     * Suspende el proyecto (cambia el estado a SUSPENDIDO).
     *
     * @return Nueva instancia de Proyecto en estado SUSPENDIDO.
     */
    public Proyecto suspender() {
        return new Proyecto(this.id, this.nombre, this.ubicacion, EstadoProyecto.SUSPENDIDO, this.fechaInicio, this.frecuenciaControl);
    }

    /**
     * Finaliza el proyecto (cambia el estado a CERRADO).
     *
     * @return Nueva instancia de Proyecto en estado CERRADO.
     */
    public Proyecto cerrar() {
        return new Proyecto(this.id, this.nombre, this.ubicacion, EstadoProyecto.CERRADO, this.fechaInicio, this.frecuenciaControl);
    }

    /**
     * Configura la frecuencia de control/corte para reportes (Invariante E-04).
     * Requiere fechaInicio no nula cuando se configura una frecuencia, ya que
     * esFechaCorteValida la necesita para validar.
     *
     * @param frecuencia  SEMANAL, QUINCENAL o MENSUAL; null para limpiar la configuración
     * @param fechaInicio fecha de inicio del proyecto; obligatoria cuando frecuencia no es null
     * @return Nueva instancia de Proyecto con la configuración aplicada
     */
    public Proyecto configurarFrecuencia(FrecuenciaControl frecuencia, LocalDateTime fechaInicio) {
        if (frecuencia != null && fechaInicio == null) {
            throw new IllegalArgumentException("fechaInicio es obligatoria cuando se configura frecuencia de control");
        }
        LocalDateTime fecha = frecuencia != null ? fechaInicio : null;
        return new Proyecto(this.id, this.nombre, this.ubicacion, this.estado, fecha, frecuencia);
    }

    /**
     * Indica si la fecha de corte es válida según la frecuencia configurada.
     * Si frecuenciaControl es null, no hay exigencia y retorna true.
     *
     * @param fechaCorte fecha de corte a validar
     * @return true si no hay enforcement o si la fecha es válida según la frecuencia
     */
    public boolean esFechaCorteValida(LocalDate fechaCorte) {
        if (frecuenciaControl == null) {
            return true;
        }
        if (fechaInicio == null) {
            return false;
        }
        return frecuenciaControl.esFechaValida(fechaInicio.toLocalDate(), fechaCorte);
    }

    // Getters

    public ProyectoId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }

    public boolean isActivo() {
        return estado == EstadoProyecto.ACTIVO;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public FrecuenciaControl getFrecuenciaControl() {
        return frecuenciaControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Proyecto proyecto = (Proyecto) o;
        return Objects.equals(id, proyecto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Proyecto{id=%s, nombre='%s', ubicacion='%s', estado=%s}", id, nombre, ubicacion, estado);
    }
}
