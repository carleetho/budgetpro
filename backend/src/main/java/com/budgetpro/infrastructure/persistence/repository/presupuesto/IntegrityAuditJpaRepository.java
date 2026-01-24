package com.budgetpro.infrastructure.persistence.repository.presupuesto;

import com.budgetpro.infrastructure.persistence.entity.presupuesto.IntegrityAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntegrityAuditJpaRepository extends JpaRepository<IntegrityAuditEntity, UUID> {
    
    List<IntegrityAuditEntity> findByPresupuestoIdOrderByValidatedAtDesc(UUID presupuestoId);
    
    List<IntegrityAuditEntity> findByEventTypeOrderByValidatedAtDesc(String eventType);
}
