package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.CuadrillaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CuadrillaJpaRepository extends JpaRepository<CuadrillaEntity, UUID> {

    List<CuadrillaEntity> findByProyectoId(UUID proyectoId);

    List<CuadrillaEntity> findByProyectoIdAndEstado(UUID proyectoId, String estado);
}
