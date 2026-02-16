package com.budgetpro.domain.logistica.compra.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado PROVEEDOR.
 * 
 * Representa un proveedor de bienes o servicios para el proyecto.
 * 
 * Invariantes:
 * - La razón social no puede ser nula ni estar en blanco
 * - El RUC no puede ser nulo ni estar en blanco
 * - El RUC debe ser único (enforced at repository level)
 * - El estado no puede ser nulo
 * - Solo proveedores con estado=ACTIVO pueden ser utilizados en compras (Invariant L-04)
 * - Los campos de auditoría son obligatorios (REGLA-167)
 * 
 * Contexto: Logística & Compras
 */
public final class Proveedor {

    private final ProveedorId id;
    private final String razonSocial;
    private final String ruc;
    
    // JUSTIFICACIÓN ARQUITECTÓNICA: Aggregate Root con estado mutable.
    // Campos que representan el estado evolutivo del proveedor:
    // - estado: workflow transitions (ACTIVO ↔ INACTIVO ↔ BLOQUEADO)
    // - contacto: información de contacto actualizable
    // - direccion: dirección física actualizable
    // - version: optimistic locking para concurrencia
    // - updatedBy, updatedAt: campos de auditoría actualizables
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private ProveedorEstado estado;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private String contacto;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private String direccion;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private Long version;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private UUID updatedBy;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private LocalDateTime updatedAt;

    // Campos de auditoría inmutables (REGLA-167)
    private final UUID createdBy;
    private final LocalDateTime createdAt;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Proveedor(ProveedorId id, String razonSocial, String ruc, ProveedorEstado estado,
                     String contacto, String direccion, Long version,
                     UUID createdBy, LocalDateTime createdAt, UUID updatedBy, LocalDateTime updatedAt) {
        validarInvariantes(razonSocial, ruc, estado, createdBy, createdAt);

        this.id = Objects.requireNonNull(id, "El ID del proveedor no puede ser nulo");
        this.razonSocial = normalizarRazonSocial(razonSocial);
        this.ruc = normalizarRuc(ruc);
        this.estado = estado != null ? estado : ProveedorEstado.ACTIVO;
        this.contacto = contacto != null && !contacto.isBlank() ? contacto.trim() : null;
        this.direccion = direccion != null && !direccion.isBlank() ? direccion.trim() : null;
        this.version = version != null ? version : 0L;
        this.createdBy = Objects.requireNonNull(createdBy, "El createdBy no puede ser nulo (REGLA-167)");
        this.createdAt = Objects.requireNonNull(createdAt, "El createdAt no puede ser nulo (REGLA-167)");
        this.updatedBy = updatedBy != null ? updatedBy : createdBy;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
    }

    /**
     * Factory method para crear un nuevo Proveedor en estado ACTIVO.
     * 
     * @param id ID del proveedor
     * @param razonSocial Razón social del proveedor
     * @param ruc RUC (número de identificación tributaria)
     * @param contacto Información de contacto (opcional)
     * @param direccion Dirección física (opcional)
     * @param createdBy ID del usuario que crea el proveedor
     * @param createdAt Fecha y hora de creación
     * @return Nuevo proveedor en estado ACTIVO
     */
    public static Proveedor crear(ProveedorId id, String razonSocial, String ruc,
                                  String contacto, String direccion,
                                  UUID createdBy, LocalDateTime createdAt) {
        return new Proveedor(id, razonSocial, ruc, ProveedorEstado.ACTIVO,
                            contacto, direccion, 0L, createdBy, createdAt, null, null);
    }

