package com.budgetpro.domain.finanzas.sobrecosto.port.out;

import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado ConfiguracionLaboral.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ConfiguracionLaboralRepository {

    /**
     * Guarda una configuración laboral.
     * 
     * @param configuracion La configuración a guardar
     */
    void save(ConfiguracionLaboral configuracion);

    /**
     * Busca una configuración por su ID.
     * 
     * @param id El ID de la configuración
     * @return Optional con la configuración si existe, vacío en caso contrario
     */
    Optional<ConfiguracionLaboral> findById(ConfiguracionLaboralId id);

    /**
     * Busca la configuración laboral global (singleton).
     * 
     * @return Optional con la configuración global si existe, vacío en caso contrario
     */
    Optional<ConfiguracionLaboral> findGlobal();

    /**
     * Busca la configuración laboral de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con la configuración del proyecto si existe, vacío en caso contrario
     */
    Optional<ConfiguracionLaboral> findByProyectoId(UUID proyectoId);
}
