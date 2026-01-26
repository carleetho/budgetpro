package com.budgetpro.domain.finanzas.cronograma.port.out;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramadaId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia de ActividadProgramada.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ActividadProgramadaRepository {

    /**
     * Guarda una actividad programada.
     * 
     * @param actividad La actividad a guardar
     */
    void save(ActividadProgramada actividad);

    /**
     * Busca una actividad por su ID.
     * 
     * @param id El ID de la actividad
     * @return Optional con la actividad si existe, vacío en caso contrario
     */
    Optional<ActividadProgramada> findById(ActividadProgramadaId id);

    /**
     * Busca todas las actividades de un programa de obra.
     * 
     * @param programaObraId El ID del programa de obra
     * @return Lista de actividades del programa
     */
    List<ActividadProgramada> findByProgramaObraId(UUID programaObraId);

    /**
     * Busca la actividad programada de una partida (relación 1:1).
     * 
     * @param partidaId El ID de la partida
     * @return Optional con la actividad si existe, vacío en caso contrario
     */
    Optional<ActividadProgramada> findByPartidaId(UUID partidaId);
}
