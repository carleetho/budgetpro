package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root representing a Crew (Cuadrilla), a team of employees led by a
 * specific leader.
 * 
 * Refactored for 100% Immutability.
 */
public final class Cuadrilla {

    private final CuadrillaId id;
    private final ProyectoId proyectoId;
    private final String nombre;
    private final String tipo;
    private final EmpleadoId liderId;
    private final List<CuadrillaMiembro> miembros;
    private final EstadoCuadrilla estado;

    // Private constructor
    private Cuadrilla(CuadrillaId id, ProyectoId proyectoId, String nombre, String tipo, EmpleadoId liderId,
            EstadoCuadrilla estado, List<CuadrillaMiembro> miembros) {
        this.id = Objects.requireNonNull(id, "CuadrillaId cannot be null");
        this.proyectoId = Objects.requireNonNull(proyectoId, "ProyectoId cannot be null");
        this.nombre = Objects.requireNonNull(nombre, "Nombre cannot be null");
        this.tipo = Objects.requireNonNull(tipo, "Tipo cannot be null");
        this.liderId = Objects.requireNonNull(liderId, "LiderId cannot be null");
        this.estado = Objects.requireNonNull(estado, "Estado cannot be null");
        this.miembros = miembros != null ? Collections.unmodifiableList(new ArrayList<>(miembros))
                : Collections.emptyList();
    }

    /**
     * Factory method to create a new Crew.
     */
    public static Cuadrilla crear(CuadrillaId id, ProyectoId proyectoId, String nombre, String tipo, EmpleadoId liderId,
            List<CuadrillaMiembro> miembrosIniciales) {
        if (nombre.isBlank())
            throw new IllegalArgumentException("Nombre cannot be empty");
        if (tipo.isBlank())
            throw new IllegalArgumentException("Tipo cannot be empty");

        return new Cuadrilla(id, proyectoId, nombre, tipo, liderId, EstadoCuadrilla.ACTIVA, miembrosIniciales);
    }

    public static Cuadrilla reconstruir(CuadrillaId id, ProyectoId proyectoId, String nombre, String tipo,
            EmpleadoId liderId, EstadoCuadrilla estado, List<CuadrillaMiembro> miembros) {
        return new Cuadrilla(id, proyectoId, nombre, tipo, liderId, estado, miembros);
    }

    /**
     * Adds a new member to the crew.
     * 
     * @return A new instance of Cuadrilla with the added member.
     */
    public Cuadrilla agregarMiembro(EmpleadoId empleadoId, String rol) {
        if (estado != EstadoCuadrilla.ACTIVA) {
            throw new IllegalStateException("Cannot add member to inactive or dissolved crew");
        }

        // Ensure not already an active member
        boolean alreadyActive = miembros.stream().anyMatch(m -> m.getEmpleadoId().equals(empleadoId) && m.esActivo());

        if (alreadyActive) {
            throw new IllegalArgumentException("Employee is already an active member of this crew");
        }

        List<CuadrillaMiembro> nuevosMiembros = new ArrayList<>(this.miembros);
        CuadrillaMiembro nuevoMiembro = CuadrillaMiembro.crear(CuadrillaMiembroId.generate(), empleadoId, rol,
                LocalDate.now());
        nuevosMiembros.add(nuevoMiembro);

        return new Cuadrilla(this.id, this.proyectoId, this.nombre, this.tipo, this.liderId, this.estado,
                nuevosMiembros);
    }

    /**
     * Removes a member from the crew (soft delete).
     * 
     * @return A new instance of Cuadrilla with the member marked as removed.
     */
    public Cuadrilla removerMiembro(CuadrillaMiembroId miembroId) {
        CuadrillaMiembro miembro = miembros.stream().filter(m -> m.getId().equals(miembroId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found in this crew"));

        if (!miembro.esActivo()) {
            throw new IllegalStateException("Member is already inactive");
        }

        List<CuadrillaMiembro> nuevosMiembros = new ArrayList<>(this.miembros);
        nuevosMiembros.remove(miembro);
        nuevosMiembros.add(miembro.remover(LocalDate.now()));

        return new Cuadrilla(this.id, this.proyectoId, this.nombre, this.tipo, this.liderId, this.estado,
                nuevosMiembros);
    }

    /**
     * Changes the leader of the crew.
     * 
     * @return A new instance of Cuadrilla with the new leader.
     */
    public Cuadrilla cambiarLider(EmpleadoId nuevoLiderId) {
        if (estado != EstadoCuadrilla.ACTIVA) {
            throw new IllegalStateException("Cannot change leader of inactive or dissolved crew");
        }
        return new Cuadrilla(this.id, this.proyectoId, this.nombre, this.tipo,
                Objects.requireNonNull(nuevoLiderId, "NuevoLiderId cannot be null"), this.estado, this.miembros);
    }

    /**
     * Inactivates the crew.
     * 
     * @return A new instance of Cuadrilla with INACTIVA status.
     */
    public Cuadrilla inactivar() {
        if (this.estado == EstadoCuadrilla.DISUELTA) {
            throw new IllegalStateException("Cannot inactivate a dissolved crew");
        }
        return new Cuadrilla(this.id, this.proyectoId, this.nombre, this.tipo, this.liderId, EstadoCuadrilla.INACTIVA,
                this.miembros);
    }

    /**
     * Dissolves the crew.
     * 
     * @return A new instance of Cuadrilla with DISUELTA status and all members
     *         removed.
     */
    public Cuadrilla disolver() {
        LocalDate now = LocalDate.now();
        List<CuadrillaMiembro> nuevosMiembros = new ArrayList<>();

        for (CuadrillaMiembro m : miembros) {
            if (m.esActivo()) {
                nuevosMiembros.add(m.remover(now));
            } else {
                nuevosMiembros.add(m);
            }
        }

        return new Cuadrilla(this.id, this.proyectoId, this.nombre, this.tipo, this.liderId, EstadoCuadrilla.DISUELTA,
                nuevosMiembros);
    }

    // Getters

    public CuadrillaId getId() {
        return id;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public EmpleadoId getLiderId() {
        return liderId;
    }

    public EstadoCuadrilla getEstado() {
        return estado;
    }

    public List<CuadrillaMiembro> getMiembros() {
        return miembros;
    }

    public Optional<CuadrillaMiembro> getMiembroActivo(EmpleadoId empleadoId) {
        return miembros.stream().filter(m -> m.getEmpleadoId().equals(empleadoId) && m.esActivo()).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Cuadrilla cuadrilla = (Cuadrilla) o;
        return Objects.equals(id, cuadrilla.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
