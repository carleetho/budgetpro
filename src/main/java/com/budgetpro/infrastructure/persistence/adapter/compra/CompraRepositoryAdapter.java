package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.finanzas.compra.port.out.CompraRepository;
import com.budgetpro.domain.finanzas.compra.Compra;
import com.budgetpro.domain.finanzas.compra.CompraId;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraDetalleEntity;
import com.budgetpro.infrastructure.persistence.mapper.compra.CompraMapper;
import com.budgetpro.infrastructure.persistence.repository.compra.CompraJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de infraestructura que implementa el puerto de salida CompraRepository.
 * 
 * Realiza el mapeo entre el agregado del dominio (Compra) y la entidad de persistencia (CompraEntity),
 * manejando las conversiones y optimistic locking.
 * 
 * Sigue el patrón Adapter de Arquitectura Hexagonal.
 */
@Component
public class CompraRepositoryAdapter implements CompraRepository {

    private final CompraJpaRepository jpaRepository;
    private final CompraMapper mapper;

    public CompraRepositoryAdapter(CompraJpaRepository jpaRepository, CompraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La compra no puede ser nula");
        }

        // 1. Buscar entidad existente (si existe)
        Optional<CompraEntity> existingEntityOpt = jpaRepository.findById(compra.getId().getValue());
        
        // 2. Mapear dominio a entidad (Hibernate maneja optimistic locking automáticamente con @Version)
        CompraEntity entityToSave = mapper.toEntity(compra);
        
        // Si la entidad existe, copiar la versión y fechas para actualización correcta
        if (existingEntityOpt.isPresent()) {
            CompraEntity existingEntity = existingEntityOpt.get();
            entityToSave.setVersion(existingEntity.getVersion());
            entityToSave.setCreatedAt(existingEntity.getCreatedAt());
        }
        
        // 3. Guardar compra (Hibernate maneja optimistic locking automáticamente con @Version)
        jpaRepository.save(entityToSave);
    }

    @Override
    public Optional<Compra> findById(CompraId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la compra no puede ser nulo");
        }

        return jpaRepository.findById(id.getValue())
                .map(entity -> {
                    // Forzar carga de detalles (si no están ya cargadas)
                    entity.getDetalles().size(); // Lazy load trigger
                    return mapper.toDomain(entity);
                });
    }
}
