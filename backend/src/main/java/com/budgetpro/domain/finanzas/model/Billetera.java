package com.budgetpro.domain.finanzas.model;

import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado BILLETERA.
 * 
 * Representa la billetera de un proyecto con su saldo actual y movimientos de caja.
 * 
 * Invariantes Críticas:
 * - El saldo NUNCA puede ser negativo (validado en egresar)
 * - El saldo no se edita manualmente; es el resultado de ingresos y egresos
 * - Todo cambio genera un MovimientoCaja
 * - No existe dinero sin movimiento
 * 
 * Contexto: Finanzas Operativas
 * 
 * Este agregado es ultra-auditable: todo movimiento queda registrado.
 */
public final class Billetera {

    private final BilleteraId id;
    private final UUID proyectoId;
    private BigDecimal saldoActual;
    private Long version;

    // Lista de movimientos nuevos pendientes de persistir
    private final List<MovimientoCaja> movimientosNuevos;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Billetera(BilleteraId id, UUID proyectoId, BigDecimal saldoActual, Long version) {
        this.id = Objects.requireNonNull(id, "El ID de la billetera no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.saldoActual = saldoActual != null ? saldoActual : BigDecimal.ZERO;
        this.version = version != null ? version : 0L;
        this.movimientosNuevos = new ArrayList<>();
    }

    /**
     * Factory method para crear una nueva Billetera con saldo inicial en ZERO.
     */
    public static Billetera crear(BilleteraId id, UUID proyectoId) {
        return new Billetera(id, proyectoId, BigDecimal.ZERO, 0L);
    }

    /**
     * Factory method para reconstruir una Billetera desde persistencia.
     */
    public static Billetera reconstruir(BilleteraId id, UUID proyectoId, BigDecimal saldoActual, Long version) {
        return new Billetera(id, proyectoId, saldoActual, version);
    }

    /**
     * Registra un INGRESO de dinero en la billetera.
     * 
     * Crea un movimiento de tipo INGRESO y suma el monto al saldo actual.
     * 
     * @param monto El monto a ingresar (debe ser positivo)
     * @param referencia Descripción o referencia del ingreso (no puede estar vacía)
     * @param evidenciaUrl URL de evidencia documental (obligatoria)
     * @return El MovimientoCaja creado
     * @throws IllegalArgumentException si el monto no es positivo o la referencia está vacía
     */
    public MovimientoCaja ingresar(BigDecimal monto, String referencia, String evidenciaUrl) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del ingreso debe ser positivo");
        }
        if (evidenciaUrl == null || evidenciaUrl.isBlank()) {
            throw new IllegalArgumentException("La evidencia del ingreso no puede ser nula ni vacía");
        }
        
        MovimientoCaja movimiento = MovimientoCaja.crearIngreso(this.id, monto, referencia, evidenciaUrl);
        
        this.saldoActual = this.saldoActual.add(monto);
        this.version = this.version + 1;
        this.movimientosNuevos.add(movimiento);
        
        return movimiento;
    }

    /**
     * Registra un EGRESO de dinero en la billetera.
     * 
     * Crea un movimiento de tipo EGRESO y resta el monto del saldo actual.
     * 
     * INVARIANTE CRÍTICA: Si el saldo resultante sería negativo, lanza SaldoInsuficienteException.
     * 
     * @param monto El monto a egresar (debe ser positivo)
     * @param referencia Descripción o referencia del egreso (no puede estar vacía)
     * @param evidenciaUrl URL opcional de evidencia documental
     * @return El MovimientoCaja creado
     * @throws IllegalArgumentException si el monto no es positivo o la referencia está vacía
     * @throws SaldoInsuficienteException si el saldo resultante sería negativo
     */
    public MovimientoCaja egresar(BigDecimal monto, String referencia, String evidenciaUrl) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del egreso debe ser positivo");
        }
        
        // INVARIANTE CRÍTICA: Validar que el saldo no quede negativo
        BigDecimal saldoResultante = this.saldoActual.subtract(monto);
        if (saldoResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException(this.proyectoId, this.saldoActual, monto);
        }
        
        MovimientoCaja movimiento = MovimientoCaja.crearEgreso(this.id, monto, referencia, evidenciaUrl);
        
        this.saldoActual = saldoResultante;
        this.version = this.version + 1;
        this.movimientosNuevos.add(movimiento);
        
        return movimiento;
    }

    /**
     * Obtiene los movimientos nuevos pendientes de persistir.
     * Después de persistir, esta lista debe ser limpiada.
     * 
     * @return Lista inmutable de movimientos nuevos
     */
    public List<MovimientoCaja> getMovimientosNuevos() {
        return List.copyOf(movimientosNuevos);
    }

    /**
     * Limpia la lista de movimientos nuevos después de persistir.
     * Debe ser llamado por el repositorio después de guardar exitosamente.
     */
    public void limpiarMovimientosNuevos() {
        this.movimientosNuevos.clear();
    }

    // Getters

    public BilleteraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public BigDecimal getSaldoActual() {
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
    public boolean tieneSaldoSuficiente(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.saldoActual.subtract(monto).compareTo(BigDecimal.ZERO) >= 0;
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
        return String.format("Billetera{id=%s, proyectoId=%s, saldoActual=%s, version=%d}", 
                           id, proyectoId, saldoActual, version);
    }
}
