package com.budgetpro.domain.logistica.inventario.model;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException;
import com.budgetpro.domain.logistica.transferencia.model.TransferenciaId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado INVENTARIO.
 * 
 * Representa el stock físico de un recurso en un proyecto y bodega específica.
 * 
 * Un registro por Proyecto + Recurso + UnidadBase + Bodega (relación única).
 * 
 * Invariantes Críticas: - La cantidadFisica NUNCA puede ser negativa - Todo
 * movimiento genera un registro en el Kardex (MovimientoInventario) - El
 * costoPromedio se calcula ponderadamente cuando hay entradas - No existe stock
 * sin movimiento - Los campos snapshot (nombre, clasificacion, unidadBase) son
 * inmutables después de creación
 */
public final class InventarioItem {

    // A record to hold the result of an inventory operation
    public record InventarioTransaction(InventarioItem inventario, MovimientoInventario movimiento) {
        public InventarioTransaction {
            Objects.requireNonNull(inventario, "El inventario no puede ser nulo");
            Objects.requireNonNull(movimiento, "El movimiento no puede ser nulo");
        }
    }

    // JUSTIFICACIÓN ARQUITECTÓNICA: Aggregate Root inmutable.
    // Todos los campos son final. Las modificaciones retornan nuevas instancias.
    private final InventarioId id;
    private final UUID proyectoId;
    private final UUID recursoId; // Deprecated
    private final String recursoExternalId;
    private final BodegaId bodegaId;
    private final String nombre;
    private final String clasificacion;
    private final String unidadBase;

    private final BigDecimal cantidadFisica; // Stock actual
    private final BigDecimal costoPromedio; // Costo promedio ponderado

    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    @Deprecated
    private final String ubicacion; // DEPRECATED: Usar bodegaId en su lugar. Mantener para compatibilidad.

    private final LocalDateTime ultimaActualizacion;
    private final Long version;

    // Lista de movimientos nuevos pendientes de persistir (Inmutable)
    private final List<MovimientoInventario> movimientosNuevos;

