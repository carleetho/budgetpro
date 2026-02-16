package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.Proveedor;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import com.budgetpro.infrastructure.persistence.entity.compra.ProveedorEntity;
import com.budgetpro.infrastructure.persistence.mapper.compra.ProveedorMapper;
import com.budgetpro.infrastructure.persistence.repository.compra.ProveedorJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para ProveedorRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ProveedorRepositoryAdapter implements ProveedorRepository {

    private final ProveedorJpaRepository jpaRepository;
    private final ProveedorMapper mapper;

    public ProveedorRepositoryAdapter(ProveedorJpaRepository jpaRepository, ProveedorMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Proveedor proveedor) {
        Optional<ProveedorEntity> existingEntityOpt = jpaRepository.findById(proveedor.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            ProveedorEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, proveedor);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ProveedorEntity newEntity = mapper.toEntity(proveedor);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proveedor> findById(ProveedorId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ProveedorId id) {
        // Verificar integridad referencial antes de eliminar
        if (isReferencedByOrdenCompra(id)) {
            throw new IllegalStateException(
                String.format("No se puede eliminar el proveedor %s porque está referenciado por órdenes de compra", id)
            );
        }
        
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRuc(String ruc) {
        return jpaRepository.existsByRuc(ruc);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReferencedByOrdenCompra(ProveedorId id) {
        // Obtener la razón social del proveedor
        Optional<ProveedorEntity> proveedorOpt = jpaRepository.findById(id.getValue());
        if (proveedorOpt.isEmpty()) {
            return false;
        }
        
        String razonSocial = proveedorOpt.get().getRazonSocial();
        
        // Verificar si hay compras que referencian este proveedor por su razón social
        // NOTA: Actualmente CompraEntity tiene un campo String "proveedor" que almacena el nombre.
        // En el futuro, cuando se migre a usar ProveedorId, esta consulta deberá actualizarse.
        long count = jpaRepository.countReferenciasEnCompras(razonSocial);
        return count > 0;
    }
}
