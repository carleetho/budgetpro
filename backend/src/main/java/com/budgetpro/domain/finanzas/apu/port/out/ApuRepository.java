package com.budgetpro.domain.finanzas.apu.port.out;

import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.model.ApuId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado APU.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ApuRepository {

    /**
     * Guarda un APU y todos sus insumos.
     * 
     * @param apu El APU a guardar (con su lista de insumos)
     */
    void save(APU apu);

    /**
     * Busca un APU por su ID.
     * 
     * @param id El ID del APU
     * @return Optional con el APU si existe, vacío en caso contrario
     */
    Optional<APU> findById(ApuId id);

    /**
     * Busca el APU de una partida (relación 1:1).
     * 
     * @param partidaId El ID de la partida
     * @return Optional con el APU si existe, vacío en caso contrario
     */
    Optional<APU> findByPartidaId(UUID partidaId);

    /**
     * Verifica si existe un APU para la partida dada.
     * 
     * @param partidaId El ID de la partida
     * @return true si existe, false en caso contrario
     */
    boolean existsByPartidaId(UUID partidaId);
}
