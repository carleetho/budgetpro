package com.budgetpro.application.recurso.usecase;

import com.budgetpro.application.recurso.dto.RecursoSearchResponse;
import com.budgetpro.application.recurso.port.in.BuscarRecursosUseCase;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

/**
 * Implementación del caso de uso para buscar recursos.
 * 
 * Responsabilidades:
 * - Orquestar la búsqueda de recursos desde la base de datos
 * - Mapear entidades JPA a DTOs de respuesta
 * - Aplicar filtros y límites
 * 
 * NO contiene lógica de negocio profunda (solo lectura).
 */
@Service
public class BuscarRecursosUseCaseImpl implements BuscarRecursosUseCase {

    private final RecursoJpaRepository recursoJpaRepository;

    public BuscarRecursosUseCaseImpl(RecursoJpaRepository recursoJpaRepository) {
        this.recursoJpaRepository = recursoJpaRepository;
    }

    @Override
    public List<RecursoSearchResponse> buscar(String searchQuery, TipoRecurso tipo, Integer limit) {
        List<RecursoEntity> entities;
        
        if (searchQuery == null || searchQuery.isBlank()) {
            // Si no hay búsqueda, listar todos
            entities = tipo != null
                    ? recursoJpaRepository.findAll().stream()
                            .filter(r -> r.getTipo() == tipo)
                            .toList()
                    : recursoJpaRepository.findAll();
        } else {
            // Búsqueda con filtro
            entities = tipo != null
                    ? recursoJpaRepository.buscarPorNombreYTipo(searchQuery, tipo)
                    : recursoJpaRepository.buscarPorNombre(searchQuery);
        }
        
        // Aplicar límite si se especifica
        Stream<RecursoEntity> stream = entities.stream();
        if (limit != null && limit > 0) {
            stream = stream.limit(limit);
        }
        
        return stream
                .map(this::toResponse)
                .toList();
    }

    private RecursoSearchResponse toResponse(RecursoEntity entity) {
        return new RecursoSearchResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getTipo(),
                entity.getUnidadBase(),
                entity.getEstado()
        );
    }
}
