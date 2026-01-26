package com.budgetpro.domain.catalogo.port;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.RecursoSearchCriteria;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;

import java.util.List;

/**
 * Puerto de salida para integración con catálogos externos.
 * Las implementaciones deben vivir en infraestructura.
 */
public interface CatalogPort {

    /**
     * Obtiene un recurso puntual desde el catálogo externo.
     *
     * @param externalId Identificador externo del recurso
     * @param catalogSource Fuente/namespace del catálogo
     * @return snapshot del recurso
     */
    RecursoSnapshot fetchRecurso(String externalId, String catalogSource);

    /**
     * Busca recursos en el catálogo externo según criterios.
     *
     * @param criteria Criterios de búsqueda
     * @param catalogSource Fuente/namespace del catálogo
     * @return lista de snapshots
     */
    List<RecursoSnapshot> searchRecursos(RecursoSearchCriteria criteria, String catalogSource);

    /**
     * Obtiene el APU desde el catálogo externo.
     *
     * @param externalApuId Identificador externo del APU
     * @param catalogSource Fuente/namespace del catálogo
     * @return snapshot del APU
     */
    APUSnapshot fetchAPU(String externalApuId, String catalogSource);

    /**
     * Verifica si un recurso sigue activo en el catálogo externo.
     *
     * @param externalId Identificador externo del recurso
     * @param catalogSource Fuente/namespace del catálogo
     * @return true si el recurso está activo
     */
    boolean isRecursoActive(String externalId, String catalogSource);
}
