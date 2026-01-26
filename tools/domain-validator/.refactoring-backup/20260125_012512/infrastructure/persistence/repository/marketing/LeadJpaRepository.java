package com.budgetpro.infrastructure.persistence.repository.marketing;

import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para leads de marketing.
 */
@Repository
public interface LeadJpaRepository extends JpaRepository<LeadEntity, UUID> {
}
