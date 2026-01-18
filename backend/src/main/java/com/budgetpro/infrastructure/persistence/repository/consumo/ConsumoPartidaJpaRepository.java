package com.budgetpro.infrastructure.persistence.repository.consumo;

import com.budgetpro.infrastructure.persistence.entity.consumo.ConsumoPartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ConsumoPartidaEntity.
 */
@Repository
public interface ConsumoPartidaJpaRepository extends JpaRepository<ConsumoPartidaEntity, UUID> {

    /**
     * Busca todos los consumos de una partida.
     * 
     * @param partidaId El ID de la partida
     * @return Lista de consumos de la partida
     */
    List<ConsumoPartidaEntity> findByPartidaId(UUID partidaId);

    /**
     * Busca todos los consumos relacionados a un detalle de compra.
     * 
     * @param compraDetalleId El ID del detalle de compra
     * @return Lista de consumos relacionados
     */
    List<ConsumoPartidaEntity> findByCompraDetalleId(UUID compraDetalleId);
}
