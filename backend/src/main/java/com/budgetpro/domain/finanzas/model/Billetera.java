package com.budgetpro.domain.finanzas.model;

import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado BILLETERA.
 * 
 * Representa la billetera de un proyecto con su saldo actual y movimientos de
 * caja.
 * 
 * Invariantes Críticas: - El saldo NUNCA puede ser negativo (validado en
 * egresar) - El saldo no se edita manualmente; es el resultado de ingresos y
 * egresos - Todo cambio genera un MovimientoCaja - No existe dinero sin
 * movimiento
 * 
 * Contexto: Finanzas Operativas
 * 
 * Este agregado es ultra-auditable: todo movimiento queda registrado.
 */
public final class Billetera {

    /**
     * Máximo número de movimientos pendientes de evidencia permitidos antes de
     * bloquear egresos.
     * 
     * Regla de negocio CD-04: Si hay más de 3 movimientos sin evidencia, se
     * bloquean nuevos egresos hasta que se proporcione evidencia para los
     * movimientos pendientes.
     * 
     * Este umbral puede ser configurado en el futuro, pero por ahora es una
     * constante de dominio.
     */
    private static final int MAX_MOVIMIENTOS_PENDIENTES_EVIDENCIA = 3;

    private final BilleteraId id;
    private final UUID proyectoId;
    private final String moneda;
    private BigDecimal saldoActual;
    private Long version;

    // Lista de movimientos nuevos pendientes de persistir
    private final List<MovimientoCaja> movimientosNuevos;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Billetera(BilleteraId id, UUID proyectoId, String moneda, BigDecimal saldoActual, Long version) {
        this.id = Objects.requireNonNull(id, "El ID de la billetera no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.moneda = Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        this.saldoActual = saldoActual != null ? saldoActual : BigDecimal.ZERO;
        this.version = version != null ? version : 0L;
        this.movimientosNuevos = new ArrayList<>();
    }

    /**
     * Factory method para crear una nueva Billetera con saldo inicial en ZERO.
     */
    public static Billetera crear(BilleteraId id, UUID proyectoId, String moneda) {
        // Validación de moneda debería ser más estricta (ISO-4217), pero por ahora solo
        // not null
        return new Billetera(id, proyectoId, moneda, BigDecimal.ZERO, 0L);
    }

    /**
     * Factory method para crear una nueva Billetera con moneda por defecto (PEN).
     * Mantiene retrocompatibilidad.
     */
    public static Billetera crear(BilleteraId id, UUID proyectoId) {
        return crear(id, proyectoId, "PEN");
    }

    /**
     * Factory method para reconstruir una Billetera desde persistencia.
     */
    public static Billetera reconstruir(BilleteraId id, UUID proyectoId, String moneda, BigDecimal saldoActual,
            Long version) {
        return new Billetera(id, proyectoId, moneda, saldoActual, version);
    }

    /**
     * Factory method para reconstruir (legacy/default moneda).
     */
    public static Billetera reconstruir(BilleteraId id, UUID proyectoId, BigDecimal saldoActual, Long version) {
        return new Billetera(id, proyectoId, "PEN", saldoActual, version);
    }

    /**
     * Registra un INGRESO de dinero en la billetera.
     * 
     * Crea un movimiento de tipo INGRESO y suma el monto al saldo actual.
     * 
     * @param monto        El monto a ingresar (debe ser positivo)
     * @param referencia   Descripción o referencia del ingreso (no puede estar
     *                     vacía)
     * @param evidenciaUrl URL de evidencia documental (obligatoria)
     * @return El MovimientoCaja creado
     * @throws IllegalArgumentException si el monto no es positivo o la referencia
     *                                  está vacía
     */
    public MovimientoCaja ingresar(BigDecimal monto, String moneda, String referencia, String evidenciaUrl) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del ingreso debe ser positivo");
        }
        if (evidenciaUrl == null || evidenciaUrl.isBlank()) {
            throw new IllegalArgumentException("La evidencia del ingreso no puede ser nula ni vacía");
        }

