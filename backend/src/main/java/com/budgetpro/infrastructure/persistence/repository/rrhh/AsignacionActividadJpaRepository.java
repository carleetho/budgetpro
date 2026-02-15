package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.AsignacionActividadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AsignacionActividadJpaRepository extends JpaRepository<AsignacionActividadEntity, UUID> {
}
