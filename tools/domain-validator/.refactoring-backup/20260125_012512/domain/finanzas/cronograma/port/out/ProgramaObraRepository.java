package com.budgetpro.domain.finanzas.cronograma.port.out;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado ProgramaObra.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ProgramaObraRepository {

    /**
     * Guarda un programa de obra.
     * 
     * @param programaObra El programa a guardar
     */
    void save(ProgramaObra programaObra);

    /**
     * Busca un programa por su ID.
     * 
     * @param id El ID del programa
     * @return Optional con el programa si existe, vacío en caso contrario
     */
    Optional<ProgramaObra> findById(ProgramaObraId id);

    /**
     * Busca el programa de obra de un proyecto (relación 1:1).
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con el programa si existe, vacío en caso contrario
     */
    Optional<ProgramaObra> findByProyectoId(UUID proyectoId);
}
