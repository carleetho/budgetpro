package com.budgetpro.domain.logistica.compra.port.out;

import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Compra.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface CompraRepository {

    /**
     * Guarda una compra y todos sus detalles.
     * 
     * @param compra La compra a guardar (con su lista de detalles)
     */
    void save(Compra compra);

    /**
     * Busca una compra por su ID.
     * 
     * @param id El ID de la compra
     * @return Optional con la compra si existe, vacío en caso contrario
     */
    Optional<Compra> findById(CompraId id);

    /**
     * Busca todas las compras de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de compras del proyecto
     */
    java.util.List<Compra> findByProyectoId(UUID proyectoId);
}
