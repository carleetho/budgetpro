package com.budgetpro.domain.finanzas.presupuesto.model;

import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado PRESUPUESTO.
 * 
 * Representa un presupuesto asociado a un proyecto.
 * 
 * Invariantes: - El nombre no puede estar vacío - El proyectoId no puede ser
 * nulo - El estado no puede ser nulo - Un proyecto solo tiene un presupuesto
 * activo (validado a nivel de persistencia)
 * 
 * **Integridad Criptográfica (Swiss-Grade Engineering):**
 * 
 * Este agregado implementa un patrón de dual-hash para garantizar la integridad
 * del presupuesto a nivel criptográfico:
 * 
 * 1. **Hash de Aprobación (integrityHashApproval):** - Se genera una sola vez
 * al aprobar el presupuesto - Es INMUTABLE después de la aprobación - Captura
 * la estructura completa del presupuesto en el momento de aprobación - Se usa
 * para detectar modificaciones no autorizadas a la estructura
 * 
 * 2. **Hash de Ejecución (integrityHashExecution):** - Se genera al aprobar y
 * se actualiza después de cada transacción financiera - Es DINÁMICO y refleja
 * el estado actual de ejecución - Captura el estado financiero (consumos,
 * saldos, movimientos) - Se usa para detectar inconsistencias en la ejecución
 * 
 * 3. **Hard-Freeze Pattern:** - Una vez que integrityHashApproval está
 * establecido, el presupuesto está "sellado" - Cualquier intento de modificar
 * campos estructurales lanza BudgetIntegrityViolationException - Solo se
 * permite actualizar el hash de ejecución después de transacciones financieras
 * 
 * **Algoritmo:** SHA-256-v1 (64 caracteres hexadecimales)
 * 
 * Contexto: Presupuestos & APUs
 */
public final class Presupuesto {

    private final PresupuestoId id;
    private final UUID proyectoId;
    private String nombre; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                           // Business logic requires renaming
    private EstadoPresupuesto estado; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                      // State machine transition
    private Boolean esContractual; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                   // State flag
    private Long version; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                          // Optimistic locking

    // Integrity Hash Fields (Swiss-Grade Engineering)
    /**
     * Hash criptográfico inmutable de la estructura del presupuesto al momento de
     * aprobación. Se genera una sola vez y nunca cambia después de la aprobación.
     * Formato: 64 caracteres hexadecimales (SHA-256).
     */
    private String integrityHashApproval; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                          // Security pattern (set on approval)

    /**
     * Hash criptográfico dinámico del estado de ejecución del presupuesto. Se
     * actualiza después de cada transacción financiera para reflejar el estado
     * actual. Formato: 64 caracteres hexadecimales (SHA-256).
     */
    private String integrityHashExecution; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                           // Security pattern (dynamic hash)

    /**
     * Timestamp de cuando se generaron los hashes de integridad.
     */
    private LocalDateTime integrityHashGeneratedAt; // nosemgrep:
                                                    // budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                                    // Audit trail

    /**
     * ID del usuario que aprobó el presupuesto y generó los hashes.
     */
    private UUID integrityHashGeneratedBy; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                           // Audit trail

    /**
     * Versión del algoritmo de hash usado. Actualmente "SHA-256-v1". Permite
     * migración futura a algoritmos más seguros sin romper compatibilidad.
     */
    private String integrityHashAlgorithm; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.presupuesto -
                                           // Audit metadata

