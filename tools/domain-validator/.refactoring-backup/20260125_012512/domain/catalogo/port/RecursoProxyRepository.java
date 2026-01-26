package com.budgetpro.domain.catalogo.port;

import com.budgetpro.domain.catalogo.model.RecursoProxy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de RecursoProxy.
 */
public interface RecursoProxyRepository {

    Optional<RecursoProxy> findById(UUID id);

    Optional<RecursoProxy> findByExternalId(String externalId, String catalogSource);

    RecursoProxy save(RecursoProxy proxy);

    List<RecursoProxy> findObsoletos();
}
