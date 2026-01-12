package com.budgetpro.domain.finanzas.partida;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.exception.PresupuestoExcedidoException;
import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado PRESUPUESTO.
 * 
 * Representa una partida presupuestaria con control de saldos (presupuestado, reservado, ejecutado).
 * 
 * REGLA DE DDD: Esta entidad NO es un agregado raíz. Pertenece al agregado Presupuesto y
 * se accede exclusivamente a través del agregado raíz (Presupuesto).
 * 
 * Invariantes Críticas:
 * 1. Saldo Disponible nunca negativo: Presupuestado - (Reservado + Ejecutado) >= 0
 * 2. Monto presupuestado no puede ser negativo
 * 3. Montos reservado y ejecutado no pueden ser negativos
 * 4. Código no puede ser nulo ni vacío
 * 5. Nombre no puede estar vacío
 * 
 * Contexto: Presupuestos & APUs
 */
public final class Partida {

    private final PartidaId id;
    private final UUID proyectoId;
    private final UUID presupuestoId;
    private final CodigoPartida codigo;
    private String nombre;
    private TipoRecurso tipo;
    private Monto montoPresupuestado;
    private Monto montoReservado;
    private Monto montoEjecutado;
    private EstadoPartida estado;
    private Long version;
    
    // WBS Jerárquico (según Directiva Maestra v2.0)
    private final PartidaId parentId; // null para partidas raíz
    private final int nivel; // 1 para raíz, incrementa para cada nivel

    /**
     * Constructor privado. Usar factory methods.
     */
    private Partida(PartidaId id, UUID proyectoId, UUID presupuestoId, CodigoPartida codigo,
                   String nombre, TipoRecurso tipo, Monto montoPresupuestado,
                   Monto montoReservado, Monto montoEjecutado, EstadoPartida estado, Long version,
                   PartidaId parentId, int nivel, boolean validar) {
        this.id = Objects.requireNonNull(id, "El ID de la partida no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código de la partida no puede ser nulo");
        
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la partida no puede estar vacío");
        }
        this.nombre = nombre.trim();
        
        this.tipo = Objects.requireNonNull(tipo, "El tipo de la partida no puede ser nulo");
        this.montoPresupuestado = Objects.requireNonNull(montoPresupuestado, "El monto presupuestado no puede ser nulo");
        this.montoReservado = montoReservado != null ? montoReservado : Monto.cero();
        this.montoEjecutado = montoEjecutado != null ? montoEjecutado : Monto.cero();
        this.estado = estado != null ? estado : EstadoPartida.BORRADOR;
        this.version = version != null ? version : 0L;
        
        // WBS: parentId puede ser null (partida raíz)
        this.parentId = parentId;
        
        // WBS: nivel debe ser positivo
        if (nivel < 1) {
            throw new IllegalArgumentException("El nivel de la partida debe ser mayor o igual a 1");
        }
        this.nivel = nivel;
        
