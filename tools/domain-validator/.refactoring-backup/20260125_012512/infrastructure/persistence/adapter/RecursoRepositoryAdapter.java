package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.domain.recurso.model.Recurso;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.mapper.RecursoMapper;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el puerto de salida
 * RecursoRepository.
 * 
 * Realiza el mapeo entre el agregado del dominio (Recurso) y la entidad de
 * persistencia (RecursoEntity), manejando las conversiones y capturando
 * excepciones de base de datos.
 * 
 * Sigue el patrón Adapter de Arquitectura Hexagonal.
 */
@Component
public class RecursoRepositoryAdapter implements RecursoRepository {

    private final RecursoJpaRepository jpaRepository;
    private final RecursoMapper mapper;

    public RecursoRepositoryAdapter(RecursoJpaRepository jpaRepository, RecursoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo");
        }

        try {
            Optional<RecursoEntity> existingEntityOpt = jpaRepository.findById(recurso.getId().getValue());

            if (existingEntityOpt.isPresent()) {
                // Actualizar entidad existente
                RecursoEntity existingEntity = existingEntityOpt.get();
                mapper.updateEntity(existingEntity, recurso);
                jpaRepository.save(existingEntity);
            } else {
                // Crear nueva entidad
                // TODO: Obtener createdBy del contexto de seguridad cuando se implemente Spring
                // Security
                // Por ahora, usar un UUID temporal o lanzar excepción
                UUID createdBy = getCurrentUserId(); // Implementación temporal
                RecursoEntity newEntity = mapper.toEntity(recurso, createdBy);
                jpaRepository.save(newEntity);
            }
        } catch (DataIntegrityViolationException e) {
            // Capturar violación de UNIQUE constraint (nombre_normalizado)
            if (e.getMessage() != null && e.getMessage().contains("uq_recurso_nombre")) {
                throw new IllegalStateException(
                        "Ya existe un recurso con el nombre normalizado: " + recurso.getNombre(), e);
            }
            // Relanzar otras violaciones de integridad
            throw e;
        }
    }

    @Override
    public Optional<Recurso> findById(RecursoId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del recurso no puede ser nulo");
        }

        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByNombre(String nombreNormalizado) {
        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre normalizado no puede ser nulo o vacío");
        }

        return jpaRepository.existsByNombreNormalizado(nombreNormalizado);
    }

    @Override
    public Optional<Recurso> findByNombre(String nombreNormalizado) {
        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre normalizado no puede ser nulo o vacío");
        }

        return jpaRepository.findByNombreNormalizado(nombreNormalizado).map(mapper::toDomain);
    }

    /**
     * Obtiene el ID del usuario actual del contexto de seguridad. Implementación
     * temporal hasta que se implemente Spring Security.
     * 
     * @return El UUID del usuario actual
     * @throws IllegalStateException si no se puede obtener el usuario del contexto
     */
    private UUID getCurrentUserId() {
        // TODO: Implementar obtención del usuario del SecurityContext cuando se agregue
        // Spring Security
        // Por ahora, usar un UUID temporal para permitir desarrollo y pruebas
        // Este método debe ser reemplazado por una implementación real cuando se
        // agregue autenticación
        return UUID.fromString("00000000-0000-0000-0000-000000000000"); // System user temporal
    }
}
