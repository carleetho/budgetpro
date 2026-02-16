package com.budgetpro.domain.logistica.compra.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida para integración con el servicio de inventario.
 * 
 * Permite actualizar el stock de materiales cuando se recibe una orden de compra.
 */
public interface InventarioService {

    /**
     * Aumenta el stock de un material en el inventario.
     * 
     * Solo debe llamarse para partidas de tipo Material.
     * 
     * @param proyectoId ID del proyecto
     * @param partidaId ID de la partida presupuestaria (debe ser Material)
     * @param cantidad Cantidad a agregar al inventario
     * @param referencia Referencia de la operación (ej. "PO-2024-001")
     * @throws IllegalArgumentException si la partida no es de tipo Material o no existe
     * @throws IllegalStateException si hay un error al actualizar el inventario
     */
    void increaseStock(UUID proyectoId, UUID partidaId, BigDecimal cantidad, String referencia);
}
