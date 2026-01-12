package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.MovimientoCajaEntity;
import com.budgetpro.infrastructure.persistence.mapper.BilleteraMapper;
import com.budgetpro.infrastructure.persistence.mapper.MovimientoCajaMapper;
import com.budgetpro.infrastructure.persistence.repository.BilleteraJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.MovimientoCajaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de infraestructura que implementa el puerto de salida BilleteraRepository.
 * 
 * Realiza el mapeo entre el agregado del dominio (Billetera) y la entidad de persistencia (BilleteraEntity),
 * manejando las conversiones y optimistic locking.
 * 
 * Sigue el patrón Adapter de Arquitectura Hexagonal.
 * 
 * CRÍTICO: Persiste tanto la billetera como todos sus movimientos nuevos en la misma transacción,
 * garantizando trazabilidad financiera completa según el contrato del puerto BilleteraRepository.
 */
@Component
public class BilleteraRepositoryAdapter implements BilleteraRepository {

    private final BilleteraJpaRepository billeteraJpaRepository;
    private final MovimientoCajaJpaRepository movimientoCajaJpaRepository;
    private final BilleteraMapper billeteraMapper;
    private final MovimientoCajaMapper movimientoCajaMapper;

    public BilleteraRepositoryAdapter(BilleteraJpaRepository billeteraJpaRepository,
                                     MovimientoCajaJpaRepository movimientoCajaJpaRepository,
                                     BilleteraMapper billeteraMapper,
                                     MovimientoCajaMapper movimientoCajaMapper) {
        this.billeteraJpaRepository = billeteraJpaRepository;
        this.movimientoCajaJpaRepository = movimientoCajaJpaRepository;
        this.billeteraMapper = billeteraMapper;
        this.movimientoCajaMapper = movimientoCajaMapper;
    }

    @Override
    @Transactional
    public void save(Billetera billetera) {
        if (billetera == null) {
            throw new IllegalArgumentException("La billetera no puede ser nula");
        }

        // 1. Buscar entidad existente (si existe)
        Optional<BilleteraEntity> existingEntityOpt = billeteraJpaRepository.findById(billetera.getId().getValue());

        // 2. Mapear dominio a entidad (Hibernate maneja optimistic locking automáticamente con @Version)
        BilleteraEntity entityToSave = billeteraMapper.toEntity(billetera, existingEntityOpt.orElse(null));

        // 3. Guardar billetera (Hibernate maneja optimistic locking automáticamente con @Version)
        BilleteraEntity savedBilleteraEntity = billeteraJpaRepository.save(entityToSave);
        
        // 5. CRÍTICO: Persistir todos los movimientos nuevos del agregado en la misma transacción
        // Esto garantiza trazabilidad financiera completa según el contrato del puerto BilleteraRepository
        List<MovimientoCaja> movimientosNuevos = billetera.getMovimientosNuevos();
        if (movimientosNuevos != null && !movimientosNuevos.isEmpty()) {
            List<MovimientoCajaEntity> movimientosEntities = movimientosNuevos.stream()
                    .map(movimiento -> movimientoCajaMapper.toEntity(movimiento, savedBilleteraEntity))
                    .collect(Collectors.toList());
            
            movimientoCajaJpaRepository.saveAll(movimientosEntities);
        }
        
        // 5. Limpiar movimientos nuevos después de persistir exitosamente
        // Esto cumple con el contrato del puerto BilleteraRepository
        billetera.limpiarMovimientosNuevos();
    }

    @Override
    public Optional<Billetera> findByProyectoId(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }

        return billeteraJpaRepository.findByProyectoId(proyectoId)
                .map(billeteraMapper::toDomain);
    }

    @Override
    public Optional<Billetera> findById(BilleteraId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la billetera no puede ser nulo");
        }

        return billeteraJpaRepository.findById(id.getValue())
                .map(billeteraMapper::toDomain);
    }
}
