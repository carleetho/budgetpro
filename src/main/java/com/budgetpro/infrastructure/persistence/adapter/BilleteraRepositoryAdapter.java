package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.billetera.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.mapper.BilleteraMapper;
import com.budgetpro.infrastructure.persistence.repository.BilleteraJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el puerto de salida BilleteraRepository.
 * 
 * Realiza el mapeo entre el agregado del dominio (Billetera) y las entidades de persistencia
 * (BilleteraEntity, MovimientoEntity), manejando las conversiones y capturando excepciones de BD.
 * 
 * REGLA CRÍTICA: El método save() debe persistir la billetera Y todos sus movimientos nuevos
 * en una transacción ACID única.
 * 
 * Sigue el patrón Adapter de Arquitectura Hexagonal.
 */
@Component
public class BilleteraRepositoryAdapter implements BilleteraRepository {

    private final BilleteraJpaRepository billeteraJpaRepository;
    private final BilleteraMapper mapper;

    public BilleteraRepositoryAdapter(BilleteraJpaRepository billeteraJpaRepository,
                                     BilleteraMapper mapper) {
        this.billeteraJpaRepository = billeteraJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> findByProyectoId(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }

        Optional<BilleteraEntity> entityOpt = billeteraJpaRepository.findByProyectoId(proyectoId);
        
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        BilleteraEntity billeteraEntity = entityOpt.get();
        Billetera billetera = mapper.toDomain(billeteraEntity);

        // Cargar los movimientos históricos de la billetera
        // NOTA: En el dominio, los movimientos históricos no se cargan automáticamente
        // porque el agregado solo gestiona movimientos nuevos. Si necesitas el historial,
        // deberías usar un Query separado (Read Model) o un método específico del repositorio.
        // Por ahora, solo reconstruimos la billetera con su saldo actual.

        return Optional.of(billetera);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> findById(BilleteraId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la billetera no puede ser nulo");
        }

        UUID billeteraIdValue = id.getValue();
        return billeteraJpaRepository.findById(billeteraIdValue)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void save(Billetera billetera) {
        if (billetera == null) {
            throw new IllegalArgumentException("La billetera no puede ser nula");
        }

        try {
            // Obtener los movimientos nuevos del agregado (pendientes de persistir)
            List<com.budgetpro.domain.finanzas.billetera.Movimiento> movimientosNuevos = billetera.getMovimientosNuevos();
            
            UUID billeteraIdValue = billetera.getId().getValue();
            Optional<BilleteraEntity> existingEntityOpt = billeteraJpaRepository.findById(billeteraIdValue);

            if (existingEntityOpt.isPresent()) {
                // ACTUALIZAR: Billetera existente con movimientos nuevos
                BilleteraEntity existingEntity = existingEntityOpt.get();
                
                // Verificar optimistic locking: el version debe coincidir
                Long currentVersion = billetera.getVersion();
                if (currentVersion == null || !existingEntity.getVersion().equals(currentVersion)) {
                    throw new ObjectOptimisticLockingFailureException(
                        BilleteraEntity.class, 
                        existingEntity.getId()
                    );
                }
                
                // Actualizar la entidad con los datos del agregado
                mapper.updateEntity(existingEntity, billetera);
                // NOTA: Hibernate incrementará automáticamente el version gracias a @Version
                
                // Sincronizar movimientos nuevos usando la relación bidireccional
                // Con CascadeType.ALL, al guardar la billetera, Hibernate persistirá automáticamente los movimientos
                if (!movimientosNuevos.isEmpty()) {
                    List<com.budgetpro.infrastructure.persistence.entity.MovimientoEntity> movimientoEntities = 
                        mapper.toEntityMovimientos(movimientosNuevos, existingEntity);
                    // Agregar cada movimiento a la colección de la billetera (establece relación bidireccional)
                    // El método agregarMovimiento() ya establece la relación en ambos sentidos
                    for (com.budgetpro.infrastructure.persistence.entity.MovimientoEntity movimientoEntity : movimientoEntities) {
                        existingEntity.agregarMovimiento(movimientoEntity);
                    }
                }
                
                // Persistir la billetera actualizada
                // Con CascadeType.ALL, Hibernate persistirá automáticamente los movimientos nuevos
                billeteraJpaRepository.save(existingEntity);
                
                // Limpiar la lista de movimientos nuevos del agregado
                billetera.limpiarMovimientosNuevos();
                
                // NOTA: El version del agregado NO se actualiza aquí porque no hay setter público.
                // Cuando se cargue nuevamente desde BD (findByProyectoId o findById), tendrá el version correcto.
                // Esto es aceptable porque el agregado normalmente se usa dentro de una transacción única.
                
            } else {
                // CREAR: Nueva billetera (primera vez)
                BilleteraEntity newEntity = mapper.toEntity(billetera);
                // Si version es null, el mapper lo establece en 0L para insert inicial
                
                // Sincronizar movimientos nuevos usando la relación bidireccional
                // Con CascadeType.ALL, al guardar la billetera, Hibernate persistirá automáticamente los movimientos
                if (!movimientosNuevos.isEmpty()) {
                    List<com.budgetpro.infrastructure.persistence.entity.MovimientoEntity> movimientoEntities = 
                        mapper.toEntityMovimientos(movimientosNuevos, newEntity);
                    // Agregar cada movimiento a la colección de la billetera (establece relación bidireccional)
                    // El método agregarMovimiento() ya establece la relación en ambos sentidos
                    for (com.budgetpro.infrastructure.persistence.entity.MovimientoEntity movimientoEntity : movimientoEntities) {
                        newEntity.agregarMovimiento(movimientoEntity);
                    }
                }
                
                // Persistir la nueva billetera
                // Con CascadeType.ALL, Hibernate persistirá automáticamente los movimientos nuevos
                // Hibernate establecerá version = 1 tras insert gracias a @Version
                billeteraJpaRepository.save(newEntity);
                
                // Limpiar la lista de movimientos nuevos del agregado
                billetera.limpiarMovimientosNuevos();
            }
            
        } catch (DataIntegrityViolationException e) {
            // Capturar violaciones de integridad (UNIQUE constraint en proyecto_id, FK, etc.)
            if (e.getMessage() != null && e.getMessage().contains("uq_billetera_proyecto")) {
                throw new IllegalStateException(
                    "Ya existe una billetera para el proyecto: " + billetera.getProyectoId(), e);
            }
            // Relanzar otras violaciones de integridad
            throw e;
        } catch (ObjectOptimisticLockingFailureException e) {
            // Capturar errores de optimistic locking
            throw new OptimisticLockingFailureException(
                "La billetera fue modificada por otro proceso. Por favor, recarga e intenta nuevamente.", e);
        }
    }
}
