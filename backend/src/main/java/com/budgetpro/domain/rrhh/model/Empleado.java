package com.budgetpro.domain.rrhh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root for the Human Resources (RRHH) domain. Represents an Employee
 * within the organization.
 * 
 * <p>
 * <strong>Invariants:</strong>
 * </p>
 * <ul>
 * <li>Must have exactly one active {@link HistorialLaboral} record at any time
 * (if active).</li>
 * <li>Identification number must be unique and encrypted at rest layer.</li>
 * </ul>
 */
public class Empleado {

    private final EmpleadoId id;
    private String nombre; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - PII editable via actualizarDatosPersonales()
    private String apellido; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - PII editable via actualizarDatosPersonales()

    /**
     * National Identification Number (DNI, SSN, etc.).
     * <p>
     * <strong>SECURITY NOTE:</strong> This field MUST be encrypted when persisted
     * to the database.
     * </p>
     */
    private final String numeroIdentificacion;

    private Contacto contacto; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - Contact info updatable
    private EstadoEmpleado estado; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - State machine ACTIVO → INACTIVO

    private Map<String, Object> atributos; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - Certifications/Skills map mutated via agregarAtributo()

    private final List<HistorialLaboral> historial;

    // Private constructor for factory methods
    private Empleado(EmpleadoId id, String nombre, String apellido, String numeroIdentificacion, Contacto contacto,
            EstadoEmpleado estado, Map<String, Object> atributos) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.nombre = Objects.requireNonNull(nombre, "Nombre cannot be null");
        this.apellido = Objects.requireNonNull(apellido, "Apellido cannot be null");
        this.numeroIdentificacion = Objects.requireNonNull(numeroIdentificacion,
                "Numero Identificacion cannot be null");
        this.contacto = contacto; // Nullable
        this.estado = Objects.requireNonNull(estado, "Estado cannot be null");
        this.atributos = atributos != null ? new HashMap<>(atributos) : new HashMap<>();
        this.historial = new ArrayList<>();
    }

    // Extended private constructor for reconstitution
    private Empleado(EmpleadoId id, String nombre, String apellido, String numeroIdentificacion, Contacto contacto,
            EstadoEmpleado estado, Map<String, Object> atributos, List<HistorialLaboral> historial) {
        this(id, nombre, apellido, numeroIdentificacion, contacto, estado, atributos);
        if (historial != null) {
            this.historial.addAll(historial);
        }
    }

    /**
     * Factory method to create a new Employee with an initial employment record.
     */
    public static Empleado crear(EmpleadoId id, String nombre, String apellido, String numeroIdentificacion,
            Contacto contacto, LocalDate fechaContratacion, BigDecimal salarioInicial, String puestoInicial,
            TipoEmpleado tipo) {

        if (numeroIdentificacion.isBlank()) {
            throw new IllegalArgumentException("Numero Identificacion cannot be empty");
        }

        Empleado empleado = new Empleado(id, nombre, apellido, numeroIdentificacion, contacto, EstadoEmpleado.ACTIVO,
                new HashMap<>());

        // Create initial history record
        HistorialLaboral inicial = HistorialLaboral.crear(HistorialId.generate(), puestoInicial, salarioInicial, tipo,
                fechaContratacion);

        empleado.historial.add(inicial);

        return empleado;
    }

    /**
     * Reconstitutes an employee from persistence.
     */
    public static Empleado reconstruir(EmpleadoId id, String nombre, String apellido, String numeroIdentificacion,
            Contacto contacto, EstadoEmpleado estado, Map<String, Object> atributos, List<HistorialLaboral> historial) {
        return new Empleado(id, nombre, apellido, numeroIdentificacion, contacto, estado, atributos, historial);
    }

    /**
     * Updates the employee's salary and/or position by creating a new history
     * record. The current active record is closed with the day before the new start
     * date.
     * 
     * @param nuevoSalario  New base salary
     * @param nuevoCargo    New position title (optional, uses current if null)
     * @param fechaEfectiva Date when the change takes effect
     * @param motivo        Reason for change (stored in attributes/logs - not
     *                      implemented in core model for simplicity)
     */
    public void actualizarCondicionesLaborales(BigDecimal nuevoSalario, String nuevoCargo, LocalDate fechaEfectiva) {
        HistorialLaboral current = getSalarioActual()
                .orElseThrow(() -> new IllegalStateException("Employee has no active employment record"));

        if (fechaEfectiva.isBefore(current.getFechaInicio())) {
            throw new IllegalArgumentException("New effective date cannot be before current start date");
        }

        // Close current record
        current.cerrar(fechaEfectiva.minusDays(1));

        // Create new record
        HistorialLaboral nuevo = HistorialLaboral.crear(HistorialId.generate(),
                nuevoCargo != null ? nuevoCargo : current.getCargo(),
                nuevoSalario != null ? nuevoSalario : current.getSalarioBase(), current.getTipoEmpleado(), // Preserve
                                                                                                           // type
                                                                                                           // unless
                                                                                                           // explicitly
                                                                                                           // changed
                                                                                                           // (could
                                                                                                           // serve as
                                                                                                           // overload)
                fechaEfectiva);

        this.historial.add(nuevo);
    }

    /**
     * Updates salary only.
     */
    public void actualizarSalario(BigDecimal nuevoSalario, LocalDate fechaEfectiva) {
        actualizarCondicionesLaborales(nuevoSalario, null, fechaEfectiva);
    }

    /**
     * Updates position only.
     */
    public void cambiarPuesto(String nuevoCargo, LocalDate fechaEfectiva) {
        actualizarCondicionesLaborales(null, nuevoCargo, fechaEfectiva);
    }

    /**
     * Inactivates the employee.
     */
    public void inactivar(LocalDate fechaSalida) {
        this.estado = EstadoEmpleado.INACTIVO;
        getSalarioActual().ifPresent(h -> h.cerrar(fechaSalida));
    }

    /**
     * Gets the currently active employment record (where end date is null).
     */
    public Optional<HistorialLaboral> getSalarioActual() {
        return historial.stream().filter(HistorialLaboral::esActivo).findFirst();
    }

    /**
     * Gets the employment record valid at a specific date.
     */
    public Optional<HistorialLaboral> getSalarioEnFecha(LocalDate fecha) {
        return historial.stream().filter(h -> h.esValidoEnFecha(fecha)).findFirst();
    }

    // Getters and Attr management

    public EmpleadoId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public EstadoEmpleado getEstado() {
        return estado;
    }

    public List<HistorialLaboral> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public void agregarAtributo(String key, Object value) {
        this.atributos.put(key, value);
    }

    public Map<String, Object> getAtributos() {
        return Collections.unmodifiableMap(atributos);
    }

    public void actualizarDatosPersonales(String nombre, String apellido, Contacto contacto) {
        if (nombre != null && !nombre.isBlank()) {
            this.nombre = nombre;
        }
        if (apellido != null && !apellido.isBlank()) {
            this.apellido = apellido;
        }
        if (contacto != null) {
            this.contacto = contacto;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Empleado empleado = (Empleado) o;
        return Objects.equals(id, empleado.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
