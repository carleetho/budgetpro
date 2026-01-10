package com.budgetpro.domain.finanzas.billetera;

import com.budgetpro.domain.finanzas.billetera.exception.SaldoInsuficienteException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado BILLETERA.
 * 
 * Representa la billetera de un proyecto con su saldo actual y movimientos de fondos.
 * 
 * Invariantes Críticas:
 * - El saldo NUNCA puede ser negativo (validado en egresar)
 * - El saldo no se edita manualmente; es el resultado de ingresos y egresos
 * - Todo cambio genera un Movimiento
 * - No existe dinero sin movimiento
 * 
 * Contexto: Finanzas Operativas
 * 
 * Este agregado es ultra-auditable: todo movimiento queda registrado.
 * 
 * Lifecycle:
 * - Creación: Usa Billetera.crear(UUID proyectoId) - dispara evento BilleteraCreada
 * - Reconstrucción: Usa Billetera.reconstruir(...) - NO dispara eventos
 */
public final class Billetera {

    private final BilleteraId id;
    private final UUID proyectoId;
    private Monto saldoActual;
    
    /**
     * Campo version para Optimistic Locking.
     * 
     * REGLAS:
     * - Se establece SOLO en reconstruir() al cargar desde BD
     * - En crear() se inicializa en null (el repositorio lo maneja al insertar)
     * - NO se modifica en ingresar() o egresar() (el repositorio lo incrementa tras persistir)
     */
    private Long version;

    // Lista de movimientos nuevos pendientes de persistir
    private final List<Movimiento> movimientosNuevos;
    
    // Lista de eventos de dominio pendientes de publicar
    private final List<DomainEvent> eventos;

    /**
     * Constructor privado. Usar factory methods.
     * 
     * @param esReconstruccion Si es true, no dispara eventos de creación y version es obligatorio.
     */
    private Billetera(BilleteraId id, UUID proyectoId, Monto saldoActual, Long version, boolean esReconstruccion) {
        this.id = Objects.requireNonNull(id, "El ID de la billetera no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.saldoActual = saldoActual != null ? saldoActual : Monto.cero();
        
        // Version: null para creación nueva, obligatorio para reconstrucción
        if (esReconstruccion) {
            this.version = Objects.requireNonNull(version, "La versión es obligatoria al reconstruir desde BD");
        } else {
            this.version = version; // null en creación nueva, el repositorio lo maneja
        }
        
        this.movimientosNuevos = new ArrayList<>();
        this.eventos = new ArrayList<>();
        
        // Solo dispara evento si es una creación nueva, no una reconstrucción
        if (!esReconstruccion) {
            this.eventos.add(new BilleteraCreada(id, proyectoId));
        }
    }

    /**
     * Factory method para crear una nueva Billetera con saldo inicial en ZERO.
     * 
     * Dispara el evento BilleteraCreada.
     * Version se inicializa en null - el repositorio lo establecerá al insertar en BD.
     * 
     * @param proyectoId El ID del proyecto al que pertenece la billetera
     * @return Una nueva instancia de Billetera con saldo ZERO y version null
     */
    public static Billetera crear(UUID proyectoId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        BilleteraId id = BilleteraId.generate();
        return new Billetera(id, proyectoId, Monto.cero(), null, false);
    }

    /**
     * Factory method para crear una nueva Billetera con ID específico.
     * Útil para tests o cuando el ID viene de fuera.
     * 
     * Dispara el evento BilleteraCreada.
     * Version se inicializa en null - el repositorio lo establecerá al insertar en BD.
     * 
     * @param id El ID de la billetera
     * @param proyectoId El ID del proyecto al que pertenece la billetera
     * @return Una nueva instancia de Billetera con saldo ZERO y version null
     */
    public static Billetera crear(BilleteraId id, UUID proyectoId) {
        Objects.requireNonNull(id, "El ID de la billetera no puede ser nulo");
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        return new Billetera(id, proyectoId, Monto.cero(), null, false);
    }

    /**
     * Factory method para reconstruir una Billetera desde persistencia.
     * 
     * IMPORTANTE: Este método NO dispara eventos de dominio.
     * Se usa cuando se carga el agregado desde la base de datos.
     * 
     * @param id El ID de la billetera
     * @param proyectoId El ID del proyecto
     * @param saldoActual El saldo actual (debe venir como Monto)
     * @param version La versión para optimistic locking
     * @return Una instancia reconstruida sin eventos
     */
    public static Billetera reconstruir(BilleteraId id, UUID proyectoId, Monto saldoActual, Long version) {
        Objects.requireNonNull(id, "El ID de la billetera no puede ser nulo");
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        Objects.requireNonNull(saldoActual, "El saldo actual no puede ser nulo");
        Objects.requireNonNull(version, "La versión no puede ser nula");
        return new Billetera(id, proyectoId, saldoActual, version, true);
    }

    /**
     * Registra un INGRESO de dinero en la billetera.
     * 
     * Crea un movimiento de tipo INGRESO, suma el monto al saldo actual
     * y dispara el evento FondosIngresados.
     * 
     * @param monto El monto a ingresar (debe ser positivo)
     * @param referencia Descripción o referencia del ingreso (no puede estar vacía)
     * @param evidenciaUrl URL opcional de evidencia documental
     * @return El Movimiento creado
     * @throws IllegalArgumentException si el monto no es positivo o la referencia está vacía
     */
    public Movimiento ingresar(Monto monto, String referencia, String evidenciaUrl) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            throw new IllegalArgumentException("El monto del ingreso debe ser positivo");
        }
        
