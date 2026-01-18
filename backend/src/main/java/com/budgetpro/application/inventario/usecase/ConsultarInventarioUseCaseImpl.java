package com.budgetpro.application.inventario.usecase;

import com.budgetpro.application.inventario.dto.InventarioItemResponse;
import com.budgetpro.application.inventario.port.in.ConsultarInventarioUseCase;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para consultar inventario.
 */
@Service
public class ConsultarInventarioUseCaseImpl implements ConsultarInventarioUseCase {

    private final InventarioRepository inventarioRepository;

    public ConsultarInventarioUseCaseImpl(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioItemResponse> consultarPorProyecto(UUID proyectoId) {
        List<InventarioItem> items = inventarioRepository.findByProyectoId(proyectoId);
        
        return items.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private InventarioItemResponse toResponse(InventarioItem item) {
        return new InventarioItemResponse(
            item.getId().getValue(),
            item.getProyectoId(),
            item.getRecursoId(),
            item.getCantidadFisica(),
            item.getCostoPromedio(),
            item.getUbicacion(),
            item.getUltimaActualizacion(),
            item.getVersion()
        );
    }
}
