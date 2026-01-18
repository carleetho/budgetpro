package com.budgetpro.infrastructure.persistence.repository.anticipo;

import com.budgetpro.infrastructure.persistence.entity.anticipo.AnticipoMovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface AnticipoMovimientoJpaRepository extends JpaRepository<AnticipoMovimientoEntity, UUID> {

    @Query("""
        select coalesce(sum(case when m.tipo = 'REGISTRO' then m.monto else -m.monto end), 0)
        from AnticipoMovimientoEntity m
        where m.proyectoId = :proyectoId
        """)
    BigDecimal obtenerSaldoPendiente(@Param("proyectoId") UUID proyectoId);
}
