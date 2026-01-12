package com.budgetpro.application.recurso.port.in;

import com.budgetpro.application.recurso.dto.RecursoSearchResponse;
import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.util.List;

/**
 * Puerto de Entrada (Inbound Port) para buscar recursos.
 * 
 * Define el contrato del caso de uso de búsqueda sin depender de tecnologías específicas.
 * 
 * REGLA: Este es un puerto puro de la capa de aplicación. NO contiene anotaciones Spring.
 */
public interface BuscarRecursosUseCase {

    /**
     * Busca recursos por nombre (búsqueda difusa).
     * 
     * @param searchQuery El término de búsqueda (opcional, puede ser null o vacío para listar todos)
     * @param tipo Filtro opcional por tipo de recurso (puede ser null)
     * @param limit Límite de resultados (opcional, puede ser null para sin límite)
     * @return Lista de recursos que coinciden con la búsqueda
     */
    List<RecursoSearchResponse> buscar(String searchQuery, TipoRecurso tipo, Integer limit);
}