    /**
     * Constructor privado. Usar factory methods.
     */
    private InventarioItem(InventarioId id, UUID proyectoId, UUID recursoId, String recursoExternalId,
            BodegaId bodegaId, String nombre, String clasificacion, String unidadBase, BigDecimal cantidadFisica,
            BigDecimal costoPromedio, String ubicacion, LocalDateTime ultimaActualizacion, Long version,
            List<MovimientoInventario> movimientosNuevos) {
        this.id = Objects.requireNonNull(id, "El ID del inventario no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.recursoId = recursoId; // Puede ser null durante migración
        if (recursoExternalId == null || recursoExternalId.isBlank()) {
            throw new IllegalArgumentException("El recursoExternalId no puede estar en blanco");
        }
        this.recursoExternalId = recursoExternalId.trim();
        this.bodegaId = Objects.requireNonNull(bodegaId, "El bodegaId es obligatorio");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del recurso no puede estar en blanco");
        }
        this.nombre = nombre.trim();
        if (clasificacion == null || clasificacion.isBlank()) {
            throw new IllegalArgumentException("La clasificación no puede estar en blanco");
        }
        this.clasificacion = clasificacion.trim();
        if (unidadBase == null || unidadBase.isBlank()) {
            throw new IllegalArgumentException("La unidad base no puede estar en blanco");
        }
        this.unidadBase = unidadBase.trim();
        this.cantidadFisica = cantidadFisica != null ? cantidadFisica : BigDecimal.ZERO;
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
        this.ubicacion = ubicacion; // DEPRECATED
        this.ultimaActualizacion = ultimaActualizacion != null ? ultimaActualizacion : LocalDateTime.now();
        this.version = version != null ? version : 0L;
        this.movimientosNuevos = movimientosNuevos != null ? List.copyOf(movimientosNuevos) : List.of();

        // Invariante: cantidadFisica no puede ser negativa
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad física no puede ser negativa");
        }
    }

    /**
     * Factory method para crear un nuevo item de inventario con snapshot completo.
     */
    public static InventarioItem crearConSnapshot(InventarioId id, UUID proyectoId, String recursoExternalId,
            BodegaId bodegaId, String nombre, String clasificacion, String unidadBase) {
        return new InventarioItem(id, proyectoId, null, recursoExternalId, bodegaId, nombre, clasificacion, unidadBase,
                BigDecimal.ZERO, BigDecimal.ZERO, null, LocalDateTime.now(), 0L, List.of());
    }

    /**
     * Factory method para crear un nuevo item de inventario (stock inicial en
     * ZERO).
     * 
     * @deprecated Usar crearConSnapshot() en su lugar.
     */
    @Deprecated
    public static InventarioItem crear(InventarioId id, UUID proyectoId, UUID recursoId, String ubicacion) {
        throw new UnsupportedOperationException(
                "Usar crearConSnapshot() con recursoExternalId, bodegaId y campos snapshot. "
                        + "Este método está deprecado y no debe usarse en nuevo código.");
    }

    /**
     * Factory method para reconstruir un item de inventario desde persistencia.
     */
    public static InventarioItem reconstruir(InventarioId id, UUID proyectoId, UUID recursoId, String recursoExternalId,
            BodegaId bodegaId, String nombre, String clasificacion, String unidadBase, BigDecimal cantidadFisica,
            BigDecimal costoPromedio, String ubicacion, LocalDateTime ultimaActualizacion, Long version) {
        return new InventarioItem(id, proyectoId, recursoId, recursoExternalId, bodegaId, nombre, clasificacion,
                unidadBase, cantidadFisica, costoPromedio, ubicacion, ultimaActualizacion, version, List.of());
    }

    /**
     * Registra una ENTRADA de material (por compra). Retorna el nuevo estado y el
     * movimiento generado.
     */
    public InventarioTransaction ingresar(BigDecimal cantidad, BigDecimal costoUnitario, UUID compraDetalleId,
            String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser positiva");
        }
        if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }

        // Crear movimiento
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearEntradaPorCompra(movimientoId, this.id.getValue(),
                cantidad, costoUnitario, compraDetalleId, referencia);

        // Calcular nuevo costo promedio ponderado
        BigDecimal nuevoCostoPromedio;
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            nuevoCostoPromedio = costoUnitario;
        } else {
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            nuevoCostoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4,
                    java.math.RoundingMode.HALF_UP);
        }

        // Actualizar cantidad física
        BigDecimal nuevaCantidadFisica = this.cantidadFisica.add(cantidad);

        // Agregar movimiento a la lista
        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                nuevoCostoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra una SALIDA de material (por consumo). Retorna el nuevo estado y el
     * movimiento generado.
     */
    public InventarioTransaction egresar(BigDecimal cantidad, String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser positiva");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }

        // Validar stock suficiente
        if (this.cantidadFisica.compareTo(cantidad) < 0) {
            throw new CantidadInsuficienteException(
                    String.format("Stock insuficiente. Disponible: %s, Requerido: %s", this.cantidadFisica, cantidad));
        }

        // Crear movimiento usando el costo promedio actual
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaPorConsumo(movimientoId, this.id.getValue(),
                cantidad, this.costoPromedio, referencia);

        // Actualizar cantidad física
        BigDecimal nuevaCantidadFisica = this.cantidadFisica.subtract(cantidad);

        // Agregar movimiento a la lista
        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                this.costoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra una SALIDA de material por requisición.
     */
    public InventarioTransaction egresarPorRequisicion(BigDecimal cantidad, UUID requisicionId, UUID requisicionItemId,
            UUID partidaId, String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser positiva");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        if (requisicionId == null) {
            throw new IllegalArgumentException("El requisicionId no puede ser nulo");
        }
        if (requisicionItemId == null) {
            throw new IllegalArgumentException("El requisicionItemId no puede ser nulo");
        }

        // Validar stock suficiente
        if (this.cantidadFisica.compareTo(cantidad) < 0) {
            throw new CantidadInsuficienteException(
                    String.format("Stock insuficiente. Disponible: %s, Requerido: %s", this.cantidadFisica, cantidad));
        }

        // Crear movimiento con referencias a requisición
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaConRequisicion(movimientoId,
                this.id.getValue(), cantidad, this.costoPromedio, requisicionId, requisicionItemId, partidaId,
                referencia);

        // Actualizar cantidad física
        BigDecimal nuevaCantidadFisica = this.cantidadFisica.subtract(cantidad);

        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                this.costoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra una SALIDA por transferencia a otra bodega del mismo proyecto.
     */
    public InventarioTransaction transferirSalida(BigDecimal cantidad, TransferenciaId transferenciaId,
            String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de transferencia debe ser positiva");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        if (transferenciaId == null) {
            throw new IllegalArgumentException("El transferenciaId no puede ser nulo");
        }

        // Validar stock suficiente
        if (this.cantidadFisica.compareTo(cantidad) < 0) {
            throw new CantidadInsuficienteException(
                    String.format("Stock insuficiente para transferencia. Disponible: %s, Requerido: %s",
                            this.cantidadFisica, cantidad));
        }

        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaTransferencia(movimientoId,
                this.id.getValue(), cantidad, this.costoPromedio, transferenciaId.getValue(), referencia);

        BigDecimal nuevaCantidadFisica = this.cantidadFisica.subtract(cantidad);

        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                this.costoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra una ENTRADA por transferencia desde otra bodega del mismo proyecto.
     */
    public InventarioTransaction transferirEntrada(BigDecimal cantidad, BigDecimal costoUnitario,
            TransferenciaId transferenciaId, String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada por transferencia debe ser positiva");
        }
        if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        if (transferenciaId == null) {
            throw new IllegalArgumentException("El transferenciaId no puede ser nulo");
        }

        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearEntradaTransferencia(movimientoId,
                this.id.getValue(), cantidad, costoUnitario, transferenciaId.getValue(), referencia);

        BigDecimal nuevoCostoPromedio;
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            nuevoCostoPromedio = costoUnitario;
        } else {
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            nuevoCostoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4,
                    java.math.RoundingMode.HALF_UP);
        }

        BigDecimal nuevaCantidadFisica = this.cantidadFisica.add(cantidad);

        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                nuevoCostoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra un AJUSTE de inventario (positivo o negativo).
     */
    public InventarioTransaction ajustar(BigDecimal cantidad, String justificacion, String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("La cantidad del ajuste no puede ser cero");
        }
        if (justificacion == null || justificacion.trim().length() < 20) {
            throw new IllegalArgumentException("La justificación debe tener al menos 20 caracteres");
        }

        // Si es salida (cantidad negativa), validar stock
        if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal required = cantidad.abs();
            if (this.cantidadFisica.compareTo(required) < 0) {
                throw new CantidadInsuficienteException(
                        String.format("Stock insuficiente para ajuste. Disponible: %s, Requerido: %s",
                                this.cantidadFisica, required));
            }
        }

        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearAjuste(movimientoId, this.id.getValue(), cantidad,
                this.costoPromedio, justificacion, referencia);

        BigDecimal nuevaCantidadFisica = this.cantidadFisica.add(cantidad);

        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                this.costoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra una SALIDA por préstamo a otro proyecto.
     */
    public InventarioTransaction transferirSalidaPrestamo(BigDecimal cantidad, TransferenciaId transferenciaId,
            String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de préstamo debe ser positiva");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        if (transferenciaId == null) {
            throw new IllegalArgumentException("El transferenciaId no puede ser nulo");
        }

        if (this.cantidadFisica.compareTo(cantidad) < 0) {
            throw new CantidadInsuficienteException(String.format(
                    "Stock insuficiente para préstamo. Disponible: %s, Requerido: %s", this.cantidadFisica, cantidad));
        }

        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaPrestamo(movimientoId, this.id.getValue(),
                cantidad, this.costoPromedio, transferenciaId.getValue(), referencia);

        BigDecimal nuevaCantidadFisica = this.cantidadFisica.subtract(cantidad);

        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                this.costoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Registra una ENTRADA por préstamo desde otro proyecto.
     */
    public InventarioTransaction transferirEntradaPrestamo(BigDecimal cantidad, BigDecimal costoUnitario,
            TransferenciaId transferenciaId, String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada por préstamo debe ser positiva");
        }
        if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        if (transferenciaId == null) {
            throw new IllegalArgumentException("El transferenciaId no puede ser nulo");
        }

        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearEntradaPrestamo(movimientoId, this.id.getValue(),
                cantidad, costoUnitario, transferenciaId.getValue(), referencia);

        BigDecimal nuevoCostoPromedio;
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            nuevoCostoPromedio = costoUnitario;
        } else {
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            nuevoCostoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4,
                    java.math.RoundingMode.HALF_UP);
        }

        BigDecimal nuevaCantidadFisica = this.cantidadFisica.add(cantidad);

        List<MovimientoInventario> nuevosMovimientos = new ArrayList<>(this.movimientosNuevos);
        nuevosMovimientos.add(movimiento);

        InventarioItem nuevoItem = new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId,
                this.bodegaId, this.nombre, this.clasificacion, this.unidadBase, nuevaCantidadFisica,
                nuevoCostoPromedio, this.ubicacion, LocalDateTime.now(), this.version + 1, nuevosMovimientos);

        return new InventarioTransaction(nuevoItem, movimiento);
    }

    /**
     * Actualiza la ubicación en el almacén. Retorna una nueva instancia.
     */
    @Deprecated
    public InventarioItem actualizarUbicacion(String nuevaUbicacion) {
        return new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId, this.bodegaId,
                this.nombre, this.clasificacion, this.unidadBase, this.cantidadFisica, this.costoPromedio,
                nuevaUbicacion, LocalDateTime.now(), this.version + 1, this.movimientosNuevos);
    }

    /**
     * Confirma la persistencia de movimientos (limpia la lista de pendientes).
     * Retorna una nueva instancia con la lista vacía.
     */
    public InventarioItem confirmarPersistenciaMovimientos() {
        return new InventarioItem(this.id, this.proyectoId, this.recursoId, this.recursoExternalId, this.bodegaId,
                this.nombre, this.clasificacion, this.unidadBase, this.cantidadFisica, this.costoPromedio,
                this.ubicacion, this.ultimaActualizacion, this.version, List.of());
    }

    // Getters

    public InventarioId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    /**
     * @deprecated Usar getRecursoExternalId() en su lugar.
     */
    @Deprecated
    public UUID getRecursoId() {
        return recursoId;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public BodegaId getBodegaId() {
        return bodegaId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public String getUnidadBase() {
        return unidadBase;
    }

    public BigDecimal getCantidadFisica() {
        return cantidadFisica;
    }

    public BigDecimal getCostoPromedio() {
        return costoPromedio;
    }

    /**
     * @deprecated Usar getBodegaId() en su lugar.
     */
    @Deprecated
    public String getUbicacion() {
        return ubicacion;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public Long getVersion() {
        return version;
    }

    public List<MovimientoInventario> getMovimientosNuevos() {
        return movimientosNuevos;
    }

    public boolean tieneStock(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.cantidadFisica.compareTo(cantidad) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InventarioItem that = (InventarioItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "InventarioItem{id=%s, proyectoId=%s, recursoExternalId=%s, bodegaId=%s, nombre=%s, cantidadFisica=%s, costoPromedio=%s}",
                id, proyectoId, recursoExternalId, bodegaId, nombre, cantidadFisica, costoPromedio);
    }
}
