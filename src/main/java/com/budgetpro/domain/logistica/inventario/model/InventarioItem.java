package com.budgetpro.domain.logistica.inventario.model;

import com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado INVENTARIO.
 * 
 * Representa el stock físico de un recurso en un proyecto.
 * 
 * Un registro por Proyecto + Recurso (relación 1:1 lógica, identificado por proyectoId + recursoId).
 * 
 * Invariantes Críticas:
 * - La cantidadFisica NUNCA puede ser negativa
 * - Todo movimiento genera un registro en el Kardex (MovimientoInventario)
 * - El costoPromedio se calcula ponderadamente cuando hay entradas
 * - No existe stock sin movimiento
 */
public final class InventarioItem {

    private final InventarioId id;
    private final UUID proyectoId;
    private final UUID recursoId;
    private BigDecimal cantidadFisica; // Stock actual
    private BigDecimal costoPromedio; // Costo promedio ponderado
    private String ubicacion; // Ubicación en el almacén
    private LocalDateTime ultimaActualizacion;
    private Long version;

    // Lista de movimientos nuevos pendientes de persistir
    private final List<MovimientoInventario> movimientosNuevos;

    /**
     * Constructor privado. Usar factory methods.
     */
    private InventarioItem(InventarioId id, UUID proyectoId, UUID recursoId,
                           BigDecimal cantidadFisica, BigDecimal costoPromedio,
                           String ubicacion, LocalDateTime ultimaActualizacion, Long version) {
        this.id = Objects.requireNonNull(id, "El ID del inventario no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId no puede ser nulo");
        this.cantidadFisica = cantidadFisica != null ? cantidadFisica : BigDecimal.ZERO;
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
        this.ubicacion = ubicacion;
        this.ultimaActualizacion = ultimaActualizacion != null ? ultimaActualizacion : LocalDateTime.now();
        this.version = version != null ? version : 0L;
        this.movimientosNuevos = new ArrayList<>();
        
        // Invariante: cantidadFisica no puede ser negativa
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad física no puede ser negativa");
        }
    }

    /**
     * Factory method para crear un nuevo item de inventario (stock inicial en ZERO).
     */
    public static InventarioItem crear(InventarioId id, UUID proyectoId, UUID recursoId, String ubicacion) {
        return new InventarioItem(id, proyectoId, recursoId, BigDecimal.ZERO, BigDecimal.ZERO,
                                 ubicacion, LocalDateTime.now(), 0L);
    }

    /**
     * Factory method para reconstruir un item de inventario desde persistencia.
     */
    public static InventarioItem reconstruir(InventarioId id, UUID proyectoId, UUID recursoId,
                                             BigDecimal cantidadFisica, BigDecimal costoPromedio,
                                             String ubicacion, LocalDateTime ultimaActualizacion, Long version) {
        return new InventarioItem(id, proyectoId, recursoId, cantidadFisica, costoPromedio,
                                 ubicacion, ultimaActualizacion, version);
    }

    /**
     * Registra una ENTRADA de material (por compra).
     * 
     * Aumenta la cantidad física y recalcula el costo promedio ponderado.
     * Crea un MovimientoInventario de tipo ENTRADA_COMPRA.
     * 
     * Fórmula del costo promedio ponderado:
     * nuevoCostoPromedio = (cantidadActual * costoPromedioActual + cantidadEntrada * costoUnitario) / (cantidadActual + cantidadEntrada)
     * 
     * @param cantidad Cantidad a ingresar (debe ser positiva)
     * @param costoUnitario Costo unitario de la entrada
     * @param compraDetalleId ID del detalle de compra (opcional, para trazabilidad)
     * @param referencia Descripción o referencia de la entrada
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException si la cantidad no es positiva o el costo es negativo
     */
    public MovimientoInventario ingresar(BigDecimal cantidad, BigDecimal costoUnitario,
                                         UUID compraDetalleId, String referencia) {
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
        MovimientoInventario movimiento = MovimientoInventario.crearEntradaPorCompra(
            movimientoId, this.id.getValue(), cantidad, costoUnitario, compraDetalleId, referencia
        );

        // Calcular nuevo costo promedio ponderado
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            // Si no hay stock, el costo promedio es directamente el costo unitario de entrada
            this.costoPromedio = costoUnitario;
        } else {
            // Fórmula del costo promedio ponderado
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            this.costoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4, java.math.RoundingMode.HALF_UP);
        }

        // Actualizar cantidad física
        this.cantidadFisica = this.cantidadFisica.add(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        // Agregar movimiento a la lista de nuevos
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Registra una SALIDA de material (por consumo).
     * 
     * Disminuye la cantidad física usando el costo promedio actual.
     * Crea un MovimientoInventario de tipo SALIDA_CONSUMO.
     * 
     * INVARIANTE CRÍTICA: Si la cantidad resultante sería negativa, lanza CantidadInsuficienteException.
     * 
     * @param cantidad Cantidad a egresar (debe ser positiva)
     * @param referencia Descripción o referencia de la salida
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException si la cantidad no es positiva o la referencia está vacía
     * @throws CantidadInsuficienteException si no hay suficiente stock
     */
    public MovimientoInventario egresar(BigDecimal cantidad, String referencia) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser positiva");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }

        // Validar stock suficiente
        if (this.cantidadFisica.compareTo(cantidad) < 0) {
            throw new CantidadInsuficienteException(
                String.format("Stock insuficiente. Disponible: %s, Requerido: %s",
                            this.cantidadFisica, cantidad)
            );
        }

        // Crear movimiento usando el costo promedio actual
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaPorConsumo(
            movimientoId, this.id.getValue(), cantidad, this.costoPromedio, referencia
        );

        // Actualizar cantidad física
        this.cantidadFisica = this.cantidadFisica.subtract(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        // Agregar movimiento a la lista de nuevos
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Actualiza la ubicación en el almacén.
     */
    public void actualizarUbicacion(String nuevaUbicacion) {
        this.ubicacion = nuevaUbicacion;
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;
    }

    /**
     * Limpia la lista de movimientos nuevos (se llama después de persistir).
     */
    public void limpiarMovimientosNuevos() {
        this.movimientosNuevos.clear();
    }

    // Getters

    public InventarioId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public BigDecimal getCantidadFisica() {
        return cantidadFisica;
    }

    public BigDecimal getCostoPromedio() {
        return costoPromedio;
    }

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
        return new ArrayList<>(movimientosNuevos); // Retorna copia defensiva
    }

    public boolean tieneStock(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.cantidadFisica.compareTo(cantidad) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioItem that = (InventarioItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("InventarioItem{id=%s, proyectoId=%s, recursoId=%s, cantidadFisica=%s, costoPromedio=%s}",
                           id, proyectoId, recursoId, cantidadFisica, costoPromedio);
    }
}
