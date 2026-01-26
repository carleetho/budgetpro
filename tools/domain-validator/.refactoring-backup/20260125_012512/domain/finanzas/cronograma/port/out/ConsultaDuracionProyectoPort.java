package com.budgetpro.domain.finanzas.cronograma.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para consultar la duración de un proyecto.
 * 
 * Este puerto es usado por el Motor de Costos (Mov 9) para obtener el Tiempo de Construcción (TC)
 * necesario para calcular el Financiamiento y los Indirectos de Campo.
 * 
 * La implementación estará en la capa de infraestructura.
 */
public interface ConsultaDuracionProyectoPort {

    /**
     * Obtiene la duración en meses de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con la duración en meses, o vacío si no existe programa de obra
     */
    Optional<Integer> getDuracionMeses(UUID proyectoId);
}
