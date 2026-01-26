package com.budgetpro.infrastructure.persistence.repository.apu;

import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ApuInsumoEntity.
 */
@Repository
public interface ApuInsumoJpaRepository extends JpaRepository<ApuInsumoEntity, UUID> {

    /**
     * Busca todos los insumos de un APU.
     * 
     * @param apuId El ID del APU
     * @return Lista de insumos del APU
     */
    List<ApuInsumoEntity> findByApuId(UUID apuId);
}
