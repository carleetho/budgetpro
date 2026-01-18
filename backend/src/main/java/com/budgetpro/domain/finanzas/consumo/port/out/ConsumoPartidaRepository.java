package com.budgetpro.domain.finanzas.consumo.port.out;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado ConsumoPartida.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ConsumoPartidaRepository {

    /**
     * Guarda un consumo de partida.
     * 
     * @param consumo El consumo a guardar
     */
    void save(ConsumoPartida consumo);

    /**
     * Guarda múltiples consumos de partida.
     * 
     * @param consumos Lista de consumos a guardar
     */
    void saveAll(List<ConsumoPartida> consumos);

    /**
     * Busca un consumo por su ID.
     * 
     * @param id El ID del consumo
     * @return Optional con el consumo si existe, vacío en caso contrario
     */
    Optional<ConsumoPartida> findById(ConsumoPartidaId id);

    /**
     * Busca todos los consumos de una partida.
     * 
     * @param partidaId El ID de la partida
     * @return Lista de consumos de la partida
     */
    List<ConsumoPartida> findByPartidaId(UUID partidaId);

    /**
     * Busca todos los consumos relacionados a un detalle de compra.
     * 
     * @param compraDetalleId El ID del detalle de compra
     * @return Lista de consumos relacionados
     */
    List<ConsumoPartida> findByCompraDetalleId(UUID compraDetalleId);
}
