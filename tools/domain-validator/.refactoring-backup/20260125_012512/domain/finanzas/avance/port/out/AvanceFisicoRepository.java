package com.budgetpro.domain.finanzas.avance.port.out;

import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;
import com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado AvanceFisico.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface AvanceFisicoRepository {

    /**
     * Guarda un avance físico.
     * 
     * @param avance El avance a guardar
     */
    void save(AvanceFisico avance);

    /**
     * Busca un avance por su ID.
     * 
     * @param id El ID del avance
     * @return Optional con el avance si existe, vacío en caso contrario
     */
    Optional<AvanceFisico> findById(AvanceFisicoId id);

    /**
     * Busca todos los avances de una partida.
     * 
     * @param partidaId El ID de la partida
     * @return Lista de avances de la partida
     */
    List<AvanceFisico> findByPartidaId(UUID partidaId);

    /**
     * Busca todos los avances de una partida en un rango de fechas.
     * 
     * @param partidaId El ID de la partida
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de avances en el rango
     */
    List<AvanceFisico> findByPartidaIdAndFechaBetween(UUID partidaId, LocalDate fechaInicio, LocalDate fechaFin);
}
