package com.budgetpro.domain.finanzas.partida;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.exception.PresupuestoExcedidoException;
import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado PARTIDA (Budget Item).
 * 
 * Representa una partida presupuestaria de un proyecto con sus saldos
 * (presupuestado, reservado, ejecutado).
 * 
 * Contexto: Presupuestos & APUs
 * 
 * Invariantes Críticas:
 * - El saldo disponible = Presupuestado - (Reservado + Ejecutado) NUNCA puede ser negativo
 * - El monto presupuestado no puede ser negativo
 * - Los montos reservado y ejecutado no pueden ser negativos
 * - El código de la partida no puede ser nulo ni vacío
 * 
 * Lifecycle:
 * - Creación: Usa Partida.crear(...) - estado BORRADOR por defecto
 * - Reconstrucción: Usa Partida.reconstruir(...) - carga desde BD
 */
public final class Partida {

    private final PartidaId id;
    private final UUID proyectoId;
    private final UUID presupuestoId;
    private final CodigoPartida codigo;
    private String nombre;
    private TipoRecurso tipo;
    
    // Saldos presupuestarios
    private Monto montoPresupuestado;
    private Monto montoReservado;
    private Monto montoEjecutado;
    
    private EstadoPartida estado;
    
    /**
     * Campo version para Optimistic Locking.
     * Se establece SOLO en reconstruir() al cargar desde BD.
     * En crear() se inicializa en null (el repositorio lo maneja al insertar).
     */
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     * 
     * @param esReconstruccion Si es true, no valida invariantes de creación y version es obligatorio.
     */
    private Partida(PartidaId id,
                   UUID proyectoId,
                   UUID presupuestoId,
                   CodigoPartida codigo,
                   String nombre,
                   TipoRecurso tipo,
                   Monto montoPresupuestado,
                   Monto montoReservado,
                   Monto montoEjecutado,
                   EstadoPartida estado,
                   Long version,
                   boolean esReconstruccion) {
        this.id = Objects.requireNonNull(id, "El ID de la partida no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código de la partida no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la partida no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de la partida no puede ser nulo");
        
        // Inicializar saldos
        this.montoPresupuestado = montoPresupuestado != null ? montoPresupuestado : Monto.cero();
        this.montoReservado = montoReservado != null ? montoReservado : Monto.cero();
        this.montoEjecutado = montoEjecutado != null ? montoEjecutado : Monto.cero();
        
        // Estado por defecto: BORRADOR si es creación nueva
        this.estado = estado != null ? estado : EstadoPartida.BORRADOR;
        
        // Version: null para creación nueva, obligatorio para reconstrucción
        if (esReconstruccion) {
            this.version = Objects.requireNonNull(version, "La versión es obligatoria al reconstruir desde BD");
        } else {
            this.version = version; // null en creación nueva, el repositorio lo maneja
        }
        
        // Validar invariantes solo si no es reconstrucción
        if (!esReconstruccion) {
            validarInvariantes();
        }
    }

    /**
     * Factory method para crear una nueva Partida con estado BORRADOR.
     * 
     * @param proyectoId El ID del proyecto
     * @param presupuestoId El ID del presupuesto
     * @param codigo El código de la partida (ej: "MAT-01")
     * @param nombre El nombre/descripción de la partida
     * @param tipo El tipo de recurso (MATERIAL, MANO_OBRA, etc)
     * @param montoPresupuestado El monto presupuestado inicial
     * @return Una nueva instancia de Partida con estado BORRADOR
     */
    public static Partida crear(UUID proyectoId,
                               UUID presupuestoId,
                               CodigoPartida codigo,
                               String nombre,
                               TipoRecurso tipo,
                               Monto montoPresupuestado) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        Objects.requireNonNull(codigo, "El código no puede ser nulo");
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        Objects.requireNonNull(montoPresupuestado, "El monto presupuestado no puede ser nulo");
        