        // Validar invariantes solo si es creación nueva (no reconstrucción desde BD)
        if (validar) {
            validarInvariantes();
        }
    }

    /**
     * Factory method para crear una nueva Partida raíz (sin padre) con estado BORRADOR por defecto.
     */
    public static Partida crear(UUID proyectoId, UUID presupuestoId, CodigoPartida codigo,
                               String nombre, TipoRecurso tipo, Monto montoPresupuestado) {
        return new Partida(
            PartidaId.generate(),
            proyectoId,
            presupuestoId,
            codigo,
            nombre,
            tipo,
            montoPresupuestado,
            Monto.cero(),
            Monto.cero(),
            EstadoPartida.BORRADOR,
            0L,
            null, // parentId: null para partida raíz
            1,    // nivel: 1 para partida raíz
            true // Validar invariantes en creación
        );
    }
    
    /**
     * Factory method para crear una nueva Partida hija (con padre) con estado BORRADOR por defecto.
     * 
     * @param padre La partida padre (no puede ser nula)
     * @param codigo El código de la partida hija
     * @param nombre El nombre de la partida hija
     * @param tipo El tipo de recurso
     * @param montoPresupuestado El monto presupuestado
     * @return La partida hija creada
     */
    public static Partida crearHija(Partida padre, CodigoPartida codigo,
                                   String nombre, TipoRecurso tipo, Monto montoPresupuestado) {
        Objects.requireNonNull(padre, "La partida padre no puede ser nula");
        
        return new Partida(
            PartidaId.generate(),
            padre.proyectoId,
            padre.presupuestoId,
            codigo,
            nombre,
            tipo,
            montoPresupuestado,
            Monto.cero(),
            Monto.cero(),
            EstadoPartida.BORRADOR,
            0L,
            padre.id,        // parentId: ID del padre
            padre.nivel + 1, // nivel: nivel del padre + 1
            true // Validar invariantes en creación
        );
    }

    /**
     * Factory method para crear una Partida con ID específico.
     */
    public static Partida crear(PartidaId id, UUID proyectoId, UUID presupuestoId, CodigoPartida codigo,
                               String nombre, TipoRecurso tipo, Monto montoPresupuestado) {
        return new Partida(
            id,
            proyectoId,
            presupuestoId,
            codigo,
            nombre,
            tipo,
            montoPresupuestado,
            Monto.cero(),
            Monto.cero(),
            EstadoPartida.BORRADOR,
            0L,
            null, // parentId: null para partida raíz
            1,    // nivel: 1 para partida raíz
            true // Validar invariantes en creación
        );
    }

    /**
     * Factory method para reconstruir una Partida desde persistencia.
     * NO valida invariantes de creación (permite estados que podrían ser inválidos al crear).
     */
    public static Partida reconstruir(PartidaId id, UUID proyectoId, UUID presupuestoId, CodigoPartida codigo,
                                     String nombre, TipoRecurso tipo, Monto montoPresupuestado,
                                     Monto montoReservado, Monto montoEjecutado, EstadoPartida estado, Long version,
                                     PartidaId parentId, int nivel) {
        return new Partida(
            id,
            proyectoId,
            presupuestoId,
            codigo,
            nombre,
            tipo,
            montoPresupuestado,
            montoReservado,
            montoEjecutado,
            estado,
            version,
            parentId,
            nivel,
            true // Validar invariantes incluso en reconstrucción (saldo disponible nunca negativo)
        );
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes() {
        // Invariante 1: Monto presupuestado no puede ser negativo
        if (montoPresupuestado.esNegativo()) {
            throw new IllegalArgumentException("El monto presupuestado no puede ser negativo");
        }

        // Invariante 2: Montos reservado y ejecutado no pueden ser negativos
        if (montoReservado.esNegativo()) {
            throw new IllegalArgumentException("El monto reservado no puede ser negativo");
        }
        if (montoEjecutado.esNegativo()) {
            throw new IllegalArgumentException("El monto ejecutado no puede ser negativo");
        }

        // Invariante 3: Saldo disponible nunca negativo (INVARIANTE CRÍTICA)
        Monto saldoDisponible = calcularSaldoDisponible();
        if (saldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                id, proyectoId, montoPresupuestado, montoReservado,
                montoEjecutado, saldoDisponible, Monto.cero()
            );
        }
    }

    /**
     * Calcula el saldo disponible: Presupuestado - (Reservado + Ejecutado).
     */
    private Monto calcularSaldoDisponible() {
        Monto totalComprometido = montoReservado.sumar(montoEjecutado);
        return montoPresupuestado.restar(totalComprometido);
    }

    /**
     * Obtiene el saldo disponible de la partida.
     */
    public Monto getSaldoDisponible() {
        return calcularSaldoDisponible();
    }

    /**
     * Verifica si la partida tiene saldo suficiente para un monto dado.
     */
    public boolean tieneSaldoDisponible(Monto monto) {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        Monto saldoDisponible = calcularSaldoDisponible();
        return saldoDisponible.esMayorOIgualQue(monto);
    }

    /**
     * Reserva un monto en la partida (aumenta el monto reservado).
     * 
     * INVARIANTE: Si el saldo disponible resultante sería negativo, lanza PresupuestoExcedidoException.
     */
    public void reservar(Monto monto) {
        Objects.requireNonNull(monto, "El monto a reservar no puede ser nulo");
        
        if (monto.esNegativo() || monto.esCero()) {
            throw new IllegalArgumentException("El monto a reservar debe ser positivo");
        }

        // Calcular nuevo monto reservado
        Monto nuevoReservado = montoReservado.sumar(monto);
        
        // Validar invariante: Saldo disponible nunca negativo
        Monto nuevoSaldoDisponible = montoPresupuestado.restar(nuevoReservado.sumar(montoEjecutado));
        if (nuevoSaldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                id, proyectoId, montoPresupuestado, nuevoReservado,
                montoEjecutado, nuevoSaldoDisponible, monto
            );
        }

        this.montoReservado = nuevoReservado;
    }

    /**
     * Libera un monto previamente reservado (reduce el monto reservado).
     */
    public void liberar(Monto monto) {
        Objects.requireNonNull(monto, "El monto a liberar no puede ser nulo");
        
        if (monto.esNegativo() || monto.esCero()) {
            throw new IllegalArgumentException("El monto a liberar debe ser positivo");
        }

        // Calcular nuevo monto reservado
        Monto nuevoReservado = montoReservado.restar(monto);
        
        // Validar que no quede negativo
        if (nuevoReservado.esNegativo()) {
            throw new IllegalArgumentException(
                String.format("No se puede liberar %s. Monto reservado actual: %s", monto, montoReservado)
            );
        }

        this.montoReservado = nuevoReservado;
    }

    /**
     * Ejecuta un monto en la partida (aumenta el monto ejecutado).
     * 
     * INVARIANTE: Si el saldo disponible resultante sería negativo, lanza PresupuestoExcedidoException.
     */
    public void ejecutar(Monto monto) {
        Objects.requireNonNull(monto, "El monto a ejecutar no puede ser nulo");
        
        if (monto.esNegativo() || monto.esCero()) {
            throw new IllegalArgumentException("El monto a ejecutar debe ser positivo");
        }

        // Calcular nuevo monto ejecutado
        Monto nuevoEjecutado = montoEjecutado.sumar(monto);
        
        // Validar invariante: Saldo disponible nunca negativo
        Monto nuevoSaldoDisponible = montoPresupuestado.restar(montoReservado.sumar(nuevoEjecutado));
        if (nuevoSaldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                id, proyectoId, montoPresupuestado, montoReservado,
                nuevoEjecutado, nuevoSaldoDisponible, monto
            );
        }

        this.montoEjecutado = nuevoEjecutado;
    }

    /**
     * Actualiza el monto presupuestado de la partida.
     * 
     * INVARIANTE: Si el saldo disponible resultante sería negativo, lanza PresupuestoExcedidoException.
     */
    public void actualizarPresupuesto(Monto nuevoMontoPresupuestado) {
        Objects.requireNonNull(nuevoMontoPresupuestado, "El nuevo monto presupuestado no puede ser nulo");
        
        if (nuevoMontoPresupuestado.esNegativo()) {
            throw new IllegalArgumentException("El monto presupuestado no puede ser negativo");
        }

        // Validar invariante: Saldo disponible nunca negativo con nuevo presupuesto
        Monto nuevoSaldoDisponible = nuevoMontoPresupuestado.restar(montoReservado.sumar(montoEjecutado));
        if (nuevoSaldoDisponible.esNegativo()) {
            throw new PresupuestoExcedidoException(
                id, proyectoId, nuevoMontoPresupuestado, montoReservado,
                montoEjecutado, nuevoSaldoDisponible, Monto.cero()
            );
        }

        this.montoPresupuestado = nuevoMontoPresupuestado;
    }

    /**
     * Aprueba la partida (cambia estado de BORRADOR a APROBADA).
     */
    public void aprobar() {
        if (estado != EstadoPartida.BORRADOR) {
            throw new IllegalStateException(
                String.format("Solo se puede aprobar una partida en estado BORRADOR. Estado actual: %s", estado)
            );
        }
        this.estado = EstadoPartida.APROBADA;
    }

    /**
     * Cierra la partida (cambia estado a CERRADA).
     */
    public void cerrar() {
        if (estado == EstadoPartida.CERRADA) {
            throw new IllegalStateException("La partida ya está cerrada");
        }
        this.estado = EstadoPartida.CERRADA;
    }

    // Setters (solo para campos mutables que no afectan invariantes críticas)

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la partida no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public void setTipo(TipoRecurso tipo) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de la partida no puede ser nulo");
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

    public void setVersion(Long version) {
        this.version = version != null ? version : 0L;
    }
    
    // WBS: Getters para jerarquía
    
    public PartidaId getParentId() {
        return parentId;
    }
    
    public int getNivel() {
        return nivel;
    }
    
    /**
     * Verifica si esta partida es una partida raíz (sin padre).
     * 
     * @return true si es raíz, false si es hija
     */
    public boolean esRaiz() {
        return parentId == null;
    }
    
    /**
     * Verifica si esta partida es hija de otra partida.
     * 
     * @return true si tiene padre, false si es raíz
     */
    public boolean esHija() {
        return parentId != null;
    }
    
    /**
     * Calcula el total rollup: monto presupuestado de esta partida más el de todas sus hijas.
     * 
     * NOTA: Este método requiere que el agregado Presupuesto proporcione las partidas hijas
     * mediante un callback o que se calcule externamente. Por ahora, retorna solo el monto
     * de esta partida. El cálculo completo debe hacerse en Presupuesto que tiene acceso a todas las partidas.
     * 
     * @param montoHijas El monto total de las partidas hijas (debe calcularse externamente)
     * @return El monto total (esta partida + hijas)
     */
    public Monto calcularTotalRollup(Monto montoHijas) {
        Objects.requireNonNull(montoHijas, "El monto de hijas no puede ser nulo");
        return montoPresupuestado.sumar(montoHijas);
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
        return "Partida{" +
                "id=" + id +
                ", codigo=" + codigo +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", montoPresupuestado=" + montoPresupuestado +
                ", montoReservado=" + montoReservado +
                ", montoEjecutado=" + montoEjecutado +
                ", estado=" + estado +
                ", version=" + version +
                ", parentId=" + parentId +
                ", nivel=" + nivel +
                '}';
    }
}
