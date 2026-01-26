package com.budgetpro.domain.finanzas.partida.port.out;

import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Partida.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface PartidaRepository {

    /**
     * Guarda una partida.
     * 
     * @param partida La partida a guardar
     */
    void save(Partida partida);

    /**
     * Busca una partida por su ID.
     * 
     * @param id El ID de la partida
     * @return Optional con la partida si existe, vacío en caso contrario
     */
    Optional<Partida> findById(PartidaId id);

    /**
     * Busca todas las partidas de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de partidas del presupuesto
     */
    List<Partida> findByPresupuestoId(UUID presupuestoId);

    /**
     * Busca una partida por su ID (UUID directo).
     * 
     * @param partidaId El ID de la partida como UUID
     * @return Optional con la partida si existe, vacío en caso contrario
     */
    Optional<Partida> findById(UUID partidaId);

    /**
     * Verifica si existe una partida con el ID dado.
     * 
     * @param partidaId El ID de la partida como UUID
     * @return true si existe, false en caso contrario
     */
    boolean existsById(UUID partidaId);
}
