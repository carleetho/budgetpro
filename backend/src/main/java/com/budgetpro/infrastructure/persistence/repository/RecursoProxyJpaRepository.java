package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.domain.catalogo.model.EstadoProxy;
import com.budgetpro.infrastructure.persistence.entity.catalogo.RecursoProxyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para RecursoProxyEntity.
 */
@Repository
public interface RecursoProxyJpaRepository extends JpaRepository<RecursoProxyEntity, UUID> {

    Optional<RecursoProxyEntity> findByExternalIdAndCatalogSource(String externalId, String catalogSource);

    List<RecursoProxyEntity> findByEstado(EstadoProxy estado);
}
