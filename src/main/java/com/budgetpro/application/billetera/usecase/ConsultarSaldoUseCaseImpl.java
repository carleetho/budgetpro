package com.budgetpro.application.billetera.usecase;

import com.budgetpro.application.billetera.dto.SaldoResponse;
import com.budgetpro.application.billetera.port.in.ConsultarSaldoUseCase;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.repository.BilleteraJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación de la query para consultar saldo por proyecto.
 * 
 * Query READ según CQRS-Lite:
 * - Lee directamente desde BilleteraJpaRepository (no usa agregados del dominio)
 * - Mapea BilleteraEntity -> SaldoResponse (proyección de lectura)
 * - Sin lógica de negocio (solo proyección)
 * - Sin hidratar Dominio
 * 
 * NOTA: Esta query NO usa BilleteraRepository del dominio porque las lecturas
 * deben ser independientes de los agregados según CQRS-Lite.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarSaldoUseCaseImpl implements ConsultarSaldoUseCase {

    private final BilleteraJpaRepository jpaRepository;

    public ConsultarSaldoUseCaseImpl(BilleteraJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SaldoResponse consultarPorProyecto(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El ID del proyecto no puede ser nulo");
        }

        // Leer directamente desde el read model (BilleteraEntity)
        // NO usar agregados ni repositorios del dominio (CQRS-Lite)
        BilleteraEntity entity = jpaRepository.findByProyectoId(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No existe una billetera para el proyecto %s", proyectoId)
                ));

        // Mapear entidad JPA a DTO de lectura (proyección)
        return toResponse(entity);
    }

    /**
     * Convierte una BilleteraEntity a un SaldoResponse (proyección de lectura).
     * 
     * Solo mapea los campos necesarios para la lectura. No hidrata el agregado del dominio.
     * 
     * @param entity La entidad JPA
     * @return El DTO de respuesta con el saldo
     */
    private SaldoResponse toResponse(BilleteraEntity entity) {
        return new SaldoResponse(
            entity.getProyectoId(),
            entity.getSaldoActual()
        );
    }
}