        if (!moneda.equals(this.moneda)) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch: Wallet currency (%s) does not match movement currency (%s)",
                            this.moneda, moneda));
        }

        MovimientoCaja movimiento = MovimientoCaja.crearIngreso(this.id, monto, moneda, referencia, evidenciaUrl);

        this.saldoActual = this.saldoActual.add(monto);
        this.version = this.version + 1;
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Registra un INGRESO de dinero en la billetera (Legacy - Default PEN).
     */
    public MovimientoCaja ingresar(BigDecimal monto, String referencia, String evidenciaUrl) {
        return ingresar(monto, "PEN", referencia, evidenciaUrl);
    }

    /**
     * Registra un EGRESO de dinero en la billetera.
     * 
     * Crea un movimiento de tipo EGRESO y resta el monto del saldo actual.
     * 
     * **CRÍTICO: Validación de Integridad Criptográfica** Antes de permitir el
     * egreso, se valida la integridad del presupuesto para prevenir transacciones
     * sobre presupuestos modificados no autorizadamente.
     * 
     * **Orden de Validaciones:** 1. Validación de monto (debe ser positivo) 2.
     * Validación CD-04: Evidencia pendiente (máximo 3 movimientos sin evidencia) 3.
     * Validación de Integridad 4. Validación de saldo suficiente
     * 
     * INVARIANTE CRÍTICA: Si el saldo resultante sería negativo, lanza
     * SaldoInsuficienteException.
     * 
     * @param monto        El monto a egresar (debe ser positivo)
     * @param moneda       La moneda del egreso
     * @param referencia   Descripción o referencia del egreso (no puede estar
     *                     vacía)
     * @param evidenciaUrl URL opcional de evidencia documental
     * @param presupuesto  El presupuesto del proyecto (para validación de
     *                     integridad)
     * @param hashService  Servicio de hash para validación criptográfica
     * @return El MovimientoCaja creado
     * @throws IllegalArgumentException          si el monto no es positivo o la
     *                                           referencia está vacía
     * @throws IllegalStateException             si se viola la regla CD-04 de
     *                                           evidencia pendiente
     * @throws BudgetIntegrityViolationException si se detecta tampering en el
     *                                           presupuesto
     * @throws SaldoInsuficienteException        si el saldo resultante sería
     *                                           negativo
     */
    public MovimientoCaja egresar(BigDecimal monto, String moneda, String referencia, String evidenciaUrl,
            com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId presupuestoId, boolean isPresupuestoValid) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del egreso debe ser positivo");
        }

        // Validación CD-04: Evidencia pendiente (antes de validar integridad)
        int pendientesEvidencia = contarMovimientosPendientesEvidencia();
        if (pendientesEvidencia > MAX_MOVIMIENTOS_PENDIENTES_EVIDENCIA) {
            throw new IllegalStateException(
                    String.format("No se permiten egresos con más de %d movimientos pendientes de evidencia.",
                            MAX_MOVIMIENTOS_PENDIENTES_EVIDENCIA));
        }
        if ((evidenciaUrl == null || evidenciaUrl.isBlank())
                && pendientesEvidencia >= MAX_MOVIMIENTOS_PENDIENTES_EVIDENCIA) {
            throw new IllegalStateException(
                    String.format("No se permiten más de %d movimientos pendientes de evidencia.",
                            MAX_MOVIMIENTOS_PENDIENTES_EVIDENCIA));
        }

        if (!moneda.equals(this.moneda)) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch: Wallet currency (%s) does not match movement currency (%s)",
                            this.moneda, moneda));
        }

        // CRÍTICO: Validar integridad criptográfica del presupuesto ANTES de permitir
        // egreso
        if (!isPresupuestoValid) {
            throw new BudgetIntegrityViolationException(presupuestoId, "INVALID_HASH",
                    "Tampering detected or budget not approved", "SHA-256-v1");
        }

        // INVARIANTE CRÍTICA: Validar que el saldo no quede negativo
        BigDecimal saldoResultante = this.saldoActual.subtract(monto);
        if (saldoResultante.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException(this.proyectoId, this.saldoActual, monto);
        }

        MovimientoCaja movimiento = MovimientoCaja.crearEgreso(this.id, monto, moneda, referencia, evidenciaUrl);

        this.saldoActual = saldoResultante;
        this.version = this.version + 1;
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Registra un EGRESO de dinero en la billetera (Legacy - Default PEN).
     */
    public MovimientoCaja egresar(BigDecimal monto, String referencia, String evidenciaUrl,
            com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId presupuestoId, boolean isPresupuestoValid) {
        return egresar(monto, "PEN", referencia, evidenciaUrl, presupuestoId, isPresupuestoValid);
    }

    public int contarMovimientosPendientesEvidencia() {
        return (int) movimientosNuevos.stream().filter(MovimientoCaja::isPendienteEvidencia).count();
    }

    /**
     * Obtiene los movimientos nuevos pendientes de persistir. Después de persistir,
     * esta lista debe ser limpiada.
     * 
     * @return Lista inmutable de movimientos nuevos
     */
    public List<MovimientoCaja> getMovimientosNuevos() {
        return List.copyOf(movimientosNuevos);
    }

    /**
     * Limpia la lista de movimientos nuevos después de persistir. Debe ser llamado
     * por el repositorio después de guardar exitosamente.
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

    public String getMoneda() {
        return moneda;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Billetera billetera = (Billetera) o;
        return Objects.equals(id, billetera.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Billetera{id=%s, proyectoId=%s, moneda='%s', saldoActual=%s, version=%d}", id, proyectoId,
                moneda, saldoActual, version);
    }
}