        PartidaId id = PartidaId.generate();
        return new Partida(id, proyectoId, presupuestoId, codigo, nombre, tipo,
                          montoPresupuestado, Monto.cero(), Monto.cero(),
                          EstadoPartida.BORRADOR, null, false);
    }

    /**
     * Factory method para crear una nueva Partida con ID específico.
     * Útil para tests o cuando el ID viene de fuera.
     */
    public static Partida crear(PartidaId id,
                               UUID proyectoId,
                               UUID presupuestoId,
                               CodigoPartida codigo,
                               String nombre,
                               TipoRecurso tipo,
                               Monto montoPresupuestado) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        Objects.requireNonNull(codigo, "El código no puede ser nulo");
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        Objects.requireNonNull(montoPresupuestado, "El monto presupuestado no puede ser nulo");
        
        return new Partida(id, proyectoId, presupuestoId, codigo, nombre, tipo,
                          montoPresupuestado, Monto.cero(), Monto.cero(),
                          EstadoPartida.BORRADOR, null, false);
    }

    /**
     * Factory method para reconstruir una Partida desde persistencia.
     * 
     * IMPORTANTE: Este método NO valida invariantes de creación.
     * Se usa cuando se carga el agregado desde la base de datos.
     * 
     * @param id El ID de la partida
     * @param proyectoId El ID del proyecto
     * @param presupuestoId El ID del presupuesto
     * @param codigo El código de la partida
     * @param nombre El nombre/descripción
     * @param tipo El tipo de recurso
     * @param montoPresupuestado El monto presupuestado
     * @param montoReservado El monto reservado
     * @param montoEjecutado El monto ejecutado
     * @param estado El estado de la partida
     * @param version La versión para optimistic locking
     * @return Una instancia reconstruida sin validaciones de creación
     */
    public static Partida reconstruir(PartidaId id,
                                     UUID proyectoId,
                                     UUID presupuestoId,
                                     CodigoPartida codigo,
                                     String nombre,
                                     TipoRecurso tipo,
                                     Monto montoPresupuestado,
                                     Monto montoReservado,
                                     Monto montoEjecutado,
                                     EstadoPartida estado,
                                     Long version) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        Objects.requireNonNull(codigo, "El código no puede ser nulo");
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        Objects.requireNonNull(montoPresupuestado, "El monto presupuestado no puede ser nulo");
        Objects.requireNonNull(montoReservado, "El monto reservado no puede ser nulo");
        Objects.requireNonNull(montoEjecutado, "El monto ejecutado no puede ser nulo");
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        Objects.requireNonNull(version, "La versión no puede ser nula");
        
        return new Partida(id, proyectoId, presupuestoId, codigo, nombre, tipo,
                          montoPresupuestado, montoReservado, montoEjecutado,
                          estado, version, true);
    }

    /**
     * Reserva un monto del presupuesto disponible.
     * 
     * La reserva aumenta el monto reservado y reduce el disponible.
     * Si la operación resultaría en saldo disponible negativo, lanza PresupuestoExcedidoException.
     * 
     * @param monto El monto a reservar (debe ser positivo)
     * @throws IllegalArgumentException si el monto no es positivo
     * @throws PresupuestoExcedidoException si el saldo disponible resultante sería negativo
     */
    public void reservar(Monto monto) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            throw new IllegalArgumentException("El monto a reservar debe ser positivo");
        }
        
        Monto nuevoReservado = this.montoReservado.sumar(monto);
        Monto saldoDisponible = calcularSaldoDisponible(this.montoPresupuestado, nuevoReservado, this.montoEjecutado);
        
        if (saldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                this.id, this.proyectoId,
                this.montoPresupuestado, nuevoReservado, this.montoEjecutado,
                saldoDisponible, monto
            );
        }
        
        this.montoReservado = nuevoReservado;
    }

    /**
     * Libera un monto previamente reservado.
     * 
     * La liberación reduce el monto reservado y aumenta el disponible.
     * 
     * @param monto El monto a liberar (debe ser positivo y no exceder el reservado)
     * @throws IllegalArgumentException si el monto no es positivo o excede el reservado
     */
    public void liberar(Monto monto) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            throw new IllegalArgumentException("El monto a liberar debe ser positivo");
        }
        
        if (monto.esMayorQue(this.montoReservado)) {
            throw new IllegalArgumentException(
                String.format("El monto a liberar (%s) no puede exceder el monto reservado (%s)",
                             monto, this.montoReservado)
            );
        }
        
        this.montoReservado = this.montoReservado.restar(monto);
    }

    /**
     * Ejecuta (consume) un monto del presupuesto disponible.
     * 
     * La ejecución aumenta el monto ejecutado y reduce el disponible.
     * Si la operación resultaría en saldo disponible negativo, lanza PresupuestoExcedidoException.
     * 
     * @param monto El monto a ejecutar (debe ser positivo)
     * @throws IllegalArgumentException si el monto no es positivo
     * @throws PresupuestoExcedidoException si el saldo disponible resultante sería negativo
     */
    public void ejecutar(Monto monto) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            throw new IllegalArgumentException("El monto a ejecutar debe ser positivo");
        }
        
        Monto nuevoEjecutado = this.montoEjecutado.sumar(monto);
        Monto saldoDisponible = calcularSaldoDisponible(this.montoPresupuestado, this.montoReservado, nuevoEjecutado);
        
        if (saldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                this.id, this.proyectoId,
                this.montoPresupuestado, this.montoReservado, nuevoEjecutado,
                saldoDisponible, monto
            );
        }
        
        this.montoEjecutado = nuevoEjecutado;
    }

    /**
     * Calcula el saldo disponible: Presupuestado - (Reservado + Ejecutado)
     * 
     * Esta es la invariante crítica del agregado.
     */
    private Monto calcularSaldoDisponible(Monto presupuestado, Monto reservado, Monto ejecutado) {
        Monto totalComprometido = reservado.sumar(ejecutado);
        return presupuestado.restar(totalComprometido);
    }

    /**
     * Obtiene el saldo disponible actual.
     * 
     * @return El monto disponible (Presupuestado - (Reservado + Ejecutado))
     */
    public Monto getSaldoDisponible() {
        return calcularSaldoDisponible(this.montoPresupuestado, this.montoReservado, this.montoEjecutado);
    }

    /**
     * Verifica si hay saldo disponible suficiente para un monto.
     * 
     * @param monto El monto a verificar
     * @return true si hay saldo suficiente, false en caso contrario
     */
    public boolean tieneSaldoDisponible(Monto monto) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            return false;
        }
        Monto saldoDisponible = getSaldoDisponible();
        return saldoDisponible.esMayorOIgualQue(monto);
    }

    /**
     * Actualiza el monto presupuestado.
     * 
     * Si el nuevo monto presupuestado resultaría en saldo disponible negativo, lanza PresupuestoExcedidoException.
     * 
     * @param nuevoMonto El nuevo monto presupuestado (debe ser positivo)
     * @throws IllegalArgumentException si el monto no es positivo
     * @throws PresupuestoExcedidoException si el saldo disponible resultante sería negativo
     */
    public void actualizarPresupuesto(Monto nuevoMonto) {
        if (nuevoMonto == null || nuevoMonto.esNegativo()) {
            throw new IllegalArgumentException("El monto presupuestado no puede ser negativo");
        }
        
        Monto saldoDisponible = calcularSaldoDisponible(nuevoMonto, this.montoReservado, this.montoEjecutado);
        
        if (saldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                this.id, this.proyectoId,
                nuevoMonto, this.montoReservado, this.montoEjecutado,
                saldoDisponible, nuevoMonto
            );
        }
        
        this.montoPresupuestado = nuevoMonto;
    }

    /**
     * Aprobar la partida.
     * Cambia el estado de BORRADOR a APROBADA.
     * 
     * @throws IllegalStateException si el estado actual no permite la aprobación
     */
    public void aprobar() {
        if (this.estado != EstadoPartida.BORRADOR) {
            throw new IllegalStateException(
                String.format("Solo se puede aprobar una partida en estado BORRADOR. Estado actual: %s", this.estado)
            );
        }
        this.estado = EstadoPartida.APROBADA;
    }

    /**
     * Cerrar la partida.
     * Cambia el estado a CERRADA.
     * 
     * @throws IllegalStateException si el estado actual no permite el cierre
     */
    public void cerrar() {
        if (this.estado == EstadoPartida.CERRADA) {
            throw new IllegalStateException("La partida ya está cerrada");
        }
        this.estado = EstadoPartida.CERRADA;
    }

    /**
     * Valida las invariantes críticas del agregado.
     * 
     * @throws IllegalStateException si alguna invariante es violada
     */
    private void validarInvariantes() {
        // Invariante 1: El monto presupuestado no puede ser negativo
        if (this.montoPresupuestado.esNegativo()) {
            throw new IllegalStateException("El monto presupuestado no puede ser negativo");
        }
        
        // Invariante 2: Los montos reservado y ejecutado no pueden ser negativos
        if (this.montoReservado.esNegativo()) {
            throw new IllegalStateException("El monto reservado no puede ser negativo");
        }
        if (this.montoEjecutado.esNegativo()) {
            throw new IllegalStateException("El monto ejecutado no puede ser negativo");
        }
        
        // Invariante 3: El saldo disponible nunca puede ser negativo
        Monto saldoDisponible = getSaldoDisponible();
        if (saldoDisponible.esNegativo()) {
            throw new IllegalStateException(
                String.format("El saldo disponible no puede ser negativo. Disponible: %s", saldoDisponible)
            );
        }
        
        // Invariante 4: El nombre no puede estar vacío
        if (this.nombre.isBlank()) {
            throw new IllegalStateException("El nombre de la partida no puede estar vacío");
        }
    }

    // Getters

    public PartidaId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public CodigoPartida getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public Monto getMontoPresupuestado() {
        return montoPresupuestado;
    }

    public Monto getMontoReservado() {
        return montoReservado;
    }

    public Monto getMontoEjecutado() {
        return montoEjecutado;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public Long getVersion() {
        return version;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la partida no puede ser nulo ni vacío");
        }
        this.nombre = nombre;
    }

    public void setTipo(TipoRecurso tipo) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de la partida no puede ser nulo");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Partida partida = (Partida) o;
        return Objects.equals(id, partida.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Partida{id=%s, codigo=%s, nombre='%s', presupuestado=%s, reservado=%s, ejecutado=%s, disponible=%s, estado=%s}",
                           id, codigo, nombre, montoPresupuestado, montoReservado, montoEjecutado, getSaldoDisponible(), estado);
    }
}