    /**
     * Factory method para reconstruir un Proveedor desde persistencia.
     * 
     * @param id ID del proveedor
     * @param razonSocial Razón social del proveedor
     * @param ruc RUC (número de identificación tributaria)
     * @param estado Estado actual del proveedor
     * @param contacto Información de contacto (opcional)
     * @param direccion Dirección física (opcional)
     * @param version Versión para optimistic locking
     * @param createdBy ID del usuario que creó el proveedor
     * @param createdAt Fecha y hora de creación
     * @param updatedBy ID del usuario que actualizó el proveedor
     * @param updatedAt Fecha y hora de última actualización
     * @return Proveedor reconstruido desde persistencia
     */
    public static Proveedor reconstruir(ProveedorId id, String razonSocial, String ruc,
                                       ProveedorEstado estado, String contacto, String direccion,
                                       Long version,
                                       UUID createdBy, LocalDateTime createdAt,
                                       UUID updatedBy, LocalDateTime updatedAt) {
        return new Proveedor(id, razonSocial, ruc, estado, contacto, direccion, version,
                            createdBy, createdAt, updatedBy, updatedAt);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(String razonSocial, String ruc, ProveedorEstado estado,
                                    UUID createdBy, LocalDateTime createdAt) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social no puede estar vacía");
        }
        if (ruc == null || ruc.isBlank()) {
            throw new IllegalArgumentException("El RUC no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo (REGLA-167)");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("El createdAt no puede ser nulo (REGLA-167)");
        }
    }

    /**
     * Normaliza la razón social (trim).
     */
    private String normalizarRazonSocial(String razonSocial) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social no puede estar vacía");
        }
        return razonSocial.trim();
    }

    /**
     * Normaliza el RUC (trim).
     */
    private String normalizarRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            throw new IllegalArgumentException("El RUC no puede estar vacío");
        }
        return ruc.trim();
    }

    /**
     * Activa el proveedor (cambia el estado a ACTIVO).
     * Solo proveedores ACTIVOS pueden ser utilizados en compras (Invariant L-04).
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     */
    public void activar(UUID updatedBy, LocalDateTime updatedAt) {
        this.estado = ProveedorEstado.ACTIVO;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Inactiva el proveedor (cambia el estado a INACTIVO).
     * Los proveedores INACTIVOS no pueden ser utilizados en nuevas compras.
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     */
    public void inactivar(UUID updatedBy, LocalDateTime updatedAt) {
        this.estado = ProveedorEstado.INACTIVO;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Bloquea el proveedor (cambia el estado a BLOQUEADO).
     * Los proveedores BLOQUEADOS requieren intervención manual para desbloquear.
     * 
     * @param updatedBy ID del usuario que realiza la acción
     * @param updatedAt Fecha y hora de la actualización
     */
    public void bloquear(UUID updatedBy, LocalDateTime updatedAt) {
        this.estado = ProveedorEstado.BLOQUEADO;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Actualiza la información de contacto del proveedor.
     * 
     * @param nuevoContacto Nueva información de contacto (puede ser null para eliminar)
     * @param updatedBy ID del usuario que realiza la actualización
     * @param updatedAt Fecha y hora de la actualización
     */
    public void actualizarContacto(String nuevoContacto, UUID updatedBy, LocalDateTime updatedAt) {
        this.contacto = nuevoContacto != null && !nuevoContacto.isBlank() ? nuevoContacto.trim() : null;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Actualiza la dirección física del proveedor.
     * 
     * @param nuevaDireccion Nueva dirección física (puede ser null para eliminar)
     * @param updatedBy ID del usuario que realiza la actualización
     * @param updatedAt Fecha y hora de la actualización
     */
    public void actualizarDireccion(String nuevaDireccion, UUID updatedBy, LocalDateTime updatedAt) {
        this.direccion = nuevaDireccion != null && !nuevaDireccion.isBlank() ? nuevaDireccion.trim() : null;
        this.updatedBy = Objects.requireNonNull(updatedBy, "El updatedBy no puede ser nulo");
        this.updatedAt = Objects.requireNonNull(updatedAt, "El updatedAt no puede ser nulo");
    }

    /**
     * Verifica si el proveedor está activo y puede ser utilizado en compras.
     * 
     * @return true si el estado es ACTIVO, false en caso contrario
     */
    public boolean isActivo() {
        return estado == ProveedorEstado.ACTIVO;
    }

    // Getters

    public ProveedorId getId() {
        return id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public ProveedorEstado getEstado() {
        return estado;
    }

    public String getContacto() {
        return contacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proveedor proveedor = (Proveedor) o;
        return Objects.equals(id, proveedor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Proveedor{id=%s, razonSocial='%s', ruc='%s', estado=%s}",
                id, razonSocial, ruc, estado);
    }
}
