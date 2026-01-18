package com.budgetpro.domain.logistica.almacen.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Agregado que representa un almacén físico.
 */
public final class Almacen {
    
    private final AlmacenId id;
    private final UUID proyectoId;
    private final String codigo;
    private String nombre;
    private String ubicacion;
    private UUID responsableId;
    private boolean activo;
    
    /**
     * Constructor privado. Usar factory methods.
     */
    private Almacen(AlmacenId id, UUID proyectoId, String codigo, String nombre,
                   String ubicacion, UUID responsableId, boolean activo) {
        this.id = Objects.requireNonNull(id, "El ID del almacén no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código del almacén no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre del almacén no puede ser nulo");
        this.ubicacion = ubicacion;
        this.responsableId = responsableId;
        this.activo = activo;
    }
    
    /**
     * Factory method para crear un nuevo almacén.
     */
    public static Almacen crear(AlmacenId id, UUID proyectoId, String codigo, String nombre,
                               String ubicacion, UUID responsableId) {
        return new Almacen(id, proyectoId, codigo, nombre, ubicacion, responsableId, true);
    }
    
    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static Almacen reconstruir(AlmacenId id, UUID proyectoId, String codigo, String nombre,
                                     String ubicacion, UUID responsableId, boolean activo) {
        return new Almacen(id, proyectoId, codigo, nombre, ubicacion, responsableId, activo);
    }
    
    /**
     * Desactiva el almacén.
     */
    public void desactivar() {
        this.activo = false;
    }
    
    /**
     * Activa el almacén.
     */
    public void activar() {
        this.activo = true;
    }
    
    // Getters
    
    public AlmacenId getId() { return id; }
    public UUID getProyectoId() { return proyectoId; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public UUID getResponsableId() { return responsableId; }
    public void setResponsableId(UUID responsableId) { this.responsableId = responsableId; }
    public boolean isActivo() { return activo; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Almacen almacen = (Almacen) o;
        return Objects.equals(id, almacen.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
