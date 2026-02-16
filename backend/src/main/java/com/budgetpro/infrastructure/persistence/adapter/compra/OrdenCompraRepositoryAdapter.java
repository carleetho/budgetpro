package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.DetalleOrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import com.budgetpro.infrastructure.persistence.entity.compra.DetalleOrdenCompraEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.OrdenCompraEntity;
import com.budgetpro.infrastructure.persistence.mapper.compra.OrdenCompraMapper;
import com.budgetpro.infrastructure.persistence.repository.compra.OrdenCompraJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para OrdenCompraRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class OrdenCompraRepositoryAdapter implements OrdenCompraRepository {

    private final OrdenCompraJpaRepository jpaRepository;
    private final OrdenCompraMapper mapper;

    public OrdenCompraRepositoryAdapter(OrdenCompraJpaRepository jpaRepository, OrdenCompraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(OrdenCompra ordenCompra) {
        Optional<OrdenCompraEntity> existingEntityOpt = jpaRepository.findById(ordenCompra.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            OrdenCompraEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, ordenCompra);
            
            // Actualizar detalles (cascade y orphanRemoval manejan la sincronización)
            sincronizarDetalles(existingEntity, ordenCompra);
            
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            OrdenCompraEntity newEntity = mapper.toEntity(ordenCompra);
            jpaRepository.save(newEntity);
        }
    }

    /**
     * Sincroniza los detalles del dominio con los de la entidad.
     */
    private void sincronizarDetalles(OrdenCompraEntity existingEntity, OrdenCompra ordenCompra) {
        // Limpiar detalles existentes y agregar los nuevos
        existingEntity.getDetalles().clear();
        
        // Crear nuevos detalles
        int orden = 0;
        for (DetalleOrdenCompra detalleDomain : ordenCompra.getDetalles()) {
            DetalleOrdenCompraEntity detalleEntity = mapper.toDetalleEntity(detalleDomain, existingEntity, orden++);
            existingEntity.getDetalles().add(detalleEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdenCompra> findById(OrdenCompraId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(OrdenCompraId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompra> findByProyectoId(java.util.UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompra> findByEstado(OrdenCompraEstado estado) {
        return jpaRepository.findByEstado(estado).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompra> findByProyectoIdAndEstado(java.util.UUID proyectoId, OrdenCompraEstado estado) {
        return jpaRepository.findByProyectoIdAndEstado(proyectoId, estado).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNextNumero(int year) {
        String pattern = String.format("PO-%d-%%", year);
        List<OrdenCompraEntity> ordenes = jpaRepository.findByNumeroPattern(pattern);
        
        int maxNumber = 0;
        String prefix = String.format("PO-%d-", year);
        
        for (OrdenCompraEntity orden : ordenes) {
            String numero = orden.getNumero();
            if (numero.startsWith(prefix)) {
                try {
                    String suffix = numero.substring(prefix.length());
                    int num = Integer.parseInt(suffix);
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar números mal formateados
                }
            }
        }
        
        int nextNumber = maxNumber + 1;
        return String.format("PO-%d-%03d", year, nextNumber);
    }
}
