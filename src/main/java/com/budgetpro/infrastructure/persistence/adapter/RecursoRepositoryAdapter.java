package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.recurso.port.out.RecursoRepository;
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
 * Adaptador de infraestructura que implementa el puerto de salida RecursoRepository.
 * 
 * Realiza el mapeo entre el agregado del dominio (Recurso) y la entidad de persistencia (RecursoEntity),
 * manejando las conversiones y capturando excepciones de base de datos.
 * 
 * Sigue el patrón Adapter de Arquitectura Hexagonal.
 */
@Component
public class RecursoRepositoryAdapter implements RecursoRepository {

    private final RecursoJpaRepository jpaRepository;
    private final RecursoMapper mapper;
    private final com.budgetpro.infrastructure.security.service.SecurityContextService securityContextService;

    public RecursoRepositoryAdapter(
            RecursoJpaRepository jpaRepository,
            RecursoMapper mapper,
            com.budgetpro.infrastructure.security.service.SecurityContextService securityContextService) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.securityContextService = securityContextService;
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
                UUID createdBy = getCurrentUserId();
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

        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByNombre(String nombreNormalizado) {
        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre normalizado no puede ser nulo o vacío");
        }

        return jpaRepository.existsByNombreNormalizado(nombreNormalizado);
    }

    /**
     * Obtiene el ID del usuario actual del contexto de seguridad.
     * 
     * @return El UUID del usuario actual
     * @throws IllegalStateException si no se puede obtener el usuario del contexto
     */
    private UUID getCurrentUserId() {
        return securityContextService.getCurrentUserId();
    }
}
