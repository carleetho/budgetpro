package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.Recepcion;
import com.budgetpro.domain.logistica.compra.port.out.RecepcionRepository;
import com.budgetpro.infrastructure.persistence.entity.compra.RecepcionEntity;
import com.budgetpro.infrastructure.persistence.mapper.compra.RecepcionMapper;
import com.budgetpro.infrastructure.persistence.repository.compra.RecepcionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia para RecepcionRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class RecepcionRepositoryAdapter implements RecepcionRepository {

    private final RecepcionJpaRepository jpaRepository;
    private final RecepcionMapper mapper;

    public RecepcionRepositoryAdapter(RecepcionJpaRepository jpaRepository, RecepcionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Recepcion recepcion) {
        // Convertir dominio a entidad
        RecepcionEntity entity = mapper.toEntity(recepcion);
        
        // Guardar (JPA maneja cascade para los detalles)
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCompraIdAndGuiaRemision(CompraId compraId, String guiaRemision) {
        // Convertir value object a UUID
        return jpaRepository.existsByCompraIdAndGuiaRemision(compraId.getValue(), guiaRemision);
    }
}
