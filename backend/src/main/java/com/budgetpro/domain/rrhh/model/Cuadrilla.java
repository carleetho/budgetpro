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
 */
public class Cuadrilla {

    private final CuadrillaId id;
    private final ProyectoId proyectoId;
    private String nombre; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - Crew name editable
    private String tipo; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - Crew type changeable
    private EmpleadoId liderId; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - Leader reassignment workflow
    private final List<CuadrillaMiembro> miembros;
    private EstadoCuadrilla estado; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - State machine ACTIVA → INACTIVA → DISUELTA

    // Private constructor
    private Cuadrilla(CuadrillaId id, ProyectoId proyectoId, String nombre, String tipo, EmpleadoId liderId,
            EstadoCuadrilla estado) {
        this.id = Objects.requireNonNull(id, "CuadrillaId cannot be null");
        this.proyectoId = Objects.requireNonNull(proyectoId, "ProyectoId cannot be null");
        this.nombre = Objects.requireNonNull(nombre, "Nombre cannot be null");
        this.tipo = Objects.requireNonNull(tipo, "Tipo cannot be null");
        this.liderId = Objects.requireNonNull(liderId, "LiderId cannot be null");
        this.estado = Objects.requireNonNull(estado, "Estado cannot be null");
        this.miembros = new ArrayList<>();
    }

    /**
     * Factory method to create a new Crew.
     * 
     * @param id                CuadrillaId
     * @param proyectoId        ProyectoId
     * @param nombre            Name of the crew
     * @param tipo              Type of crew
     * @param liderId           ID of the leader (must be an active employee -
     *                          validation assumed in Usecase but enforced as
     *                          non-null here)
     * @param miembrosIniciales Initial list of members (optional)
     * @return New Cuadrilla instance
     */
    public static Cuadrilla crear(CuadrillaId id, ProyectoId proyectoId, String nombre, String tipo, EmpleadoId liderId,
            List<CuadrillaMiembro> miembrosIniciales) {
        if (nombre.isBlank())
            throw new IllegalArgumentException("Nombre cannot be empty");
        if (tipo.isBlank())
            throw new IllegalArgumentException("Tipo cannot be empty");

        Cuadrilla cuadrilla = new Cuadrilla(id, proyectoId, nombre, tipo, liderId, EstadoCuadrilla.ACTIVA);

        if (miembrosIniciales != null) {
            cuadrilla.miembros.addAll(miembrosIniciales);
        }

        return cuadrilla;
    }

    public static Cuadrilla reconstruir(CuadrillaId id, ProyectoId proyectoId, String nombre, String tipo,
            EmpleadoId liderId, EstadoCuadrilla estado, List<CuadrillaMiembro> miembros) {
        Cuadrilla cuadrilla = new Cuadrilla(id, proyectoId, nombre, tipo, liderId, estado);
        if (miembros != null) {
            cuadrilla.miembros.addAll(miembros);
        }
        return cuadrilla;
    }

    /**
     * Adds a new member to the crew. Uses current date as entry date.
     */
    public void agregarMiembro(EmpleadoId empleadoId, String rol) {
        if (estado != EstadoCuadrilla.ACTIVA) {
            throw new IllegalStateException("Cannot add member to inactive or dissolved crew");
        }

        // Ensure not already an active member
        boolean alreadyActive = miembros.stream().anyMatch(m -> m.getEmpleadoId().equals(empleadoId) && m.esActivo());

        if (alreadyActive) {
            throw new IllegalArgumentException("Employee is already an active member of this crew");
        }

        CuadrillaMiembro nuevoMiembro = CuadrillaMiembro.crear(CuadrillaMiembroId.generate(), empleadoId, rol,
                LocalDate.now());
        this.miembros.add(nuevoMiembro);
    }

    /**
     * Removes a member from the crew (soft delete). Uses current date as exit date.
     */
    public void removerMiembro(CuadrillaMiembroId miembroId) {
        CuadrillaMiembro miembro = miembros.stream().filter(m -> m.getId().equals(miembroId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found in this crew"));

        if (!miembro.esActivo()) {
            throw new IllegalStateException("Member is already inactive");
        }

        miembro.remover(LocalDate.now());
    }

    /**
     * Changes the leader of the crew.
     */
    public void cambiarLider(EmpleadoId nuevoLiderId) {
        if (estado != EstadoCuadrilla.ACTIVA) {
            throw new IllegalStateException("Cannot change leader of inactive or dissolved crew");
        }
        this.liderId = Objects.requireNonNull(nuevoLiderId, "NuevoLiderId cannot be null");
    }

    /**
     * Inactivates the crew.
     */
    public void inactivar() {
        if (this.estado == EstadoCuadrilla.DISUELTA) {
            throw new IllegalStateException("Cannot inactivate a dissolved crew");
        }
        this.estado = EstadoCuadrilla.INACTIVA;
        // Optionally deactivate all active members?
        // Requirements say "Crew lifecycle: ACTIVA -> INACTIVA -> DISUELTA"
        // usually implies active members might remain associated historically or should
        // be "closed".
        // Use case logic can dictate this, aggregate ensures state consistency.
    }

    /**
     * Dissolves the crew.
     */
    public void disolver() {
        this.estado = EstadoCuadrilla.DISUELTA;
        // Close all active memberships
        LocalDate now = LocalDate.now();
        miembros.stream().filter(CuadrillaMiembro::esActivo).forEach(m -> m.remover(now));
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
        return Collections.unmodifiableList(miembros);
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
