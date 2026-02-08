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

    private final InventarioId id;
    private final UUID proyectoId;

    @Deprecated
    private final UUID recursoId; // Mantener para compatibilidad durante migración

    private final String recursoExternalId; // ID externo del recurso (ej. "MAT-001")
    private final BodegaId bodegaId; // FK a la bodega donde está el inventario

    // Campos snapshot (inmutables después de creación)
    private final String nombre; // Nombre del recurso al momento de creación
    private final String clasificacion; // Clasificación del recurso (snapshot)
    private final String unidadBase; // Unidad base del recurso (snapshot)

    // JUSTIFICACIÓN ARQUITECTÓNICA: Aggregate Root con estado mutable intencional.
    // Estos campos representan el estado evolutivo del inventario y DEBEN ser
    // mutables:
    // - cantidadFisica: se actualiza en cada movimiento (ingresar/egresar/ajustar)
    // - costoPromedio: se recalcula con cada entrada (fórmula PMP)
    // - ultimaActualizacion: timestamp de última modificación
    // - version: optimistic locking para concurrencia
    // Pattern: Aggregate Root con Invariantes (cantidadFisica >= 0)
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal cantidadFisica; // Stock actual
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal costoPromedio; // Costo promedio ponderado

    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    @Deprecated
    private String ubicacion; // DEPRECATED: Usar bodegaId en su lugar. Mantener para compatibilidad.

    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private LocalDateTime ultimaActualizacion;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private Long version;

    // Lista de movimientos nuevos pendientes de persistir
    private final List<MovimientoInventario> movimientosNuevos;

    /**
     * Constructor privado. Usar factory methods.
     */
    private InventarioItem(InventarioId id, UUID proyectoId, UUID recursoId, String recursoExternalId,
            BodegaId bodegaId, String nombre, String clasificacion, String unidadBase, BigDecimal cantidadFisica,
            BigDecimal costoPromedio, String ubicacion, LocalDateTime ultimaActualizacion, Long version) {
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
        this.movimientosNuevos = new ArrayList<>();

        // Invariante: cantidadFisica no puede ser negativa
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad física no puede ser negativa");
        }
    }

    /**
     * Factory method para crear un nuevo item de inventario con snapshot completo.
     * 
     * @param id                Identificador único del inventario
     * @param proyectoId        ID del proyecto
     * @param recursoExternalId ID externo del recurso (ej. "MAT-001")
     * @param bodegaId          ID de la bodega donde se almacena
     * @param nombre            Nombre del recurso (snapshot inmutable)
     * @param clasificacion     Clasificación del recurso (snapshot inmutable)
     * @param unidadBase        Unidad base del recurso (snapshot inmutable)
     * @return Nuevo InventarioItem con stock inicial en ZERO
     */
    public static InventarioItem crearConSnapshot(InventarioId id, UUID proyectoId, String recursoExternalId,
            BodegaId bodegaId, String nombre, String clasificacion, String unidadBase) {
        return new InventarioItem(id, proyectoId, null, recursoExternalId, bodegaId, nombre, clasificacion, unidadBase,
                BigDecimal.ZERO, BigDecimal.ZERO, null, LocalDateTime.now(), 0L);
    }

    /**
     * Factory method para crear un nuevo item de inventario (stock inicial en
     * ZERO).
     * 
     * @deprecated Usar crearConSnapshot() en su lugar. Este método se mantiene para
     *             compatibilidad durante migración.
     */
    @Deprecated
    public static InventarioItem crear(InventarioId id, UUID proyectoId, UUID recursoId, String ubicacion) {
        // Para compatibilidad: crear con valores por defecto para campos nuevos
        // Esto requiere que se migre después a crearConSnapshot()
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
                unidadBase, cantidadFisica, costoPromedio, ubicacion, ultimaActualizacion, version);
    }

    /**
     * Registra una ENTRADA de material (por compra).
     * 
     * Aumenta la cantidad física y recalcula el costo promedio ponderado. Crea un
     * MovimientoInventario de tipo ENTRADA_COMPRA.
     * 
     * Fórmula del costo promedio ponderado: nuevoCostoPromedio = (cantidadActual *
     * costoPromedioActual + cantidadEntrada * costoUnitario) / (cantidadActual +
     * cantidadEntrada)
     * 
     * @param cantidad        Cantidad a ingresar (debe ser positiva)
     * @param costoUnitario   Costo unitario de la entrada
     * @param compraDetalleId ID del detalle de compra (opcional, para trazabilidad)
     * @param referencia      Descripción o referencia de la entrada
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException si la cantidad no es positiva o el costo es
     *                                  negativo
     */
    public MovimientoInventario ingresar(BigDecimal cantidad, BigDecimal costoUnitario, UUID compraDetalleId,
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
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            // Si no hay stock, el costo promedio es directamente el costo unitario de
            // entrada
            this.costoPromedio = costoUnitario;
        } else {
            // Fórmula del costo promedio ponderado
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            this.costoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4,
                    java.math.RoundingMode.HALF_UP);
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
     * Disminuye la cantidad física usando el costo promedio actual. Crea un
     * MovimientoInventario de tipo SALIDA_CONSUMO.
     * 
     * INVARIANTE CRÍTICA: Si la cantidad resultante sería negativa, lanza
     * CantidadInsuficienteException.
     * 
     * @param cantidad   Cantidad a egresar (debe ser positiva)
     * @param referencia Descripción o referencia de la salida
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException      si la cantidad no es positiva o la
     *                                       referencia está vacía
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
                    String.format("Stock insuficiente. Disponible: %s, Requerido: %s", this.cantidadFisica, cantidad));
        }

        // Crear movimiento usando el costo promedio actual
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaPorConsumo(movimientoId, this.id.getValue(),
                cantidad, this.costoPromedio, referencia);

        // Actualizar cantidad física
        this.cantidadFisica = this.cantidadFisica.subtract(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        // Agregar movimiento a la lista de nuevos
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Registra una SALIDA de material por requisición.
     * 
     * Similar a egresar() pero crea MovimientoInventario con referencias a
     * requisición para trazabilidad completa del despacho.
     * 
     * @param cantidad          Cantidad a egresar (debe ser positiva)
     * @param requisicionId     ID de la requisición
     * @param requisicionItemId ID del ítem de requisición
     * @param partidaId         ID de la partida presupuestal (imputación AC)
     * @param referencia        Descripción o referencia de la salida
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException      si la cantidad no es positiva o la
     *                                       referencia está vacía
     * @throws CantidadInsuficienteException si no hay suficiente stock
     */
    public MovimientoInventario egresarPorRequisicion(BigDecimal cantidad, UUID requisicionId, UUID requisicionItemId,
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
        this.cantidadFisica = this.cantidadFisica.subtract(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        // Agregar movimiento a la lista de nuevos
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Registra una SALIDA por transferencia a otra bodega del mismo proyecto.
     * 
     * Disminuye la cantidad física usando el costo promedio actual. Crea un
     * MovimientoInventario de tipo SALIDA_TRANSFERENCIA.
     * 
     * @param cantidad        Cantidad a transferir (debe ser positiva)
     * @param transferenciaId ID de la transferencia (vincula con la entrada)
     * @param referencia      Descripción o referencia
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException      si la cantidad no es positiva
     * @throws CantidadInsuficienteException si no hay suficiente stock
     */
    public MovimientoInventario transferirSalida(BigDecimal cantidad, TransferenciaId transferenciaId,
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

        // Crear movimiento usando el costo promedio actual
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaTransferencia(movimientoId,
                this.id.getValue(), cantidad, this.costoPromedio, transferenciaId.getValue(), referencia);

        // Actualizar cantidad física
        this.cantidadFisica = this.cantidadFisica.subtract(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        // Agregar movimiento a la lista de nuevos
        this.movimientosNuevos.add(movimiento);

        return movimiento;
    }

    /**
     * Registra una ENTRADA por transferencia desde otra bodega del mismo proyecto.
     * 
     * Aumenta la cantidad física y recalcula el PMP. Crea un MovimientoInventario
     * de tipo ENTRADA_TRANSFERENCIA.
     * 
     * @param cantidad        Cantidad recibida
     * @param costoUnitario   Costo unitario (viene del origen)
     * @param transferenciaId ID de la transferencia
     * @param referencia      Descripción o referencia
     * @return El MovimientoInventario creado
     */
    public MovimientoInventario transferirEntrada(BigDecimal cantidad, BigDecimal costoUnitario,
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

        // Crear movimiento
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearEntradaTransferencia(movimientoId,
                this.id.getValue(), cantidad, costoUnitario, transferenciaId.getValue(), referencia);

        // Calcular nuevo costo promedio ponderado (misma lógica que ingresar)
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            this.costoPromedio = costoUnitario;
        } else {
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            this.costoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4,
                    java.math.RoundingMode.HALF_UP);
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
     * Registra un AJUSTE de inventario (positivo o negativo).
     * 
     * @param cantidad      Cantidad a ajustar (positiva o negativa)
     * @param justificacion Motivo del ajuste
     * @param referencia    Referencia opcional
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException si la justificación es inválida
     */
    public MovimientoInventario ajustar(BigDecimal cantidad, String justificacion, String referencia) {
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

        // Crear Movimiento usando el factory de AJUSTE
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearAjuste(movimientoId, this.id.getValue(), cantidad,
                this.costoPromedio, justificacion, referencia);

        // Recalcular PMP si es entrada positiva?
        // REGLA: Los ajustes positivos se valoran típicamente al PMP actual o a un
        // costo específico.
        // MovimientoInventario.crearAjuste usa 'costoUnitario'. En este caso pasamos
        // 'this.costoPromedio'.
        // Si usamos el costoPromedio actual, el PMP no cambia matemáticamente.
        // PMP_new = (Q*P + q*P) / (Q+q) = P(Q+q)/(Q+q) = P.
        // Así que NO recalculamos PMP para ajustes valorados al PMP actual.
        // Si se necesitara revaluar, sería otro tipo de ajuste (REVALUO).

        // Actualizar stock
        this.cantidadFisica = this.cantidadFisica.add(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        this.movimientosNuevos.add(movimiento);
        return movimiento;
    }

    /**
     * Registra una SALIDA por préstamo a otro proyecto.
     * 
     * Disminuye la cantidad física usando el costo promedio actual. Crea un
     * MovimientoInventario de tipo SALIDA_PRESTAMO.
     * 
     * @param cantidad        Cantidad a transferir (debe ser positiva)
     * @param transferenciaId ID de la transferencia (vincula con la entrada)
     * @param referencia      Descripción o referencia
     * @return El MovimientoInventario creado
     * @throws IllegalArgumentException      si la cantidad no es positiva
     * @throws CantidadInsuficienteException si no hay suficiente stock
     */
    public MovimientoInventario transferirSalidaPrestamo(BigDecimal cantidad, TransferenciaId transferenciaId,
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

        // Validar stock suficiente
        if (this.cantidadFisica.compareTo(cantidad) < 0) {
            throw new CantidadInsuficienteException(String.format(
                    "Stock insuficiente para préstamo. Disponible: %s, Requerido: %s", this.cantidadFisica, cantidad));
        }

        // Crear movimiento usando el costo promedio actual
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearSalidaPrestamo(movimientoId, this.id.getValue(),
                cantidad, this.costoPromedio, transferenciaId.getValue(), referencia);

        // Actualizar cantidad física
        this.cantidadFisica = this.cantidadFisica.subtract(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        this.movimientosNuevos.add(movimiento);
        return movimiento;
    }

    /**
     * Registra una ENTRADA por préstamo desde otro proyecto.
     * 
     * Aumenta la cantidad física y recalcula el PMP. Crea un MovimientoInventario
     * de tipo ENTRADA_PRESTAMO.
     * 
     * @param cantidad        Cantidad recibida
     * @param costoUnitario   Costo unitario (viene del origen)
     * @param transferenciaId ID de la transferencia
     * @param referencia      Descripción o referencia
     * @return El MovimientoInventario creado
     */
    public MovimientoInventario transferirEntradaPrestamo(BigDecimal cantidad, BigDecimal costoUnitario,
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

        // Crear movimiento
        MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
        MovimientoInventario movimiento = MovimientoInventario.crearEntradaPrestamo(movimientoId, this.id.getValue(),
                cantidad, costoUnitario, transferenciaId.getValue(), referencia);

        // Calcular nuevo costo promedio ponderado
        if (this.cantidadFisica.compareTo(BigDecimal.ZERO) == 0) {
            this.costoPromedio = costoUnitario;
        } else {
            BigDecimal valorActual = this.cantidadFisica.multiply(this.costoPromedio);
            BigDecimal valorEntrada = cantidad.multiply(costoUnitario);
            BigDecimal nuevaCantidadTotal = this.cantidadFisica.add(cantidad);
            this.costoPromedio = valorActual.add(valorEntrada).divide(nuevaCantidadTotal, 4,
                    java.math.RoundingMode.HALF_UP);
        }

        // Actualizar cantidad física
        this.cantidadFisica = this.cantidadFisica.add(cantidad);
        this.ultimaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;

        this.movimientosNuevos.add(movimiento);
        return movimiento;
    }

    /**
     * Actualiza la ubicación en el almacén.
     * 
     * @deprecated Usar bodegaId en su lugar. Este método se mantiene para
     *             compatibilidad durante migración.
     */
    @Deprecated
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

    /**
     * @deprecated Usar getRecursoExternalId() en su lugar. Este método se mantiene
     *             para compatibilidad durante migración.
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
     * @deprecated Usar getBodegaId() en su lugar. Este método se mantiene para
     *             compatibilidad durante migración.
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
