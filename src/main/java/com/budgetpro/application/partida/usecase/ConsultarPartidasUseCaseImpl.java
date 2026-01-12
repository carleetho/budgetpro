package com.budgetpro.application.partida.usecase;

import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.port.in.ConsultarPartidasUseCase;
import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.presupuesto.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso para consultar partidas.
 * 
 * Responsabilidades:
 * - Orquestar la consulta de partidas por presupuesto o proyecto
 * - Convertir agregados del dominio a DTOs de respuesta
 * - Controlar transacciones (read-only)
 * 
 * NOTA: Las partidas se obtienen a través del agregado Presupuesto (son entidades internas).
 * NO contiene lógica de negocio profunda (eso está en el Agregado Presupuesto).
 */
@Service
@Transactional(readOnly = true)
public class ConsultarPartidasUseCaseImpl implements ConsultarPartidasUseCase {

    private final PresupuestoRepository presupuestoRepository;

    public ConsultarPartidasUseCaseImpl(PresupuestoRepository presupuestoRepository) {
        this.presupuestoRepository = presupuestoRepository;
    }

    @Override
    public List<PartidaResponse> consultarPorPresupuesto(UUID presupuestoId) {
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El ID del presupuesto no puede ser nulo");
        }

        // Obtener el agregado Presupuesto (con sus partidas internas)
        PresupuestoId id = PresupuestoId.of(presupuestoId);
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No existe un presupuesto con ID %s", presupuestoId)
                ));

        // Obtener partidas desde el agregado raíz
        List<Partida> partidas = presupuesto.getPartidas();
        return partidas.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PartidaResponse> consultarPorProyecto(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El ID del proyecto no puede ser nulo");
        }

        // Obtener el presupuesto del proyecto (puede haber múltiples, obtenemos el primero)
        // NOTA: Esta es una simplificación. En el futuro, podría haber múltiples presupuestos por proyecto.
        Presupuesto presupuesto = presupuestoRepository.findByProyectoId(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No existe un presupuesto para el proyecto %s", proyectoId)
                ));

        // Obtener partidas desde el agregado raíz
        List<Partida> partidas = presupuesto.getPartidas();
        return partidas.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Convierte un agregado del dominio a DTO de respuesta.
     * 
     * @param partida El agregado del dominio
     * @return El DTO de respuesta con saldos desglosados
     */
    private PartidaResponse toResponse(Partida partida) {
        return new PartidaResponse(
            partida.getId().getValue(),
            partida.getProyectoId(),
            partida.getPresupuestoId(),
            partida.getCodigo().getValue(),
            partida.getNombre(),
            partida.getTipo().name(),
            mapEstado(partida.getEstado()),
            new PartidaResponse.SaldosPartidaResponse(
                partida.getMontoPresupuestado().toBigDecimal(),
                partida.getMontoReservado().toBigDecimal(),
                partida.getMontoEjecutado().toBigDecimal(),
                partida.getSaldoDisponible().toBigDecimal()
            ),
            partida.getVersion()
        );
    }

    /**
     * Mapea el estado del dominio al DTO de respuesta.
     */
    private PartidaResponse.EstadoPartidaResponse mapEstado(com.budgetpro.domain.finanzas.partida.EstadoPartida estado) {
        return switch (estado) {
            case BORRADOR -> PartidaResponse.EstadoPartidaResponse.BORRADOR;
            case APROBADA -> PartidaResponse.EstadoPartidaResponse.APROBADA;
            case CERRADA -> PartidaResponse.EstadoPartidaResponse.CERRADA;
        };
    }
}
