package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;

import java.util.UUID;

/**
 * Servicio de Dominio para gestionar el inventario.
 * 
 * Responsabilidad:
 * - Registrar entradas de material por compras
 * - Registrar salidas de material por consumos
 * - Asegurar que el stock físico se actualiza correctamente
 * 
 * No persiste, solo orquesta la lógica de dominio.
 */
public class GestionInventarioService {

    private final InventarioRepository inventarioRepository;

    public GestionInventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    /**
     * Registra la entrada de material al inventario cuando se aprueba una compra.
     * 
     * Para cada detalle de la compra:
     * - Busca o crea el InventarioItem del proyecto + recurso
     * - Registra la entrada usando el método ingresar() del agregado
     * - El agregado calcula el costo promedio ponderado automáticamente
     * 
     * @param compra La compra aprobada con sus detalles
     * @throws IllegalArgumentException si el proyecto o recurso no existe
     */
    public void registrarEntradaPorCompra(Compra compra) {
        for (CompraDetalle detalle : compra.getDetalles()) {
            // TODO: Migrar inventario para usar recursoExternalId en lugar de UUID recursoId
            // Por ahora, generamos un UUID determinístico desde el externalId para compatibilidad
            UUID recursoId = generarRecursoIdDesdeExternalId(detalle.getRecursoExternalId());
            
            // Buscar o crear el InventarioItem para este proyecto + recurso
            InventarioItem inventarioItem = inventarioRepository
                    .findByProyectoIdAndRecursoId(compra.getProyectoId(), recursoId)
                    .orElseGet(() -> {
                        // Si no existe, crear uno nuevo
                        InventarioId inventarioId = InventarioId.generate();
                        return InventarioItem.crear(inventarioId, compra.getProyectoId(),
                                                   recursoId, null); // ubicacion null por ahora
                    });

            // Registrar la entrada usando el método del agregado
            // El agregado calcula el costo promedio y crea el movimiento automáticamente
            inventarioItem.ingresar(
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getId().getValue(), // compraDetalleId para trazabilidad
                    String.format("Entrada por compra #%s - %s", compra.getId().getValue(), compra.getProveedor())
            );

            // Persistir el inventario (con sus movimientos nuevos)
            inventarioRepository.save(inventarioItem);
        }
    }

    /**
     * Registra una salida de material por consumo.
     * 
     * @param proyectoId ID del proyecto
     * @param recursoId ID del recurso
     * @param cantidad Cantidad a egresar
     * @param referencia Descripción o referencia de la salida
     * @throws IllegalArgumentException si el inventario no existe o la referencia está vacía
     * @throws com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException si no hay suficiente stock
     */
    public void registrarSalidaPorConsumo(UUID proyectoId, UUID recursoId, java.math.BigDecimal cantidad,
                                         String referencia) {
        InventarioItem inventarioItem = inventarioRepository
                .findByProyectoIdAndRecursoId(proyectoId, recursoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Inventario no encontrado para proyecto %s y recurso %s",
                                    proyectoId, recursoId)));

        // Registrar la salida usando el método del agregado
        inventarioItem.egresar(cantidad, referencia);

        // Persistir el inventario (con sus movimientos nuevos)
        inventarioRepository.save(inventarioItem);
    }

    /**
     * Genera un UUID determinístico desde un externalId.
     * 
     * TODO: Esto es una solución temporal hasta que el inventario migre a usar externalId.
     * Usa el hash del externalId para generar un UUID consistente.
     */
    private UUID generarRecursoIdDesdeExternalId(String externalId) {
        // Generar UUID v5 (determinístico) usando el namespace DNS y el externalId
        return UUID.nameUUIDFromBytes(externalId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
