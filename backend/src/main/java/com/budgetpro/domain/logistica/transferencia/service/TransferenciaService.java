package com.budgetpro.domain.logistica.transferencia.service;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.transferencia.event.MaterialTransferredBetweenProjects;
import com.budgetpro.domain.logistica.transferencia.exception.ExcepcionNoEncontradaException;
import com.budgetpro.domain.logistica.transferencia.model.TransferenciaId;
import com.budgetpro.domain.logistica.transferencia.port.out.ExcepcionValidator;
import com.budgetpro.domain.logistica.transferencia.port.out.TransferenciaEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de Dominio para gestionar transferencias de inventario.
 * 
 * Responsabilidad:
 * - Coordinar la transferencia de items entre bodegas de un mismo proyecto.
 * - Asegurar la atomicidad lógica (salida + entrada).
 * - Garantizar la trazabilidad mediante TransferenciaId.
 */
public class TransferenciaService {

    private final InventarioRepository inventarioRepository;
    private final ExcepcionValidator excepcionValidator;
    private final TransferenciaEventPublisher eventPublisher;

    public TransferenciaService(InventarioRepository inventarioRepository,
            ExcepcionValidator excepcionValidator,
            TransferenciaEventPublisher eventPublisher) {
        this.inventarioRepository = inventarioRepository;
        this.excepcionValidator = excepcionValidator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Realiza una transferencia de material entre dos bodegas del mismo proyecto.
     * 
     * 1. Valida existencia de origen y stock suficiente.
     * 2. Busca o crea el item de inventario en la bodega destino (mismo
     * recurso/unidad).
     * 3. Genera un TransferenciaId único para vincular los movimientos.
     * 4. Registra SALIDA_TRANSFERENCIA en origen (al PMP actual de origen).
     * 5. Registra ENTRADA_TRANSFERENCIA en destino (recalcula PMP de destino).
     * 6. Guarda ambos items (actualizando saldos y registrando movimientos).
     * 
     * @param origenId        ID del item de inventario origen
     * @param bodegaDestinoId ID de la bodega destino
     * @param cantidad        Cantidad a transferir
     * @param referencia      Referencia de la operación
     * @throws IllegalArgumentException                                                          si
     *                                                                                           los
     *                                                                                           argumentos
     *                                                                                           son
     *                                                                                           inválidos,
     *                                                                                           bodegas
     *                                                                                           iguales
     *                                                                                           o
     *                                                                                           no
     *                                                                                           existe
     *                                                                                           origen
     * @throws com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException si
     *                                                                                           no
     *                                                                                           hay
     *                                                                                           stock
     */
    public void transferirEntreBodegas(InventarioId origenId, BodegaId bodegaDestinoId, BigDecimal cantidad,
            String referencia) {
        if (origenId == null)
            throw new IllegalArgumentException("El ID de inventario origen no puede ser nulo");
        if (bodegaDestinoId == null)
            throw new IllegalArgumentException("El ID de bodega destino no puede ser nulo");
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        if (referencia == null || referencia.isBlank())
            throw new IllegalArgumentException("La referencia es obligatoria");

        // 1. Obtener item Origen
        InventarioItem origen = inventarioRepository.findById(origenId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario origen no encontrado: " + origenId));

        // Validar que la bodega destino sea diferente a la origen
        if (origen.getBodegaId().equals(bodegaDestinoId)) {
            throw new IllegalArgumentException("La bodega destino debe ser diferente a la bodega origen");
        }

        // 2. Obtener o Crear item Destino
        InventarioItem destino = inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                origen.getProyectoId(),
                origen.getRecursoExternalId(),
                origen.getUnidadBase(),
                bodegaDestinoId).orElseGet(
                        () -> InventarioItem.crearConSnapshot(
                                InventarioId.generate(),
                                origen.getProyectoId(),
                                origen.getRecursoExternalId(),
                                bodegaDestinoId,
                                origen.getNombre(),
                                origen.getClasificacion(),
                                origen.getUnidadBase()));

        // 3. Generar ID de Transferencia
        TransferenciaId transferenciaId = TransferenciaId.generate();

        // 4. Registrar Salida en Origen
        MovimientoInventario movimientoSalida = origen.transferirSalida(cantidad, transferenciaId, referencia);

        // 5. Registrar Entrada en Destino
        // Usamos el costo unitario de la salida (que es el PMP del origen al momento de
        // la transferencia)
        destino.transferirEntrada(cantidad, movimientoSalida.getCostoUnitario(), transferenciaId, referencia);

        // 6. Guardar cambios (debe ser transaccional desde la capa de aplicación o
        // implícito)
        inventarioRepository.save(origen);
        inventarioRepository.save(destino);
    }

    /**
     * Realiza una transferencia (préstamo) de material entre proyectos distintos.
     * 
     * Requiere una Excepción aprobada.
     * Genera eventos de dominio para que Finanzas registre la deuda.
     * 
     * @param origenId          Item de inventario origen
     * @param bodegaDestinoId   Bodega en el proyecto destino
     * @param proyectoDestinoId ID del proyecto destino
     * @param cantidad          Cantidad a transferir
     * @param excepcionId       ID de la excepción aprobada
     * @param referencia        Referencia
     * @throws ExcepcionNoEncontradaException si la excepción no es válida
     */
    public void transferirEntreProyectos(InventarioId origenId, BodegaId bodegaDestinoId,
            UUID proyectoDestinoId, BigDecimal cantidad,
            UUID excepcionId, String referencia) {
        if (origenId == null)
            throw new IllegalArgumentException("El ID de inventario origen no puede ser nulo");
        if (bodegaDestinoId == null)
            throw new IllegalArgumentException("El ID de bodega destino no puede ser nulo");
        if (proyectoDestinoId == null)
            throw new IllegalArgumentException("El proyecto destino no puede ser nulo");
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        if (excepcionId == null)
            throw new IllegalArgumentException("El excepcionId es obligatorio");

        // 1. Validar Excepción
        if (!excepcionValidator.esExcepcionAprobada(excepcionId)) {
            throw new ExcepcionNoEncontradaException("La excepción no existe o no está aprobada: " + excepcionId);
        }

        // 2. Obtener Origen
        InventarioItem origen = inventarioRepository.findById(origenId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario origen no encontrado: " + origenId));

        // Validar proyectos distintos
        if (origen.getProyectoId().equals(proyectoDestinoId)) {
            throw new IllegalArgumentException("Para transferencias en el mismo proyecto use transferirEntreBodegas");
        }

        // 3. Obtener o Crear Destino (en bodega destino y proyecto destino)
        InventarioItem destino = inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                proyectoDestinoId, // Proyecto Destino
                origen.getRecursoExternalId(),
                origen.getUnidadBase(),
                bodegaDestinoId).orElseGet(
                        () -> InventarioItem.crearConSnapshot(
                                InventarioId.generate(),
                                proyectoDestinoId, // Nuevo proyecto
                                origen.getRecursoExternalId(),
                                bodegaDestinoId,
                                origen.getNombre(),
                                origen.getClasificacion(),
                                origen.getUnidadBase()));

        // 4. Generar ID Transferencia
        TransferenciaId transferenciaId = TransferenciaId.generate();

        // 5. Registrar Movimientos (Salida Prestamo + Entrada Prestamo)
        MovimientoInventario movSalida = origen.transferirSalidaPrestamo(cantidad, transferenciaId, referencia);
        // La entrada en destino toma el costo unitario de salida (origen PMP)
        destino.transferirEntradaPrestamo(cantidad, movSalida.getCostoUnitario(), transferenciaId, referencia);

        // 6. Publicar Evento de Dominio
        MaterialTransferredBetweenProjects event = new MaterialTransferredBetweenProjects(
                transferenciaId,
                origen.getProyectoId(),
                proyectoDestinoId,
                origen.getRecursoExternalId(),
                cantidad,
                movSalida.getCostoTotal(), // Total Valorizado
                LocalDateTime.now());
        eventPublisher.publicar(event);

        // 7. Guardar
        inventarioRepository.save(origen);
        inventarioRepository.save(destino);
    }
}