        Movimiento movimiento = Movimiento.crearIngreso(this.id, monto, referencia, evidenciaUrl);
        
        this.saldoActual = this.saldoActual.sumar(monto);
        // NOTA: version NO se incrementa aquí. El repositorio lo maneja tras persistir (Optimistic Locking).
        this.movimientosNuevos.add(movimiento);
        
        // Disparar evento de dominio
        this.eventos.add(new FondosIngresados(this.id, monto, referencia, evidenciaUrl));
        
        return movimiento;
    }

    /**
     * Registra un EGRESO de dinero en la billetera.
     * 
     * Crea un movimiento de tipo EGRESO, resta el monto del saldo actual
     * y dispara el evento FondosEgresados.
     * 
     * INVARIANTE CRÍTICA: Si el saldo resultante sería negativo, lanza SaldoInsuficienteException.
     * 
     * @param monto El monto a egresar (debe ser positivo)
     * @param referencia Descripción o referencia del egreso (no puede estar vacía)
     * @param evidenciaUrl URL opcional de evidencia documental
     * @return El Movimiento creado
     * @throws IllegalArgumentException si el monto no es positivo o la referencia está vacía
     * @throws SaldoInsuficienteException si el saldo resultante sería negativo
     */
    public Movimiento egresar(Monto monto, String referencia, String evidenciaUrl) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            throw new IllegalArgumentException("El monto del egreso debe ser positivo");
        }
        
        // INVARIANTE CRÍTICA: Validar que el saldo no quede negativo
        Monto saldoResultante = this.saldoActual.restar(monto);
        if (saldoResultante.esNegativo()) {
            throw new SaldoInsuficienteException(this.proyectoId, this.saldoActual, monto);
        }
        
        Movimiento movimiento = Movimiento.crearEgreso(this.id, monto, referencia, evidenciaUrl);
        
        this.saldoActual = saldoResultante;
        // NOTA: version NO se incrementa aquí. El repositorio lo maneja tras persistir (Optimistic Locking).
        this.movimientosNuevos.add(movimiento);
        
        // Disparar evento de dominio
        this.eventos.add(new FondosEgresados(this.id, monto, referencia, evidenciaUrl));
        
        return movimiento;
    }

    /**
     * Obtiene los movimientos nuevos pendientes de persistir.
     * Después de persistir, esta lista debe ser limpiada.
     * 
     * @return Lista inmutable de movimientos nuevos
     */
    public List<Movimiento> getMovimientosNuevos() {
        return Collections.unmodifiableList(movimientosNuevos);
    }

    /**
     * Limpia la lista de movimientos nuevos después de persistir.
     * Debe ser llamado por el repositorio después de guardar exitosamente.
     */
    public void limpiarMovimientosNuevos() {
        this.movimientosNuevos.clear();
    }

    /**
     * Obtiene los eventos de dominio pendientes de publicar.
     * Después de publicar, esta lista debe ser limpiada.
     * 
     * @return Lista inmutable de eventos
     */
    public List<DomainEvent> getEventos() {
        return Collections.unmodifiableList(eventos);
    }

    /**
     * Limpia la lista de eventos después de publicarlos.
     * Debe ser llamado por el mecanismo de publicación de eventos.
     */
    public void limpiarEventos() {
        this.eventos.clear();
    }

    // Getters

    public BilleteraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public Monto getSaldoActual() {
        return saldoActual;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Verifica si la billetera tiene saldo suficiente para un egreso.
     * 
     * @param monto El monto a verificar
     * @return true si hay saldo suficiente, false en caso contrario
     */
    public boolean tieneSaldoSuficiente(Monto monto) {
        if (monto == null || monto.esCero() || monto.esNegativo()) {
            return false;
        }
        Monto saldoResultante = this.saldoActual.restar(monto);
        return !saldoResultante.esNegativo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Billetera billetera = (Billetera) o;
        return Objects.equals(id, billetera.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Billetera{id=%s, proyectoId=%s, saldoActual=%s, version=%s}", 
                           id, proyectoId, saldoActual, version);
    }
}