    /**
     * Constructor privado. Usar factory methods.
     */
    private Presupuesto(PresupuestoId id, UUID proyectoId, String nombre, EstadoPresupuesto estado,
            Boolean esContractual, Long version, String integrityHashApproval, String integrityHashExecution,
            LocalDateTime integrityHashGeneratedAt, UUID integrityHashGeneratedBy, String integrityHashAlgorithm) {
        validarInvariantes(proyectoId, nombre, estado);

        this.id = Objects.requireNonNull(id, "El ID del presupuesto no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.nombre = normalizarNombre(nombre);
        this.estado = Objects.requireNonNull(estado, "El estado del presupuesto no puede ser nulo");
        this.esContractual = esContractual != null ? esContractual : false;
        this.version = version != null ? version : 0L;

        // Integrity hash fields (nullable until approval)
        this.integrityHashApproval = integrityHashApproval;
        this.integrityHashExecution = integrityHashExecution;
        this.integrityHashGeneratedAt = integrityHashGeneratedAt;
        this.integrityHashGeneratedBy = integrityHashGeneratedBy;
        this.integrityHashAlgorithm = integrityHashAlgorithm;
    }

    /**
     * Factory method para crear un nuevo Presupuesto en estado BORRADOR. Los campos
     * de integridad hash se inicializan como null hasta la aprobación.
     */
    public static Presupuesto crear(PresupuestoId id, UUID proyectoId, String nombre) {
        return new Presupuesto(id, proyectoId, nombre, EstadoPresupuesto.BORRADOR, false, 0L, null, null, null, null,
                null);
    }

    /**
     * Factory method para reconstruir un Presupuesto desde persistencia (firma
     * simplificada). Los campos de integridad hash se establecen como null (para
     * compatibilidad con código existente).
     * 
     * @deprecated Use la firma completa con campos de integridad hash cuando estén
     *             disponibles en la persistencia.
     */
    @Deprecated
    public static Presupuesto reconstruir(PresupuestoId id, UUID proyectoId, String nombre, EstadoPresupuesto estado,
            Boolean esContractual, Long version) {
        return new Presupuesto(id, proyectoId, nombre, estado, esContractual, version, null, null, null, null, null);
    }

    /**
     * Factory method para reconstruir un Presupuesto desde persistencia. Incluye
     * todos los campos de integridad hash si el presupuesto fue aprobado.
     */
    public static Presupuesto reconstruir(PresupuestoId id, UUID proyectoId, String nombre, EstadoPresupuesto estado,
            Boolean esContractual, Long version, String integrityHashApproval, String integrityHashExecution,
            LocalDateTime integrityHashGeneratedAt, UUID integrityHashGeneratedBy, String integrityHashAlgorithm) {
        return new Presupuesto(id, proyectoId, nombre, estado, esContractual, version, integrityHashApproval,
                integrityHashExecution, integrityHashGeneratedAt, integrityHashGeneratedBy, integrityHashAlgorithm);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, String nombre, EstadoPresupuesto estado) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del presupuesto no puede ser nulo");
        }
    }

    /**
     * Normaliza el nombre del presupuesto (trim).
     */
    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío");
        }
        return nombre.trim();
    }

    /**
     * Actualiza el nombre del presupuesto.
     * 
     * **Hard-Freeze Guard:** Si el presupuesto tiene un hash de aprobación
     * establecido, cualquier modificación lanza BudgetIntegrityViolationException
     * para proteger la integridad.
     * 
     * @param nuevoNombre El nuevo nombre del presupuesto
     * @throws IllegalArgumentException          si el nombre es nulo o vacío
     * @throws BudgetIntegrityViolationException si el presupuesto está sellado
     *                                           (tiene hash de aprobación)
     */
    public void actualizarNombre(String nuevoNombre) {
        // Hard-freeze check: no se puede modificar un presupuesto sellado
        if (integrityHashApproval != null) {
            throw new BudgetIntegrityViolationException(this.id, integrityHashApproval, "N/A",
                    "Structure modification attempted");
        }

        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío");
        }
        this.nombre = nuevoNombre.trim();
    }

    /**
     * Aprueba el presupuesto y genera los hashes criptográficos de integridad.
     * 
     * Este método implementa el patrón de "hard-freeze" mediante sellado
     * criptográfico:
     * 
     * 1. Cambia el estado a CONGELADO y marca como contractual 2. Genera el hash de
     * aprobación (immutable) que captura la estructura del presupuesto 3. Genera el
     * hash de ejecución (dinámico) que refleja el estado financiero inicial 4.
     * Registra metadata de cuándo y quién aprobó el presupuesto
     * 
     * **Una vez aprobado, el presupuesto está "sellado" y no puede modificarse
     * estructuralmente.**
     * 
     * @param approvedBy  ID del usuario que aprueba el presupuesto
     * @param hashService Servicio para calcular los hashes criptográficos
     * @throws IllegalArgumentException si approvedBy es nulo o hashService es nulo
     */
    public void aprobar(UUID approvedBy, IntegrityHashService hashService) {
        Objects.requireNonNull(approvedBy, "El ID del usuario que aprueba no puede ser nulo");
        Objects.requireNonNull(hashService, "El servicio de hash no puede ser nulo");

        this.estado = EstadoPresupuesto.CONGELADO;
        this.esContractual = true; // Congelamiento lógico

        // Generate cryptographic seal (dual-hash pattern)
        this.integrityHashApproval = hashService.calculateApprovalHash(this);
        this.integrityHashExecution = hashService.calculateExecutionHash(this);
        this.integrityHashGeneratedAt = LocalDateTime.now();
        this.integrityHashGeneratedBy = approvedBy;
        this.integrityHashAlgorithm = "SHA-256-v1";
    }

    /**
     * Marca el presupuesto como contractual.
     * 
     * **Hard-Freeze Guard:** Si el presupuesto tiene un hash de aprobación
     * establecido, cualquier modificación lanza BudgetIntegrityViolationException.
     */
    public void marcarComoContractual() {
        // Hard-freeze check: no se puede modificar un presupuesto sellado
        if (integrityHashApproval != null) {
            throw new BudgetIntegrityViolationException(this.id, integrityHashApproval, "N/A",
                    "Structure modification attempted");
        }
        this.esContractual = true;
    }

    /**
     * Valida la integridad criptográfica del presupuesto comparando el hash de
     * aprobación actual con el hash almacenado.
     * 
     * Este método recalcula el hash de aprobación usando la estructura actual del
     * presupuesto y lo compara con el hash inmutable almacenado. Si difieren,
     * significa que la estructura del presupuesto ha sido modificada después de la
     * aprobación.
     * 
     * **Uso típico:** - Antes de operaciones críticas (pagos, reportes financieros)
     * - En procesos de auditoría - En validaciones de integridad periódicas
     * 
     * @param hashService Servicio para calcular el hash de aprobación
     * @throws BudgetIntegrityViolationException si el hash calculado no coincide
     *                                           con el almacenado
     */
    public void validarIntegridad(IntegrityHashService hashService) {
        Objects.requireNonNull(hashService, "El servicio de hash no puede ser nulo");

        // Si no hay hash de aprobación, el presupuesto aún no está sellado
        if (integrityHashApproval == null) {
            return; // Not sealed yet, no validation needed
        }

        // Recalculate approval hash with current structure
        String currentHash = hashService.calculateApprovalHash(this);

        // Compare with stored immutable hash
        if (!currentHash.equals(integrityHashApproval)) {
            throw new BudgetIntegrityViolationException(this.id, integrityHashApproval, currentHash,
                    "Tampering detected");
        }
    }

    /**
     * Actualiza el hash de ejecución después de una transacción financiera.
     * 
     * El hash de ejecución es dinámico y refleja el estado financiero actual del
     * presupuesto (consumos, saldos disponibles, movimientos de caja relacionados).
     * Se debe actualizar después de cada operación financiera que afecte el
     * presupuesto:
     * 
     * - Registro de compras - Aprobación de pagos - Ajustes presupuestarios -
     * Cualquier movimiento que afecte saldos o consumos
     * 
     * **Precondición:** El presupuesto debe estar aprobado (tener hash de
     * aprobación).
     * 
     * @param hashService Servicio para calcular el hash de ejecución
     * @throws IllegalStateException    si el presupuesto no está aprobado
     * @throws IllegalArgumentException si hashService es nulo
     */
    public void actualizarHashEjecucion(IntegrityHashService hashService) {
        Objects.requireNonNull(hashService, "El servicio de hash no puede ser nulo");

        // Precondition: budget must be approved (sealed)
        if (integrityHashApproval == null) {
            throw new IllegalStateException("Cannot update execution hash before approval");
        }

        // Update dynamic execution hash with current financial state
        this.integrityHashExecution = hashService.calculateExecutionHash(this);
    }

    // Getters

    public PresupuestoId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoPresupuesto getEstado() {
        return estado;
    }

    public Boolean getEsContractual() {
        return esContractual;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isAprobado() {
        return estado == EstadoPresupuesto.CONGELADO;
    }

    public boolean isContractual() {
        return esContractual != null && esContractual;
    }

    // Integrity Hash Getters

    /**
     * Obtiene el hash criptográfico inmutable de aprobación.
     * 
     * @return El hash de aprobación (64 caracteres hex) o null si no está aprobado
     */
    public String getIntegrityHashApproval() {
        return integrityHashApproval;
    }

    /**
     * Obtiene el hash criptográfico dinámico de ejecución.
     * 
     * @return El hash de ejecución (64 caracteres hex) o null si no está aprobado
     */
    public String getIntegrityHashExecution() {
        return integrityHashExecution;
    }

    /**
     * Obtiene el timestamp de cuando se generaron los hashes de integridad.
     * 
     * @return La fecha y hora de generación o null si no está aprobado
     */
    public LocalDateTime getIntegrityHashGeneratedAt() {
        return integrityHashGeneratedAt;
    }

    /**
     * Obtiene el ID del usuario que aprobó el presupuesto y generó los hashes.
     * 
     * @return El ID del usuario aprobador o null si no está aprobado
     */
    public UUID getIntegrityHashGeneratedBy() {
        return integrityHashGeneratedBy;
    }

    /**
     * Obtiene la versión del algoritmo de hash usado.
     * 
     * @return El algoritmo usado (ej: "SHA-256-v1") o null si no está aprobado
     */
    public String getIntegrityHashAlgorithm() {
        return integrityHashAlgorithm;
    }

    /**
     * Verifica si el presupuesto tiene un sello de integridad (hash de aprobación).
     * 
     * @return true si el presupuesto está sellado criptográficamente, false en caso
     *         contrario
     */
    public boolean isSealed() {
        return integrityHashApproval != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Presupuesto that = (Presupuesto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Presupuesto{id=%s, proyectoId=%s, nombre='%s', estado=%s, esContractual=%s, version=%d, sealed=%s}",
                id, proyectoId, nombre, estado, esContractual, version, isSealed());
    }
}
